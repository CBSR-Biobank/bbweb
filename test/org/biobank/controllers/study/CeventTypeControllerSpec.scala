package org.biobank.controllers.study

import org.biobank.fixture._
import org.biobank.domain.study.{ CollectionEventType, Study }
import org.biobank.fixture.ControllerFixture
import org.biobank.domain.JsonHelper
import org.biobank.domain.study._

import play.api.test.Helpers._
import play.api.libs.json._
import org.joda.time.DateTime

class CeventTypeControllerSpec extends ControllerFixture with JsonHelper {
  import org.biobank.TestUtils._

  def uri(): String = "/studies/cetypes"

  def uri(study: Study): String = uri + s"/${study.id.id}"

  def uri(study: Study, ceventType: CollectionEventType): String =
    uri(study) + s"/${ceventType.id.id}"

  def uri(ceventType: CollectionEventType, path: String): String =
    uri + s"/$path/${ceventType.id.id}"

  def uriWithQuery(study: Study, ceventType: CollectionEventType): String =
    uri(study) + s"?cetId=${ceventType.id.id}"

  def uri(study: Study, ceventType: CollectionEventType, version: Long): String =
    uri(study, ceventType) + s"/$version"

  def uri(study: Study, ceventType: CollectionEventType, version: Long, id: String): String =
    uri(study, ceventType, version) + s"/$id"

  def createEntities(fn: (Study, CollectionEventType) => Unit): Unit = {
    val disabledStudy = factory.createDisabledStudy
    studyRepository.put(disabledStudy)

    val cet = factory.createCollectionEventType
    collectionEventTypeRepository.put(cet)

    fn(disabledStudy, cet)
  }

  def cetToAddCmd(cet: CollectionEventType) = {
    Json.obj(
      "studyId"         -> cet.studyId.id,
      "name"            -> cet.name,
      "description"     -> cet.description,
      "recurring"       -> cet.recurring,
      "specimenSpecs"   -> cet.specimenSpecs,
      "annotationTypes" -> cet.annotationTypes)
  }

  def addOnNonDisabledStudy(study: Study) {
    studyRepository.put(study)

    val cet = factory.createCollectionEventType.copy(
        studyId         = study.id,
        specimenSpecs   = Set(factory.createCollectionSpecimenSpec),
        annotationTypes = Set(factory.createAnnotationType))

    val json = makeRequest(POST, uri(study), BAD_REQUEST, cetToAddCmd(cet))

    (json \ "status").as[String] must include ("error")

    (json \ "message").as[String] must include regex("InvalidStatus.*study not disabled")
  }

  def updateWithInvalidVersion(path: String, jsonField: JsObject) {
    createEntities { (study, cet) =>
      val reqJson = jsonField ++ Json.obj("id"              -> cet.id.id,
                                          "studyId"         -> study.id,
                                          "expectedVersion" -> (cet.version + 1))

      val json = makeRequest(POST, uri(cet, path), BAD_REQUEST, reqJson)

      (json \ "status").as[String] must include ("error")

      (json \ "message").as[String] must include regex (".*expected version doesn't match current version.*")
    }
  }

  def updateOnInvalidCeventType(path: String, jsonField: JsObject) {
    val study = factory.createDisabledStudy
    studyRepository.put(study)

    val cet = factory.createCollectionEventType

    val reqJson = jsonField ++ Json.obj("id"              -> cet.id.id,
                                        "studyId"         -> study.id,
                                        "expectedVersion" -> cet.version)

    val json = makeRequest(POST, uri(cet, path), NOT_FOUND, reqJson)

    (json \ "status").as[String] must include ("error")

    (json \ "message").as[String] must include regex ("IdNotFound.*collection event type")
  }

  def updateOnNonDisabledStudy(study: Study, path: String, jsonField: JsObject) {
    study must not be an [DisabledStudy]
    studyRepository.put(study)

    val cet = factory.createCollectionEventType.copy(
        studyId            = study.id,
        specimenSpecs   = Set(factory.createCollectionSpecimenSpec),
        annotationTypes = Set(factory.createAnnotationType))
    collectionEventTypeRepository.put(cet)

    val reqJson = jsonField ++ Json.obj("studyId"         -> study.id.id,
                                        "id"              -> cet.id.id,
                                        "expectedVersion" -> cet.version)

    val json = makeRequest(POST, uri(cet, path), BAD_REQUEST, reqJson)

    (json \ "status").as[String] must include ("error")

    (json \ "message").as[String] must include regex ("InvalidStatus.*study not disabled")
  }

  def removeOnNonDisabledStudy(study: Study) {
    studyRepository.put(study)

    val cet = factory.createCollectionEventType.copy(
        studyId         = study.id,
        specimenSpecs   = Set(factory.createCollectionSpecimenSpec),
        annotationTypes = Set(factory.createAnnotationType))
    collectionEventTypeRepository.put(cet)

    val json = makeRequest(DELETE, uri(study, cet, cet.version), BAD_REQUEST)

    (json \ "status").as[String] must include ("error")

    (json \ "message").as[String] must include regex ("InvalidStatus.*study not disabled")
  }

  "Collection Event Type REST API" when {

    "GET /studies/cetypes" must {

      "list none" in {
        val study = factory.createDisabledStudy
        studyRepository.put(study)

        val json = makeRequest(GET, uri(study))
                              (json \ "status").as[String] must include ("success")
        val jsonList = (json \ "data").as[List[JsObject]]
        jsonList must have size 0
      }

      "list a single collection event type" in {
        createEntities { (study, cet) =>
          val json = makeRequest(GET, uri(study))

          (json \ "status").as[String] must include ("success")
          val jsonList = (json \ "data").as[List[JsObject]]
          jsonList must have size 1
          compareObj(jsonList(0), cet)
        }
      }

      "get a single collection event type" in {
        createEntities { (study, cet) =>
          val json = makeRequest(GET, uriWithQuery(study, cet))

          (json \ "status").as[String] must include ("success")
          val jsonObj = (json \ "data").as[JsObject]
          compareObj(jsonObj, cet)
        }
      }

      "list multiple collection event types" in {
        createEntities { (study, cet) =>
          val cet2 = factory.createCollectionEventType.copy(
              specimenSpecs   = Set(factory.createCollectionSpecimenSpec),
              annotationTypes = Set(factory.createAnnotationType))

          val cetypes = List(cet, cet2)
          cetypes map { cet => collectionEventTypeRepository.put(cet) }

          val json = makeRequest(GET, uri(study))
                                (json \ "status").as[String] must include ("success")
          val jsonList = (json \ "data").as[List[JsObject]]
          jsonList must have size cetypes.size

          jsonList.foreach { jsonCet =>
            val jsonId = (jsonCet \ "id").as[String]
            val cet = cetypes.find { x => x.id.id == jsonId }
            cet mustBe defined
            compareObj(jsonCet, cet.value)
          }
          ()
        }
      }

      "fail for invalid study id" in {
        val study = factory.createDisabledStudy

        val json = makeRequest(GET, uri(study), NOT_FOUND)

        (json \ "status").as[String] must include ("error")

        (json \ "message").as[String] must include regex("IdNotFound.*study")
      }

      "fail for an invalid study ID when using a collection event type id" in {
        val study = factory.createDisabledStudy
        val cet = factory.createCollectionEventType

        val json = makeRequest(GET, uriWithQuery(study, cet), NOT_FOUND)

        (json \ "status").as[String] must include ("error")

        (json \ "message").as[String] must include regex("IdNotFound.*study")
      }

      "fail for an invalid collection event type id" in {
        val study = factory.createDisabledStudy
        studyRepository.put(study)

        val cet = factory.createCollectionEventType

        val json = makeRequest(GET, uriWithQuery(study, cet), NOT_FOUND)

        (json \ "status").as[String] must include ("error")

        (json \ "message").as[String] must include regex("IdNotFound.*collection event type")
      }

    }

    "POST /studies/cetypes/:studyId" must {

      "add a collection event type" in {
        val study = factory.createDisabledStudy
        studyRepository.put(study)

        val cet = factory.createCollectionEventType
        val json = makeRequest(POST, uri(study), cetToAddCmd(cet))

        (json \ "status").as[String] must include ("success")

        val jsonId = CollectionEventTypeId((json \ "data" \ "id").as[String])
        jsonId.id.length must be > 0
        collectionEventTypeRepository.getByKey(jsonId) mustSucceed { repoCet =>
          repoCet must have (
            'studyId     (cet.studyId),
            'id          (jsonId),
            'version     (cet.version),
            'name        (cet.name),
            'description (cet.description),
            'recurring   (cet.recurring)
          )

          repoCet.specimenSpecs must have size cet.specimenSpecs.size
          repoCet.annotationTypes must have size cet.annotationTypes.size
          checkTimeStamps(repoCet, cet.timeAdded, None)
        }
      }

      "allow adding a collection event type with same name on two different studies" in {
        val cet = factory.createCollectionEventType

        List(factory.createDisabledStudy, factory.createDisabledStudy) foreach { study =>
          studyRepository.put(study)

          val json = makeRequest(POST, uri(study), cetToAddCmd(cet.copy(studyId = study.id)))
                                (json \ "status").as[String] must include ("success")

          val jsonId = CollectionEventTypeId((json \ "data" \ "id").as[String])
          jsonId.id.length must be > 0
          collectionEventTypeRepository.getByKey(jsonId) mustSucceed { repoCet =>
            repoCet must have (
              'studyId     (study.id),
              'id          (jsonId),
              'version     (cet.version),
              'name        (cet.name),
              'description (cet.description),
              'recurring   (cet.recurring)
            )

            repoCet.specimenSpecs must have size cet.specimenSpecs.size
            repoCet.annotationTypes must have size cet.annotationTypes.size
            checkTimeStamps(repoCet, cet.timeAdded, None)
          }
        }
      }

      "not add a collection event type to an enabled study" in {
        addOnNonDisabledStudy(factory.createEnabledStudy)
      }

      "not add a collection event type to an retired study" in {
        addOnNonDisabledStudy(factory.createRetiredStudy)
      }

      "fail when adding and study IDs is invalid" in {
        val study = factory.createDisabledStudy
        val cet = factory.createCollectionEventType

        val json = makeRequest(POST, uri(study), NOT_FOUND, cetToAddCmd(cet))

        (json \ "status").as[String] must include ("error")

        (json \ "message").as[String] must include regex ("IdNotFound.*study")
      }

      "fail when adding a collection event type with a duplicate name to the same study" in {
        val study = factory.createDisabledStudy
        studyRepository.put(study)

        val ceventType = factory.createCollectionEventType
        collectionEventTypeRepository.put(ceventType)

        val ceventType2 = factory.createCollectionEventType.copy(name = ceventType.name)
        val json = makeRequest(POST, uri(study), FORBIDDEN, cetToAddCmd(ceventType2))

        (json \ "status").as[String] must include ("error")

        (json \ "message").as[String] must include ("collection event type with name already exists")
      }

    }

    "DELETE /studies/cetypes/:studyId/:id/:ver" must {

      "remove a collection event type" in {
        createEntities { (study, cet) =>
          val json = makeRequest(DELETE, uri(study, cet, cet.version))

          (json \ "status").as[String] must include ("success")

          collectionEventTypeRepository.getByKey(cet.id) mustFail "IdNotFound.*collection event type.*"
        }
      }

      "not remove a collection event type on an enabled study" in {
        removeOnNonDisabledStudy(factory.createEnabledStudy)
      }

      "not remove a collection event type on an retired study" in {
        removeOnNonDisabledStudy(factory.createRetiredStudy)
      }

      "not remove a collection event type that is in use" ignore {
        fail("write this test")
      }

    }

    "POST /studies/cetypes/name/:id" must {

      "update a collection event type's name" in {
        createEntities { (study, cet) =>
          val newName = nameGenerator.next[CollectionEventType]
          val json = makeRequest(POST, uri(cet, "name"),
                                 Json.obj("studyId"         -> cet.studyId.id,
                                          "expectedVersion" -> Some(cet.version),
                                          "name"            -> newName))

          (json \ "status").as[String] must include ("success")

          val jsonId = (json \ "data" \ "id").as[String]
          jsonId must be (cet.id.id)

          collectionEventTypeRepository.getByKey(cet.id) mustSucceed { repoCet =>
            compareObj((json \ "data").as[JsObject], repoCet)

            repoCet must have (
              'studyId     (cet.studyId),
              'version     (cet.version + 1),
              'name        (newName),
              'description (cet.description),
              'recurring   (cet.recurring)
            )

            repoCet.specimenSpecs must have size cet.specimenSpecs.size
            repoCet.annotationTypes must have size cet.annotationTypes.size
            checkTimeStamps(repoCet, cet.timeAdded, DateTime.now)
          }
        }
      }

      "allow updating to the same name on collection event types of two different studies" in {
        val studyCetTuples = (1 to 2).map { _ =>
            val study = factory.createDisabledStudy
            studyRepository.put(study)

            val cet = factory.createCollectionEventType
            collectionEventTypeRepository.put(cet)

            (study, cet)
          }

        val commonName = nameGenerator.next[CollectionEventType]
        studyCetTuples.foreach { case (study, cet) =>
          val json = makeRequest(POST,
                                 uri(cet, "name"),
                                 Json.obj("studyId"         -> cet.studyId,
                                          "id"              -> cet.id.id,
                                          "expectedVersion" -> cet.version,
                                          "name"            -> commonName))

          (json \ "status").as[String] must include ("success")

          val jsonId = CollectionEventTypeId((json \ "data" \ "id").as[String])
          jsonId.id.length must be > 0
          collectionEventTypeRepository.getByKey(jsonId) mustSucceed { repoCet =>
            repoCet must have (
              'studyId     (study.id),
              'id          (jsonId),
              'version     (cet.version + 1),
              'name        (commonName),
              'description (cet.description),
              'recurring   (cet.recurring)
            )

            repoCet.specimenSpecs must have size cet.specimenSpecs.size
            repoCet.annotationTypes must have size cet.annotationTypes.size
            checkTimeStamps(repoCet, cet.timeAdded, DateTime.now)
          }
        }
      }

      "fail when updating name to one already used by another collection event type in the same study" in {
        val study = factory.createDisabledStudy
        studyRepository.put(study)

        val cetList = (1 to 2).map { _ =>
            val cet = factory.createCollectionEventType
            collectionEventTypeRepository.put(cet)
            cet
          }.toList

        val duplicateName = cetList(0).name
        val json = makeRequest(POST,
                               uri(cetList(1), "name"),
                               FORBIDDEN,
                               Json.obj("studyId"         -> cetList(1).studyId,
                                        "id"              -> cetList(1).id.id,
                                        "expectedVersion" -> cetList(1).version,
                                        "name"            -> duplicateName))

        (json \ "status").as[String] must include ("error")

        (json \ "message").as[String] must include ("collection event type with name already exists")
      }

      "not update a collection event type's name on an enabled study" in {
        updateOnNonDisabledStudy(factory.createEnabledStudy,
                                 "name",
                                 Json.obj("name" -> nameGenerator.next[CollectionEventType]))
      }

      "not update a collection event type's name on an retired study" in {
        updateOnNonDisabledStudy(factory.createRetiredStudy,
                                 "name",
                                 Json.obj("name" -> nameGenerator.next[CollectionEventType]))
      }

      "fail when updating name and collection event type ID is invalid" in {
        updateOnInvalidCeventType("name", Json.obj("name" -> nameGenerator.next[CollectionEventType]))
      }

      "fail when updating name with an invalid version" in {
        updateWithInvalidVersion("name", Json.obj("name" -> nameGenerator.next[CollectionEventType]))
      }
    }

    "POST /studies/cetypes/description/:id" must {

      "update a collection event type's name" in {
        createEntities { (study, cet) =>
          val newDescription = Some(nameGenerator.next[CollectionEventType])
          val json = makeRequest(POST, uri(cet, "description"),
                                 Json.obj("studyId"         -> cet.studyId.id,
                                          "expectedVersion" -> Some(cet.version),
                                          "description"     -> newDescription))

          (json \ "status").as[String] must include ("success")

          val jsonId = (json \ "data" \ "id").as[String]
          jsonId must be (cet.id.id)

          collectionEventTypeRepository.getByKey(cet.id) mustSucceed { repoCet =>
            compareObj((json \ "data").as[JsObject], repoCet)

            repoCet must have (
              'studyId     (cet.studyId),
              'version     (cet.version + 1),
              'name        (cet.name),
              'description (newDescription),
              'recurring   (cet.recurring)
            )

            repoCet.specimenSpecs must have size cet.specimenSpecs.size
            repoCet.annotationTypes must have size cet.annotationTypes.size
            checkTimeStamps(repoCet, cet.timeAdded, DateTime.now)
          }
        }
      }

      "not update a collection event type's description on an enabled study" in {
        updateOnNonDisabledStudy(factory.createEnabledStudy,
                                 "description",
                                 Json.obj("description" -> nameGenerator.next[CollectionEventType]))
      }

      "not update a collection event type's description on an retired study" in {
        updateOnNonDisabledStudy(factory.createRetiredStudy,
                                 "description",
                                 Json.obj("description" -> nameGenerator.next[CollectionEventType]))
      }

      "fail when updating description and collection event type ID is invalid" in {
        updateOnInvalidCeventType("description",
                                  Json.obj("description" -> nameGenerator.next[CollectionEventType]))

      }

      "fail when updating description with an invalid version" in {
        updateWithInvalidVersion("description",
                                 Json.obj("description" -> nameGenerator.next[CollectionEventType]))
      }
    }

    "POST /studies/cetypes/recurring/:id" must {

      "update a collection event type's name" in {
        createEntities { (study, cet) =>
          var version = cet.version

          Set(true, false).foreach { recurring =>
            val json = makeRequest(POST, uri(cet, "recurring"),
                                   Json.obj("studyId"         -> cet.studyId.id,
                                            "expectedVersion" -> Some(version),
                                            "recurring"       -> recurring))

            version = version + 1

            (json \ "status").as[String] must include ("success")

            val jsonId = (json \ "data" \ "id").as[String]
            jsonId must be (cet.id.id)

            collectionEventTypeRepository.getByKey(cet.id) mustSucceed { repoCet =>
              compareObj((json \ "data").as[JsObject], repoCet)

              repoCet must have (
                'studyId     (cet.studyId),
                'version     (version),
                'name        (cet.name),
                'description (cet.description),
                'recurring   (recurring)
              )

              repoCet.specimenSpecs must have size cet.specimenSpecs.size
              repoCet.annotationTypes must have size cet.annotationTypes.size
              checkTimeStamps(repoCet, cet.timeAdded, DateTime.now)
            }
          }
        }
      }

      "not update a collection event type's recurring on an enabled study" in {
        updateOnNonDisabledStudy(factory.createEnabledStudy,
                                 "recurring",
                                 Json.obj("recurring" -> false))
      }

      "not update a collection event type's recurring on an retired study" in {
        updateOnNonDisabledStudy(factory.createRetiredStudy,
                                 "recurring",
                                 Json.obj("recurring" -> false))
      }

      "fail when updating recurring and collection event type ID is invalid" in {
        updateOnInvalidCeventType("recurring", Json.obj("recurring" -> false))
      }

      "fail when updating recurring with an invalid version" in {
        updateWithInvalidVersion("recurring", Json.obj("recurring" -> false))
      }
    }

    "POST /studies/cetypes/annottypes/:id" must {

      "add an annotation type" in {
        createEntities { (study, cet) =>
          val annotType = factory.createAnnotationType

          val reqJson = Json.obj(
              "id"              -> cet.id.id,
              "studyId"         -> cet.studyId.id,
              "expectedVersion" -> Some(cet.version)) ++
            annotationTypeToJsonNoId(annotType)

          val json = makeRequest(POST, uri(cet, "annottype"), reqJson)

          (json \ "status").as[String] must include ("success")

          val jsonId = (json \ "data" \ "id").as[String]
          jsonId must be (cet.id.id)

          collectionEventTypeRepository.getByKey(cet.id) mustSucceed { repoCet =>
            compareObj((json \ "data").as[JsObject], repoCet)

            repoCet must have (
              'studyId     (cet.studyId),
              'version     (cet.version + 1),
              'name        (cet.name),
              'description (cet.description),
              'recurring   (cet.recurring)
            )

            repoCet.specimenSpecs must have size cet.specimenSpecs.size
            repoCet.annotationTypes must have size 1

            repoCet.annotationTypes.head.uniqueId must not be empty
            repoCet.annotationTypes.head must have (
              'name          (annotType.name),
              'description   (annotType.description),
              'valueType     (annotType.valueType),
              'maxValueCount (annotType.maxValueCount),
              'options       (annotType.options),
              'required      (annotType.required)
            )

            checkTimeStamps(repoCet, cet.timeAdded, DateTime.now)
          }
        }
      }

      "fail when adding annotation type and collection event type ID does not exist" in {
        updateOnInvalidCeventType("annottype",
                                  annotationTypeToJsonNoId(factory.createAnnotationType))
      }

      "fail when adding annotation type and an invalid version" in {
        updateWithInvalidVersion("annottype",
                                  annotationTypeToJsonNoId(factory.createAnnotationType))
      }

      "not add an annotation type on an enabled study" in {
        updateOnNonDisabledStudy(factory.createEnabledStudy,
                                 "annottype",
                                  annotationTypeToJsonNoId(factory.createAnnotationType))
      }

      "not add an annotation type on an retired study" in {
        updateOnNonDisabledStudy(factory.createRetiredStudy,
                                 "annottype",
                                  annotationTypeToJsonNoId(factory.createAnnotationType))
      }

      "fail when adding annotation type and collection event type ID is invalid" in {
        updateOnInvalidCeventType("annottype",
                                  annotationTypeToJsonNoId(factory.createAnnotationType))
      }
    }

  }

  "DELETE /studies/cetypes/annottype/:id/:ver/:uniqueId" must {

      "remove an annotation type" in {
        createEntities { (study, cet) =>
          val annotationType = factory.createAnnotationType
          collectionEventTypeRepository.put(cet.copy(annotationTypes = Set(annotationType)))

          val json = makeRequest(
              DELETE,
              s"/studies/cetypes/annottype/${study.id}/${cet.id}/${cet.version}/${annotationType.uniqueId}")

          (json \ "status").as[String] must include ("success")

          val jsonId = (json \ "data" \ "id").as[String]
          jsonId must be (cet.id.id)

          collectionEventTypeRepository.getByKey(cet.id) mustSucceed { repoCet =>
            compareObj((json \ "data").as[JsObject], repoCet)

            repoCet must have (
              'studyId     (cet.studyId),
              'version     (cet.version + 1),
              'name        (cet.name),
              'description (cet.description),
              'recurring   (cet.recurring)
            )

            repoCet.specimenSpecs must have size cet.specimenSpecs.size
            repoCet.annotationTypes must have size 0

            checkTimeStamps(repoCet, cet.timeAdded, DateTime.now)
          }
        }
      }

      "fail when removing annotation type and an invalid version" in {
        createEntities { (study, cet) =>
          val annotationType = factory.createAnnotationType
          collectionEventTypeRepository.put(cet.copy(annotationTypes = Set(annotationType)))

          val badVersion = cet.version + 1

          val json = makeRequest(
              DELETE,
              s"/studies/cetypes/annottype/${study.id}/${cet.id}/$badVersion/${annotationType.uniqueId}",
              BAD_REQUEST)

          (json \ "status").as[String] must include ("error")

          (json \ "message").as[String] must include ("expected version doesn't match current version")
        }
      }

      "fail when removing annotation type and study ID does not exist" in {
        val studyId = nameGenerator.next[Study]
        val cetId = nameGenerator.next[CollectionEventType]

        val json = makeRequest(
            DELETE,
            s"/studies/cetypes/annottype/$studyId/$cetId/0/xyz", NOT_FOUND)

        (json \ "status").as[String] must include ("error")

        (json \ "message").as[String] must include regex ("IdNotFound.*study")
      }

      "fail when removing annotation type and collection event type ID does not exist" in {
        val study = factory.createDisabledStudy
        studyRepository.put(study)
        val cetId = nameGenerator.next[CollectionEventType]

        val json = makeRequest(
            DELETE,
            s"/studies/cetypes/annottype/${study.id}/$cetId/0/xyz", NOT_FOUND)

        (json \ "status").as[String] must include ("error")

        (json \ "message").as[String] must include regex ("IdNotFound.*collection event type")
      }

      "fail when removing an annotation type that does not exist" in {
        createEntities { (study, cet) =>
          val badUniqueId = nameGenerator.next[Study]
          val annotationType = factory.createAnnotationType

          collectionEventTypeRepository.put(cet.copy(annotationTypes = Set(annotationType)))

          val json = makeRequest(
              DELETE,
              s"/studies/cetypes/annottype/${study.id}/${cet.id}/${cet.version}/$badUniqueId",
              NOT_FOUND)

          (json \ "status").as[String] must include ("error")

          (json \ "message").as[String] must startWith ("annotation type does not exist")
        }
      }

      "fail when removing an annotation type on a non disabled study" in {
        List(factory.createEnabledStudy, factory.createRetiredStudy).foreach { study =>
          studyRepository.put(study)

          val annotationType = factory.createAnnotationType
          val cet = factory.createCollectionEventType.copy(studyId = study.id,
                                                       annotationTypes = Set(annotationType))
          collectionEventTypeRepository.put(cet)

          val json = makeRequest(
              DELETE,
              s"/studies/cetypes/annottype/${study.id}/${cet.id}/${cet.version}/${annotationType.uniqueId}",
              BAD_REQUEST)

          (json \ "status").as[String] must include ("error")

          (json \ "message").as[String] must include regex ("InvalidStatus.*study not disabled")
        }
      }

  }

  "POST /studies/cetypes/spcspec/:id" must {

      "add a specimen spec" in {
        createEntities { (study, cet) =>
          val spec = factory.createCollectionSpecimenSpec

          val reqJson = Json.obj("id"                          -> cet.id.id,
                                 "studyId"                     -> cet.studyId.id,
                                 "expectedVersion"             -> Some(cet.version)) ++
            collectionSpecimenSpecToJsonNoId(spec)

          val json = makeRequest(POST, uri(cet, "spcspec"), reqJson)

          (json \ "status").as[String] must include ("success")

          val jsonId = (json \ "data" \ "id").as[String]
          jsonId must be (cet.id.id)

          collectionEventTypeRepository.getByKey(cet.id) mustSucceed { repoCet =>
            compareObj((json \ "data").as[JsObject], repoCet)

            repoCet must have (
              'studyId     (cet.studyId),
              'version     (cet.version + 1),
              'name        (cet.name),
              'description (cet.description),
              'recurring   (cet.recurring)
            )

            repoCet.annotationTypes must have size cet.annotationTypes.size
            repoCet.specimenSpecs must have size 1

            repoCet.specimenSpecs.head.uniqueId must not be empty
            repoCet.specimenSpecs.head must have (
              'name                        (spec.name),
              'description                 (spec.description),
              'units                       (spec.units),
              'anatomicalSourceType        (spec.anatomicalSourceType),
              'preservationType            (spec.preservationType),
              'preservationTemperatureType (spec.preservationTemperatureType),
              'specimenType                (spec.specimenType)
            )

            checkTimeStamps(repoCet, cet.timeAdded, DateTime.now)
          }
        }
      }

      "fail when adding specimen spec and collection event type ID does not exist" in {
        updateOnInvalidCeventType("spcspec",
                                  collectionSpecimenSpecToJsonNoId(factory.createCollectionSpecimenSpec))
      }

      "fail when adding specimen spec and an invalid version" in {
        updateWithInvalidVersion("spcspec",
                                  collectionSpecimenSpecToJsonNoId(factory.createCollectionSpecimenSpec))
      }

      "not add an specimen spec on an enabled study" in {
        updateOnNonDisabledStudy(factory.createEnabledStudy,
                                 "spcspec",
                                  collectionSpecimenSpecToJsonNoId(factory.createCollectionSpecimenSpec))
      }

      "not add a specimen spec on an retired study" in {
        updateOnNonDisabledStudy(factory.createRetiredStudy,
                                 "spcspec",
                                  collectionSpecimenSpecToJsonNoId(factory.createCollectionSpecimenSpec))
      }

      "fail when adding specimen spec and collection event type ID is invalid" in {
        updateOnInvalidCeventType("spcspec",
                                  collectionSpecimenSpecToJsonNoId(factory.createCollectionSpecimenSpec))
      }
  }

  "DELETE /studies/cetypes/spcspec/:id/:ver/:uniqueId" must {

      "remove an specimen spec" in {
        createEntities { (study, cet) =>
          val specimenSpec = factory.createCollectionSpecimenSpec
          collectionEventTypeRepository.put(cet.copy(specimenSpecs = Set(specimenSpec)))

          val json = makeRequest(
              DELETE,
              s"/studies/cetypes/spcspec/${study.id}/${cet.id}/${cet.version}/${specimenSpec.uniqueId}")

          (json \ "status").as[String] must include ("success")

          val jsonId = (json \ "data" \ "id").as[String]
          jsonId must be (cet.id.id)

          collectionEventTypeRepository.getByKey(cet.id) mustSucceed { repoCet =>
            compareObj((json \ "data").as[JsObject], repoCet)

            repoCet must have (
              'studyId     (cet.studyId),
              'version     (cet.version + 1),
              'name        (cet.name),
              'description (cet.description),
              'recurring   (cet.recurring)
            )

            repoCet.annotationTypes must have size cet.annotationTypes.size
            repoCet.specimenSpecs must have size 0

            checkTimeStamps(repoCet, cet.timeAdded, DateTime.now)
          }
        }
      }

      "fail when removing specimen spec and an invalid version" in {
        createEntities { (study, cet) =>
          val specimenSpec = factory.createCollectionSpecimenSpec
          collectionEventTypeRepository.put(cet.copy(specimenSpecs = Set(specimenSpec)))

          val badVersion = cet.version + 1

          val json = makeRequest(
              DELETE,
              s"/studies/cetypes/spcspec/${study.id}/${cet.id}/$badVersion/${specimenSpec.uniqueId}",
              BAD_REQUEST)

          (json \ "status").as[String] must include ("error")

          (json \ "message").as[String] must include ("expected version doesn't match current version")
        }
      }

      "fail when removing specimen spec and study ID does not exist" in {
        val studyId = nameGenerator.next[Study]
        val cetId = nameGenerator.next[CollectionEventType]

        val json = makeRequest(
            DELETE,
            s"/studies/cetypes/spcspec/$studyId/$cetId/0/xyz", NOT_FOUND)

        (json \ "status").as[String] must include ("error")

        (json \ "message").as[String] must include regex ("IdNotFound.*study")
      }

      "fail when removing specimen spec and collection event type ID does not exist" in {
        val study = factory.createDisabledStudy
        studyRepository.put(study)
        val cetId = nameGenerator.next[CollectionEventType]

        val json = makeRequest(
            DELETE,
            s"/studies/cetypes/spcspec/${study.id}/$cetId/0/xyz", NOT_FOUND)

        (json \ "status").as[String] must include ("error")

        (json \ "message").as[String] must include regex ("IdNotFound.*collection event type")
      }

      "fail when removing an specimen spec that does not exist" in {
        createEntities { (study, cet) =>
          val badUniqueId = nameGenerator.next[Study]
          val specimenSpec = factory.createCollectionSpecimenSpec

          collectionEventTypeRepository.put(cet.copy(specimenSpecs = Set(specimenSpec)))

          val json = makeRequest(
              DELETE,
              s"/studies/cetypes/spcspec/${study.id}/${cet.id}/${cet.version}/$badUniqueId",
              NOT_FOUND)

          (json \ "status").as[String] must include ("error")

          (json \ "message").as[String] must startWith ("specimen spec does not exist")
        }
      }

      "fail when removing an specimen spec on a non disabled study" in {
        List(factory.createEnabledStudy, factory.createRetiredStudy).foreach { study =>
          studyRepository.put(study)

          val specimenSpec = factory.createCollectionSpecimenSpec
          val cet = factory.createCollectionEventType.copy(studyId = study.id,
                                                           specimenSpecs = Set(specimenSpec))
          collectionEventTypeRepository.put(cet)

          val json = makeRequest(
              DELETE,
              s"/studies/cetypes/spcspec/${study.id}/${cet.id}/${cet.version}/${specimenSpec.uniqueId}",
              BAD_REQUEST)

          (json \ "status").as[String] must include ("error")

          (json \ "message").as[String] must include regex ("InvalidStatus.*study not disabled")
        }
      }

  }

}
