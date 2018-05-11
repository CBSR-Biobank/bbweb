package org.biobank.controllers

import org.biobank.domain._
import org.biobank.domain.annotations._
import org.biobank.domain.studies.{ EnabledStudy, Study }
import org.biobank.fixtures._
import scala.collection.mutable.ListBuffer
import play.api.libs.json._
import play.api.test.Helpers._

trait AnnotationsControllerSharedSpec[T <: ConcurrencySafeEntity[_] with HasAnnotations[_]]
    extends ControllerFixture { this: ControllerFixture =>

  import org.biobank.AnnotationTestUtils._
  import org.biobank.matchers.JsonMatchers._

  protected val nameGenerator: NameGenerator

  protected def entityName(): String

  protected def createEntity(annotationTypes: Set[AnnotationType],
                             annotations:     Set[Annotation]): T

  protected def entityFromRepository(id: String): DomainValidation[T]

  protected def updateUri(entity: T): String

  /**
   * create pairs of annotation types and annotation of each value type plus a second of type select that
   * allows multiple selections
   *
   * the result is a map where the keys are the annotation types and the values are the corresponding
   * annotations
   */
  protected def createAnnotationsAndTypes() = {
    val options = Seq(nameGenerator.next[String],
                      nameGenerator.next[String],
                      nameGenerator.next[String])
    val multipleSelectType = factory.createAnnotationType(AnnotationValueType.Select, Some(2), options)
    val multipleSelectAnnotation = factory.createAnnotationWithValues(multipleSelectType)

    (AnnotationValueType.values.map { vt =>
       vt match {
         case AnnotationValueType.Select =>
           val annotationType = factory.createAnnotationType(vt, Some(1), options)
           (annotationType, factory.createAnnotationWithValues(annotationType))
         case _ =>
           val annotationType = factory.createAnnotationType(vt, None, Seq.empty)
           (annotationType, factory.createAnnotationWithValues(annotationType))
       }
     }.toList ++
       List((multipleSelectType, multipleSelectAnnotation))).toMap
  }

  def annotationTypeUpdateSharedBehaviour() = {

    it(s"update a ${entityName} with annotations") {
      val annotTypeData = createAnnotationsAndTypes
      val addedAnnotations = ListBuffer.empty[Annotation]

      val entity = createEntity(annotTypeData.keys.toSet, Set.empty)
      entity.annotations must have size 0

      annotTypeData.values.zipWithIndex.foreach { case (annotation, index) =>
        val reply = makeAuthRequest(POST,
                                    updateUri(entity),
                                    Json.obj("expectedVersion" -> (entity.version + index))
                                      ++ annotationToJson(annotation)).value
        reply must beOkResponseWithJsonReply

        addedAnnotations += annotation

        val replyAnnotations = (contentAsJson(reply) \ "data" \ "annotations").validate[List[Annotation]]
        replyAnnotations must be (jsSuccess)
        replyAnnotations.get.sortBy(_.annotationTypeId.id) must equal (
          addedAnnotations.sortBy(_.annotationTypeId.id))
      }
    }

    it(s"fail when adding annotation and ${entityName} parent does not have annotation types") {
      val annotation = factory.createAnnotation
      val entity = createEntity(Set.empty, Set(annotation))
      val reply = makeAuthRequest(POST,
                                  updateUri(entity),
                                  Json.obj("expectedVersion" -> entity.version)
                                    ++ annotationToJson(annotation)).value
      reply must beNotFoundWithMessage("does not have annotation type")
    }

    it("fail when adding annotation and annotation has invalid annotation type id") {
      val annotationType = factory.createAnnotationType
      val annotation = factory.createAnnotation
        .copy(annotationTypeId = AnnotationTypeId(nameGenerator.next[Annotation]))

      val entity = createEntity(Set(annotationType), Set(annotation))

      val reply = makeAuthRequest(POST,
                                  updateUri(entity),
                                  Json.obj("expectedVersion" -> entity.version)
                                    ++ annotationToJson(annotation)).value
      reply must beNotFoundWithMessage("does not have annotation type")
    }

    it("fail when adding annotation with an invalid version") {
      val annotation = factory.createAnnotation
      val entity = createEntity(Set.empty, Set(annotation))
      val reply = makeAuthRequest(POST,
                                  updateUri(entity),
                                  Json.obj("expectedVersion" -> (entity.version + 1))
                               ++ annotationToJson(annotation)).value
      reply must beBadRequestWithMessage("expected version doesn't match current version")
    }

  }

  def annotationTypeRemoveSharedBehaviour() = {

    it("remove an annotation") {
      val annotationType = factory.createAnnotationType
      val annotation = factory.createAnnotation
      val entity = createEntity(Set(annotationType), Set(annotation))
      val reply = makeAuthRequest(
          DELETE,
          updateUri(entity) + s"/${annotation.annotationTypeId}/${entity.version}").value
      reply must beOkResponseWithJsonReply
      val replyAnnotations = (contentAsJson(reply) \ "data" \ "annotations").validate[List[Annotation]]
      replyAnnotations must be (jsSuccess)
      replyAnnotations.get must be (List.empty[Annotation])
    }

    it("fail when attempting to remove a required annotation") {
      val annotationType = factory.createAnnotationType.copy(required = true)
      val annotation = factory.createAnnotation
      val entity = createEntity(Set(annotationType), Set(annotation))
      val reply = makeAuthRequest(
          DELETE,
          updateUri(entity) + s"/${annotation.annotationTypeId}/${entity.version}").value
      reply must beBadRequestWithMessage("annotation is required")
    }
  }

}

trait StudyAnnotationsControllerSharedSpec[T <: ConcurrencySafeEntity[_] with HasAnnotations[_]]
    extends AnnotationsControllerSharedSpec[T] {
  import org.biobank.TestUtils._
  import org.biobank.AnnotationTestUtils._

  protected def getStudy(entity: T): DomainValidation[EnabledStudy]

  protected def createAnnotationType() = {
    val name = nameGenerator.next[AnnotationType]
    AnnotationType(
      id            = AnnotationTypeId(nameGenerator.next[AnnotationType]),
      slug          = Slug(name),
      name          = name,
      description   = None,
      valueType     = AnnotationValueType.Text,
      maxValueCount = None,
      options       = Seq.empty,
      required      = true)
  }

  override def annotationTypeUpdateSharedBehaviour() = {

    super.annotationTypeUpdateSharedBehaviour

    it("fail when adding an annotation on a non enabled study") {
      val annotationType = factory.createAnnotationType
      val annotation = factory.createAnnotation
      val entity = createEntity(Set(annotationType), Set(annotation))

      getStudy(entity) mustSucceed { study =>
        study.disable mustSucceed { disabledStudy =>
          studyRepository.put(disabledStudy)

          updateOnNonEnabledStudy(disabledStudy, entity, annotation)

          disabledStudy.retire mustSucceed { retiredStudy =>
            studyRepository.put(retiredStudy)
            updateOnNonEnabledStudy(retiredStudy, entity, annotation)
          }
        }
      }
    }

    def updateOnNonEnabledStudy(study: Study, entity: T, annotation: Annotation) {
      study must not be an [EnabledStudy]
      val reply = makeAuthRequest(POST,
                                  updateUri(entity),
                                  Json.obj("expectedVersion" -> entity.version) ++
                                    annotationToJson(annotation)).value
      reply must beBadRequestWithMessage("InvalidStatus: study not enabled")

      ()
    }
  }

}
