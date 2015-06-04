package org.biobank.controllers.study

import org.biobank.fixture._
import org.biobank.domain._
import org.biobank.domain.study._
import org.biobank.domain.JsonHelper._

import play.api.test.Helpers._
import play.api.libs.json._
import org.slf4j.LoggerFactory
import org.joda.time.DateTime
import org.scalatest.Tag
import org.scalatest.FlatSpec

trait StudyAnnotTypeControllerSpec[T <: StudyAnnotationType]
    extends ControllerFixture {

  val log = LoggerFactory.getLogger(this.getClass)

  val nameGenerator = new NameGenerator(this.getClass)

  protected def annotationTypeRepository: ReadWriteRepository[AnnotationTypeId, T]

  protected def createAnnotationType(): T

  protected def annotationTypeCopyWithId(at: T, id: AnnotationTypeId): T

  protected def annotationTypeCopyWithStudyId(at: T, id: StudyId): T

  protected def annotationTypeCopyWithVersion(at: T, version: Long): T

  protected def annotationTypeCopyWithName(at: T, name: String): T

  protected val uriPart: String

  protected def uri(study: Study): String =
    s"/studies/${study.id.id}/$uriPart"

  protected def uri(study: Study, annotationType: T): String =
    uri(study) + s"/${annotationType.id.id}"

  protected def uriWithQuery(study: Study, annotationType: T): String =
    uri(study) + s"?annotTypeId=${annotationType.id.id}"

  protected def uri(study: Study, annotationType: T, version: Long): String =
    uri(study, annotationType) + s"/${version}"

  protected  def annotTypeToAddCmdJson(annotType: T) = {
    Json.obj(
      "studyId"       -> annotType.studyId.id,
      "name"          -> annotType.name,
      "description"   -> annotType.description,
      "valueType"     -> annotType.valueType.toString,
      "maxValueCount" -> annotType.maxValueCount,
      "options"       -> annotType.options
    )
  }

  protected def annotTypeToUpdateCmdJson(annotType: T) = {
    annotTypeToAddCmdJson(annotType) ++ Json.obj(
      "id"              -> annotType.id.id,
      "expectedVersion" -> Some(annotType.version)
    )
  }

  def annotationTypeBehaviour() = {

    "GET /studies/{studyId}/" + uriPart must {

      "list none" in {
        val study = factory.createDisabledStudy
        studyRepository.put(study)

        val json = makeRequest(GET, uri(study))
        (json \ "status").as[String] must include ("success")
        val jsonList = (json \ "data").as[List[JsObject]]
        jsonList must have size 0
      }

      "list a single collection event annotation type" in {
        val study = factory.createDisabledStudy
        studyRepository.put(study)

        val annotType = createAnnotationType
        annotationTypeRepository.put(annotType)

        val json = makeRequest(GET, uri(study))
        (json \ "status").as[String] must include ("success")
        val jsonList = (json \ "data").as[List[JsObject]]
        jsonList must have size 1
        compareObj(jsonList(0), annotType)
      }

      "get a single collection event annotation type" in {
        val study = factory.createDisabledStudy
        studyRepository.put(study)

        val annotType = createAnnotationType()
        annotationTypeRepository.put(annotType)

        val json = makeRequest(GET, uriWithQuery(study, annotType)).as[JsObject]
        (json \ "status").as[String] must include ("success")
        val jsonObj = (json \ "data").as[JsObject]
        compareObj(jsonObj, annotType)
      }

      "list multiple collection event annotation types" in {
        val study = factory.createDisabledStudy
        studyRepository.put(study)

        val annotTypes = List(
          createAnnotationType(),
          createAnnotationType())
        annotTypes map { annotType => annotationTypeRepository.put(annotType) }

        val json = makeRequest(GET, uri(study))
        (json \ "status").as[String] must include ("success")
        val jsonList = (json \ "data").as[List[JsObject]]

        jsonList must have size annotTypes.size
        (jsonList zip annotTypes).map { item => compareObj(item._1, item._2) }
        ()
      }

      "fail for an invalid study ID" in {
        val study = factory.createDisabledStudy

        val json = makeRequest(GET, uri(study), BAD_REQUEST)
        (json \ "status").as[String] must include ("error")
        (json \ "message").as[String] must include ("invalid study id")
      }

      "fail for an invalid study ID when using an annotation type id" in {
        val study = factory.createDisabledStudy
        val annotType = createAnnotationType()

        val json = makeRequest(GET, uriWithQuery(study, annotType), BAD_REQUEST)
        (json \ "status").as[String] must include ("error")
        (json \ "message").as[String] must include ("invalid study id")
      }

      "fail for an invalid collection event annotation type id" in {
        val study = factory.createDisabledStudy
        studyRepository.put(study)

        val annotType = createAnnotationType()

        val json = makeRequest(GET, uriWithQuery(study, annotType), NOT_FOUND)
        (json \ "status").as[String] must include ("error")
        (json \ "message").as[String] must include ("annotation type does not exist")
      }

    }

    "POST /studies/" + uriPart must {
      "add a collection event annotation type" in {
        val study = factory.createDisabledStudy
        studyRepository.put(study)

        val annotType = createAnnotationType()
        val json = makeRequest(POST, uri(study), json = annotTypeToAddCmdJson(annotType))
        (json \ "status").as[String] must include ("success")
      }

      "not add a collection event annotation type to an enabled study" in {
        addOnNonDisabledStudy(factory.createEnabledStudy)
      }

      "not add a collection event annotation type to an retired study" in {
        addOnNonDisabledStudy(factory.createRetiredStudy)
      }

      "fail when adding and the study IDs do not match" in {
        val study = factory.createDisabledStudy
        studyRepository.put(study)
        val annotType = createAnnotationType()

        val study2 = factory.createDisabledStudy

        val jsonCmd = annotTypeToAddCmdJson(annotType)
        val json = makeRequest(POST, uri(study2), BAD_REQUEST, jsonCmd)
        (json \ "status").as[String] must include ("error")
        (json \ "message").as[String] must include ("study id mismatch")
      }

      "allow adding an annotation type with same name on two different studies" in {
        val commonName = nameGenerator.next[AnnotationType]

        (0 until 2).foreach { x =>
          val study = factory.createDisabledStudy
          studyRepository.put(study)

          val annotType = annotationTypeCopyWithName(createAnnotationType(), commonName)

          val cmdJson = annotTypeToAddCmdJson(annotType)
          val json = makeRequest(POST, uri(study), json = cmdJson)
          (json \ "status").as[String] must include ("success")
        }
      }


      def addOnNonDisabledStudy(study: Study) {
        studyRepository.put(study)

        val annotType = annotationTypeCopyWithStudyId(createAnnotationType(),  study.id)
        annotationTypeRepository.put(annotType)

        val json = makeRequest(
          POST,
          uri(study),
          BAD_REQUEST,
          annotTypeToAddCmdJson(annotType))

        (json \ "status").as[String] must include ("error")
        (json \ "message").as[String] must include ("is not disabled")
      }
    }

    "PUT /studies/" + uriPart must {
      "update a collection event annotation type" in {
        val study = factory.createDisabledStudy
        studyRepository.put(study)

        val annotType = createAnnotationType()
        annotationTypeRepository.put(annotType)

        val annotType2 = annotationTypeCopyWithVersion(
          annotationTypeCopyWithId(annotType, annotType.id),
          annotType.version)

        val json = makeRequest(PUT, uri(study, annotType), json = annotTypeToUpdateCmdJson(annotType2))

        (json \ "status").as[String] must include ("success")
      }

      "not update a collection event annotation type on an enabled study" in {
        updateOnNonDisabledStudy(factory.createEnabledStudy)
      }

      "not update a collection event annotation type on an retired study" in {
        updateOnNonDisabledStudy(factory.createRetiredStudy)
      }

      "fail when updating and study IDs do not match" in {
        val study = factory.createDisabledStudy
        studyRepository.put(study)

        val annotType = createAnnotationType()
        annotationTypeRepository.put(annotType)

        val annotType2 = annotationTypeCopyWithVersion(
          annotationTypeCopyWithId(createAnnotationType(), annotType.id),
          annotType.version)

        val study2 = factory.createDisabledStudy

        val json = makeRequest(PUT,
                               uri(study2, annotType),
                               BAD_REQUEST,
                               json = annotTypeToUpdateCmdJson(annotType2))
        (json \ "status").as[String] must include ("error")
        (json \ "message").as[String] must include ("study id mismatch")
      }

      "fail when updating and annotation type IDs do not match" in {
        val study = factory.createDisabledStudy
        studyRepository.put(study)

        val annotType = createAnnotationType()
        annotationTypeRepository.put(annotType)

        val annotType2 = annotationTypeCopyWithVersion(
          annotationTypeCopyWithId(createAnnotationType(), annotType.id),
          annotType.version)

        val annotType3 = createAnnotationType()

        val json = makeRequest(PUT,
                               uri(study, annotType3),
                               BAD_REQUEST,
                               json = annotTypeToUpdateCmdJson(annotType2))
        (json \ "status").as[String] must include ("error")
        (json \ "message").as[String] must include ("annotation type id mismatch")
      }

      "allow a updating annotation types on two different studies to same name" in {
        val commonName = nameGenerator.next[AnnotationType]

        (0 until 2).map { study =>
          val study = factory.createDisabledStudy
          studyRepository.put(study)
          val annotType = createAnnotationType
          annotationTypeRepository.put(annotType)
          (study, annotType)
        } foreach { case (study: Study, at) =>
            val annotType = annotationTypeCopyWithName(at, commonName)
            val cmdJson = annotTypeToUpdateCmdJson(annotType)
            val json = makeRequest(PUT, uri(study, annotType), json = cmdJson)
            (json \ "status").as[String] must include ("success")
        }
      }

      def updateOnNonDisabledStudy(study: Study) {
        studyRepository.put(study)

        val annotType = annotationTypeCopyWithStudyId(createAnnotationType(),  study.id)
        annotationTypeRepository.put(annotType)

        val json = makeRequest(
          PUT,
          uri(study, annotType),
          BAD_REQUEST,
          annotTypeToUpdateCmdJson(annotType))

        (json \ "status").as[String] must include ("error")
        (json \ "message").as[String] must include ("is not disabled")
      }
    }

    "DELETE /studies/" + uriPart must {
      "remove a collection event annotation type" in {
        val study = factory.createDisabledStudy
        studyRepository.put(study)

        val annotType = createAnnotationType()
        annotationTypeRepository.put(annotType)

        val json = makeRequest(DELETE, uri(study, annotType, annotType.version))

        (json \ "status").as[String] must include ("success")
      }

      "not remove a collection event annotation type on an enabled study" in {
        removeOnNonDisabledStudy(factory.createEnabledStudy)
      }

      "not remove a collection event annotation type on an retired study" in {
        removeOnNonDisabledStudy(factory.createRetiredStudy)
      }

      def removeOnNonDisabledStudy(study: Study) {
        studyRepository.put(study)

        val sg = factory.createSpecimenGroup
        specimenGroupRepository.put(sg)

        val annotType = createAnnotationType()
        annotationTypeRepository.put(annotType)

        val json = makeRequest(
          DELETE,
          uri(study, annotType, annotType.version),
          BAD_REQUEST)

        (json \ "status").as[String] must include ("error")
        (json \ "message").as[String] must include ("is not disabled")
      }
    }
  }

}

