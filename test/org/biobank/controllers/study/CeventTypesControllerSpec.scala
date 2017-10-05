package org.biobank.controllers.study

import java.time.OffsetDateTime
import org.biobank.controllers.PagedResultsSpec
import org.biobank.domain.AnnotationType
import org.biobank.domain.JsonHelper
import org.biobank.domain.study._
import org.biobank.domain.study.{ CollectionEventType, Study }
import org.biobank.fixture.ControllerFixture
import org.biobank.fixture._
import org.scalatest.prop.TableDrivenPropertyChecks._
import play.api.libs.json._
import play.api.test.Helpers._

class CeventTypesControllerSpec extends ControllerFixture with JsonHelper {
  import org.biobank.TestUtils._

  class MultipleCeTypes(numCeTypes: Int) {
    val study = factory.createDisabledStudy
    val ceTypes = 1.to(numCeTypes).map { index =>
        factory.createCollectionEventType.copy(
          specimenDescriptions = Set(factory.createCollectionSpecimenDescription),
          annotationTypes      = Set(factory.createAnnotationType))
      }

    addToRepository(study)
    ceTypes.foreach(addToRepository)
  }

  private def uri(): String = "/api/studies/cetypes"

  private def uri(study: Study): String = uri + s"/${study.id.id}"

  private def uri(study: Study, ceventType: CollectionEventType): String =
    uri(study) + s"/${ceventType.id.id}"

  private def uri(ceventType: CollectionEventType, path: String): String =
    uri + s"/$path/${ceventType.id.id}"

  private def uri(study: Study, ceventType: CollectionEventType, version: Long): String =
    uri(study, ceventType) + s"/$version"

  private def createEntities(fn: (Study, CollectionEventType) => Unit): Unit = {
    val disabledStudy = factory.createDisabledStudy
    val cet = factory.createCollectionEventType
    Set(disabledStudy, cet).foreach(addToRepository)
    fn(disabledStudy, cet)
  }

  private def urlName(cet: CollectionEventType) = uri(cet, "name")
  private def urlDescription(cet: CollectionEventType) = uri(cet, "description")
  private def urlRecurring(cet: CollectionEventType) = uri(cet, "recurring")
  private def urlAddSepecimenDescription(cet: CollectionEventType) = uri(cet, "spcdesc")

  private def urlAddAnnotationType(cet: CollectionEventType) = uri(cet, "annottype")

  private def urlUpdateAnnotationType(annotType: AnnotationType) =
      (cet: CollectionEventType) => urlAddAnnotationType(cet) + s"/${annotType.id}"

  private def urlUpdateSpecimenDescription(sd: SpecimenDescription) =
      (cet: CollectionEventType) => urlAddSepecimenDescription(cet) + s"/${sd.id}"

  private def cetToAddCmd(cet: CollectionEventType) = {
    Json.obj("studyId"              -> cet.studyId.id,
             "name"                 -> cet.name,
             "description"          -> cet.description,
             "recurring"            -> cet.recurring,
             "specimenDescriptions" -> cet.specimenDescriptions,
             "annotationTypes"      -> cet.annotationTypes)
  }

  def addOnNonDisabledStudy(study: Study) {
    studyRepository.put(study)

    val cet = factory.createCollectionEventType.copy(
        studyId         = study.id,
        specimenDescriptions   = Set(factory.createCollectionSpecimenDescription),
        annotationTypes = Set(factory.createAnnotationType))

    val json = makeRequest(POST, uri(study), BAD_REQUEST, cetToAddCmd(cet))

    (json \ "status").as[String] must include ("error")

    (json \ "message").as[String] must include regex("InvalidStatus: study not disabled")

    ()
  }

  private def updateWithInvalidVersion(jsonField: JsObject,
                                       urlFunc:   CollectionEventType => String) {
    createEntities { (study, cet) =>
      val reqJson = jsonField ++ Json.obj("id"              -> cet.id.id,
                                          "studyId"         -> study.id,
                                          "expectedVersion" -> (cet.version + 1))

      val json = makeRequest(POST, urlFunc(cet), BAD_REQUEST, reqJson)

      (json \ "status").as[String] must include ("error")

      (json \ "message").as[String] must include regex (".*expected version doesn't match current version.*")

      ()
    }
  }

  private def updateOnInvalidCeventType(jsonField: JsObject,
                                        urlFunc: CollectionEventType => String) {
    val study = factory.createDisabledStudy
    studyRepository.put(study)
    val cet = factory.createCollectionEventType
    val reqJson = jsonField ++ Json.obj("id"              -> cet.id.id,
                                        "studyId"         -> study.id,
                                        "expectedVersion" -> cet.version)

    val json = makeRequest(POST, urlFunc(cet), NOT_FOUND, reqJson)

    (json \ "status").as[String] must include ("error")

    (json \ "message").as[String] must include regex ("IdNotFound.*collection event type")

    ()
  }

  private def updateOnNonDisabledStudy(study: Study,
                                       jsonField: JsObject,
                                       urlFunc:   CollectionEventType => String) {
    study must not be an [DisabledStudy]
    studyRepository.put(study)

    val cet = factory.createCollectionEventType.copy(
        studyId              = study.id,
        specimenDescriptions = Set(factory.createCollectionSpecimenDescription),
        annotationTypes      = Set(factory.createAnnotationType))
    collectionEventTypeRepository.put(cet)

    val reqJson = jsonField ++ Json.obj("studyId"         -> study.id.id,
                                        "id"              -> cet.id.id,
                                        "expectedVersion" -> cet.version)

    val json = makeRequest(POST, urlFunc(cet), BAD_REQUEST, reqJson)

    (json \ "status").as[String] must include ("error")

    (json \ "message").as[String] must include regex ("InvalidStatus: study not disabled")

    ()
  }

  private def removeOnNonDisabledStudy(study: Study) {
    studyRepository.put(study)

    val cet = factory.createCollectionEventType.copy(
        studyId         = study.id,
        specimenDescriptions   = Set(factory.createCollectionSpecimenDescription),
        annotationTypes = Set(factory.createAnnotationType))
    collectionEventTypeRepository.put(cet)

    val json = makeRequest(DELETE, uri(study, cet, cet.version), BAD_REQUEST)

    (json \ "status").as[String] must include ("error")

    (json \ "message").as[String] must include regex ("InvalidStatus: study not disabled")

    ()
  }

  describe("Collection Event Type REST API") {

    describe("GET /api/studies/cetypes/:studyId/:ceventId") {

      it("get a single collection event type") {
        createEntities { (study, cet) =>
          val json = makeRequest(GET, uri(study, cet))

          (json \ "status").as[String] must include ("success")
          val jsonObj = (json \ "data").as[JsObject]
          compareObj(jsonObj, cet)
        }
      }

      it("fail for an invalid study ID") {
        val study = factory.createDisabledStudy
        val cet = factory.createCollectionEventType

        val json = makeRequest(GET, uri(study, cet), NOT_FOUND)

        (json \ "status").as[String] must include ("error")

        (json \ "message").as[String] must include regex("IdNotFound.*study")
      }

      it("fail for an invalid collection event type id") {
        val study = factory.createDisabledStudy
        studyRepository.put(study)

        val cet = factory.createCollectionEventType

        val json = makeRequest(GET, uri(study, cet), NOT_FOUND)

        (json \ "status").as[String] must include ("error")

        (json \ "message").as[String] must include regex("IdNotFound.*collection event type")
      }

    }

    describe("GET /api/studies/cetypes") {

      it("list none") {
        val study = factory.createDisabledStudy
        studyRepository.put(study)
        PagedResultsSpec(this).emptyResults(uri(study))
      }

      it("list a single collection event type") {
        val f = new MultipleCeTypes(1)
        val jsonItems = PagedResultsSpec(this).multipleItemsResult(
            uri = uri(f.study),
            offset = 0,
            total = 1,
            maybeNext = None,
            maybePrev = None)
        jsonItems must have size 1
        compareObj(jsonItems(0), f.ceTypes(0))
      }

      it("get all collection event types for a study") {
        val f = new MultipleCeTypes(3)
        val jsonItems = PagedResultsSpec(this).multipleItemsResult(
            uri       = uri(f.study),
            offset    = 0,
            total     = f.ceTypes.size.toLong,
            maybeNext = None,
            maybePrev = None)
        jsonItems must have size f.ceTypes.size.toLong

        jsonItems.foreach { jsonCet =>
          val jsonId = (jsonCet \ "id").as[String]
          val cet = f.ceTypes.find { x => x.id.id == jsonId }.value
          compareObj(jsonCet, cet)
        }
      }

      it("list collection event types sorted by name") {
        val f = new MultipleCeTypes(3)
        val sortExprs = Table("sort expressions", "name", "-name")
        forAll(sortExprs) { sortExpr =>
          val jsonItems = PagedResultsSpec(this).multipleItemsResult(
              uri         = uri(f.study),
              queryParams = Map("sort" -> sortExpr),
              offset      = 0,
              total       = f.ceTypes.size.toLong,
              maybeNext   = None,
              maybePrev   = None)
          jsonItems must have size f.ceTypes.size.toLong
          if (sortExpr == sortExprs(0)) {
            compareObj(jsonItems(0), f.ceTypes(0))
            compareObj(jsonItems(1), f.ceTypes(1))
            compareObj(jsonItems(2), f.ceTypes(2))
          } else {
            compareObj(jsonItems(0), f.ceTypes(2))
            compareObj(jsonItems(1), f.ceTypes(1))
            compareObj(jsonItems(2), f.ceTypes(0))
          }
        }
      }

      it("list the first Collection Event Type in a paged query") {
        val f = new MultipleCeTypes(3)
        val jsonItem = PagedResultsSpec(this).singleItemResult(
            uri         = uri(f.study),
            queryParams = Map("filter" -> s"name::${f.ceTypes(0).name}"),
            total       = 1)

        compareObj(jsonItem, f.ceTypes(0))
      }

      it("list the last Collection Event Type in a paged query") {
        val f = new MultipleCeTypes(3)
        val jsonItem = PagedResultsSpec(this).singleItemResult(
            uri         = uri(f.study),
            queryParams = Map("filter" -> s"name::${f.ceTypes(2).name}"),
            total       = 1)

        compareObj(jsonItem, f.ceTypes(2))
      }

      it("fail for invalid study id") {
        val study = factory.createDisabledStudy
        val json = makeRequest(GET, uri(study), NOT_FOUND)

        (json \ "status").as[String] must include ("error")

        (json \ "message").as[String] must include regex("IdNotFound.*study")
      }

      it("fail when using an invalid query parameters") {
        val f = new MultipleCeTypes(3)
        val url = uri(f.study)

        PagedResultsSpec(this).failWithNegativePageNumber(url)
        PagedResultsSpec(this).failWithInvalidPageNumber(url)
        PagedResultsSpec(this).failWithNegativePageSize(url)
        PagedResultsSpec(this).failWithInvalidPageSize(url, 100);
        PagedResultsSpec(this).failWithInvalidSort(url)
      }

    }

    describe("POST /api/studies/cetypes/:studyId") {

      it("add a collection event type") {
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

          repoCet.specimenDescriptions must have size cet.specimenDescriptions.size.toLong
          repoCet.annotationTypes must have size cet.annotationTypes.size.toLong
          checkTimeStamps(repoCet, cet.timeAdded, None)
        }
      }

      it("allow adding a collection event type with same name on two different studies") {
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

            repoCet.specimenDescriptions must have size cet.specimenDescriptions.size.toLong
            repoCet.annotationTypes must have size cet.annotationTypes.size.toLong
            checkTimeStamps(repoCet, cet.timeAdded, None)
          }
        }
      }

      it("not add a collection event type to an enabled study") {
        addOnNonDisabledStudy(factory.createEnabledStudy)
      }

      it("not add a collection event type to an retired study") {
        addOnNonDisabledStudy(factory.createRetiredStudy)
      }

      it("fail when adding and study IDs is invalid") {
        val study = factory.createDisabledStudy
        val cet = factory.createCollectionEventType

        val json = makeRequest(POST, uri(study), NOT_FOUND, cetToAddCmd(cet))

        (json \ "status").as[String] must include ("error")

        (json \ "message").as[String] must include regex ("IdNotFound.*study")
      }

      it("fail when adding a collection event type with a duplicate name to the same study") {
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

    describe("DELETE /api/studies/cetypes/:studyId/:id/:ver") {

      it("remove a collection event type") {
        createEntities { (study, cet) =>
          val json = makeRequest(DELETE, uri(study, cet, cet.version))

          (json \ "status").as[String] must include ("success")

          collectionEventTypeRepository.getByKey(cet.id) mustFail "IdNotFound.*collection event type.*"
        }
      }

      it("not remove a collection event type on an enabled study") {
        removeOnNonDisabledStudy(factory.createEnabledStudy)
      }

      it("not remove a collection event type on an retired study") {
        removeOnNonDisabledStudy(factory.createRetiredStudy)
      }

      ignore("not remove a collection event type that is in use") {
        fail("write this test")
      }

    }

    describe("POST /api/studies/cetypes/name/:id") {

      it("update a collection event type's name") {
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

            repoCet.specimenDescriptions must have size cet.specimenDescriptions.size.toLong
            repoCet.annotationTypes must have size cet.annotationTypes.size.toLong
            checkTimeStamps(repoCet, cet.timeAdded, OffsetDateTime.now)
          }
        }
      }

      it("allow updating to the same name on collection event types of two different studies") {
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

            repoCet.specimenDescriptions must have size cet.specimenDescriptions.size.toLong
            repoCet.annotationTypes must have size cet.annotationTypes.size.toLong
            checkTimeStamps(repoCet, cet.timeAdded, OffsetDateTime.now)
          }
        }
      }

      it("fail when updating name to one already used by another collection event type in the same study") {
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

      it("not update a collection event type's name on an enabled study") {
        updateOnNonDisabledStudy(factory.createEnabledStudy,
                                 Json.obj("name" -> nameGenerator.next[CollectionEventType]),
                                 urlName)
      }

      it("not update a collection event type's name on an retired study") {
        updateOnNonDisabledStudy(factory.createRetiredStudy,
                                 Json.obj("name" -> nameGenerator.next[CollectionEventType]),
                                 urlName)
      }

      it("fail when updating name and collection event type ID is invalid") {
        updateOnInvalidCeventType(Json.obj("name" -> nameGenerator.next[CollectionEventType]), urlName)
      }

      it("fail when updating name with an invalid version") {
        updateWithInvalidVersion(Json.obj("name" -> nameGenerator.next[CollectionEventType]),
                                 urlName)
      }
    }

    describe("POST /api/studies/cetypes/description/:id") {

      it("update a collection event type's name") {
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

            repoCet.specimenDescriptions must have size cet.specimenDescriptions.size.toLong
            repoCet.annotationTypes must have size cet.annotationTypes.size.toLong
            checkTimeStamps(repoCet, cet.timeAdded, OffsetDateTime.now)
          }
        }
      }

      it("not update a collection event type's description on an enabled study") {
        updateOnNonDisabledStudy(factory.createEnabledStudy,
                                 Json.obj("description" -> nameGenerator.next[CollectionEventType]),
                                 urlDescription)
      }

      it("not update a collection event type's description on an retired study") {
        updateOnNonDisabledStudy(factory.createRetiredStudy,
                                 Json.obj("description" -> nameGenerator.next[CollectionEventType]),
                                 urlDescription)
      }

      it("fail when updating description and collection event type ID is invalid") {
        updateOnInvalidCeventType(Json.obj("description" -> nameGenerator.next[CollectionEventType]),
                                  urlDescription)

      }

      it("fail when updating description with an invalid version") {
        updateWithInvalidVersion(Json.obj("description" -> nameGenerator.next[CollectionEventType]),
                                 urlDescription)
      }
    }

    describe("POST /api/studies/cetypes/recurring/:id") {

      it("update a collection event type's name") {
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

              repoCet.specimenDescriptions must have size cet.specimenDescriptions.size.toLong
              repoCet.annotationTypes must have size cet.annotationTypes.size.toLong
              checkTimeStamps(repoCet, cet.timeAdded, OffsetDateTime.now)
            }
          }
        }
      }

      it("not update a collection event type's recurring on an enabled study") {
        updateOnNonDisabledStudy(factory.createEnabledStudy,
                                 Json.obj("recurring" -> false),
                                 urlRecurring)
      }

      it("not update a collection event type's recurring on an retired study") {
        updateOnNonDisabledStudy(factory.createRetiredStudy,
                                 Json.obj("recurring" -> false),
                                 urlRecurring)
      }

      it("fail when updating recurring and collection event type ID is invalid") {
        updateOnInvalidCeventType(Json.obj("recurring" -> false), urlRecurring)
      }

      it("fail when updating recurring with an invalid version") {
        updateWithInvalidVersion(Json.obj("recurring" -> false), urlRecurring)
      }
    }

    describe("POST /api/studies/cetypes/annottypes/:id") {

      it("add an annotation type") {
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

            repoCet.specimenDescriptions must have size cet.specimenDescriptions.size.toLong
            repoCet.annotationTypes must have size 1

            repoCet.annotationTypes.head.id.id must not be empty
            repoCet.annotationTypes.head must have (
              'name          (annotType.name),
              'description   (annotType.description),
              'valueType     (annotType.valueType),
              'maxValueCount (annotType.maxValueCount),
              'options       (annotType.options),
              'required      (annotType.required)
            )

            checkTimeStamps(repoCet, cet.timeAdded, OffsetDateTime.now)
          }
        }
      }

      it("fail when adding annotation type and collection event type ID does not exist") {
        updateOnInvalidCeventType(annotationTypeToJsonNoId(factory.createAnnotationType),
                                  urlAddAnnotationType)
      }

      it("fail when adding annotation type and an invalid version") {
        updateWithInvalidVersion(annotationTypeToJsonNoId(factory.createAnnotationType),
                                 urlAddAnnotationType)
      }

      it("not add an annotation type on an enabled study") {
        updateOnNonDisabledStudy(factory.createEnabledStudy,
                                 annotationTypeToJsonNoId(factory.createAnnotationType),
                                 urlAddAnnotationType)
      }

      it("not add an annotation type on an retired study") {
        updateOnNonDisabledStudy(factory.createRetiredStudy,
                                 annotationTypeToJsonNoId(factory.createAnnotationType),
                                 urlAddAnnotationType)
      }

      it("fail when adding annotation type and collection event type ID is invalid") {
        def url(cet: CollectionEventType) = uri(cet, "annottype")
        updateOnInvalidCeventType(annotationTypeToJsonNoId(factory.createAnnotationType),
                                  url)
      }
    }

    describe("POST /api/studies/cetypes/annottype/:cetId/:annotationTypeId") {

      it("update an annotation type") {
        createEntities { (study, cet) =>
          val annotationType = factory.createAnnotationType
          collectionEventTypeRepository.put(cet.copy(annotationTypes = Set(annotationType)))


          val updatedAnnotationType =
            annotationType.copy(description = Some(nameGenerator.next[CollectionEventType]))

          val reqJson = Json.obj("id"              -> cet.id.id,
                                 "studyId"         -> cet.studyId.id,
                                 "expectedVersion" -> Some(cet.version)) ++
          annotationTypeToJson(updatedAnnotationType)

          val json = makeRequest(POST, uri(cet, "annottype") + s"/${annotationType.id}", reqJson)

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

            repoCet.specimenDescriptions must have size cet.specimenDescriptions.size.toLong
            repoCet.annotationTypes must have size 1

            repoCet.annotationTypes.head.id.id must not be empty
            repoCet.annotationTypes.head must have (
              'name          (updatedAnnotationType.name),
              'description   (updatedAnnotationType.description),
              'valueType     (updatedAnnotationType.valueType),
              'maxValueCount (updatedAnnotationType.maxValueCount),
              'options       (updatedAnnotationType.options),
              'required      (updatedAnnotationType.required)
            )

            checkTimeStamps(repoCet, cet.timeAdded, OffsetDateTime.now)
          }
        }
      }

      it("fail when updating annotation type and collection event type ID does not exist") {
        val annotationType = factory.createAnnotationType
        updateOnInvalidCeventType(annotationTypeToJson(annotationType),
                                  urlUpdateAnnotationType(annotationType))
      }

      it("fail when updating annotation type and an invalid version") {
        val annotationType = factory.createAnnotationType
        updateWithInvalidVersion(annotationTypeToJson(annotationType),
                                 urlUpdateAnnotationType(annotationType))
      }

      it("not add an annotation type on an enabled study") {
        val annotationType = factory.createAnnotationType
        updateOnNonDisabledStudy(factory.createEnabledStudy,
                                 annotationTypeToJson(annotationType),
                                 urlUpdateAnnotationType(annotationType))
      }

      it("not add an annotation type on an retired study") {
        val annotationType = factory.createAnnotationType
        updateOnNonDisabledStudy(factory.createRetiredStudy,
                                 annotationTypeToJson(annotationType),
                                 urlUpdateAnnotationType(annotationType))
      }

      it("fail when adding annotation type and collection event type ID is invalid") {
        val annotationType = factory.createAnnotationType
        updateOnInvalidCeventType(annotationTypeToJsonNoId(factory.createAnnotationType),
                                 urlUpdateAnnotationType(annotationType))
      }
    }

    describe("DELETE /api/studies/cetypes/annottype/:id/:ver/:uniqueId") {

      it("remove an annotation type") {
        createEntities { (study, cet) =>
          val annotationType = factory.createAnnotationType
          collectionEventTypeRepository.put(cet.copy(annotationTypes = Set(annotationType)))

          val json = makeRequest(
              DELETE,
              uri + s"/annottype/${study.id}/${cet.id}/${cet.version}/${annotationType.id}")

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

            repoCet.specimenDescriptions must have size cet.specimenDescriptions.size.toLong
            repoCet.annotationTypes must have size 0

            checkTimeStamps(repoCet, cet.timeAdded, OffsetDateTime.now)
          }
        }
      }

      it("fail when removing annotation type and an invalid version") {
        createEntities { (study, cet) =>
          val annotationType = factory.createAnnotationType
          collectionEventTypeRepository.put(cet.copy(annotationTypes = Set(annotationType)))

          val badVersion = cet.version + 1

          val json = makeRequest(
              DELETE,
              uri + s"/annottype/${study.id}/${cet.id}/$badVersion/${annotationType.id}",
              BAD_REQUEST)

          (json \ "status").as[String] must include ("error")

          (json \ "message").as[String] must include ("expected version doesn't match current version")

          ()
        }
      }

      it("fail when removing annotation type and study ID does not exist") {
        val studyId = nameGenerator.next[Study]
        val cetId = nameGenerator.next[CollectionEventType]

        val json = makeRequest(
            DELETE,
            uri + s"/annottype/$studyId/$cetId/0/xyz", NOT_FOUND)

        (json \ "status").as[String] must include ("error")

        (json \ "message").as[String] must include regex ("IdNotFound.*study")
      }

      it("fail when removing annotation type and collection event type ID does not exist") {
        val study = factory.createDisabledStudy
        studyRepository.put(study)
        val cetId = nameGenerator.next[CollectionEventType]

        val json = makeRequest(
            DELETE,
            uri + s"/annottype/${study.id}/$cetId/0/xyz", NOT_FOUND)

        (json \ "status").as[String] must include ("error")

        (json \ "message").as[String] must include regex ("IdNotFound.*collection event type")
      }

      it("fail when removing an annotation type that does not exist") {
        createEntities { (study, cet) =>
          val badUniqueId = nameGenerator.next[Study]
          val annotationType = factory.createAnnotationType

          collectionEventTypeRepository.put(cet.copy(annotationTypes = Set(annotationType)))

          val json = makeRequest(
              DELETE,
              uri + s"/annottype/${study.id}/${cet.id}/${cet.version}/$badUniqueId",
              NOT_FOUND)

          (json \ "status").as[String] must include ("error")

          (json \ "message").as[String] must startWith ("annotation type does not exist")

          ()
        }
      }

      it("fail when removing an annotation type on a non disabled study") {
        List(factory.createEnabledStudy, factory.createRetiredStudy).foreach { study =>
          studyRepository.put(study)

          val annotationType = factory.createAnnotationType
          val cet = factory.createCollectionEventType.copy(studyId = study.id,
                                                           annotationTypes = Set(annotationType))
          collectionEventTypeRepository.put(cet)

          val json = makeRequest(
              DELETE,
              uri + s"/annottype/${study.id}/${cet.id}/${cet.version}/${annotationType.id}",
              BAD_REQUEST)

          (json \ "status").as[String] must include ("error")

          (json \ "message").as[String] must include regex ("InvalidStatus: study not disabled")
        }
      }

    }

    describe("POST /api/studies/cetypes/spcdesc/:id") {

      it("add a specimen spec") {
        createEntities { (study, cet) =>
          val spec = factory.createCollectionSpecimenDescription

          val reqJson = Json.obj("id"              -> cet.id.id,
                                 "studyId"         -> cet.studyId.id,
                                 "expectedVersion" -> Some(cet.version)) ++
          collectionSpecimenDescriptionToJsonNoId(spec)

          val json = makeRequest(POST, uri(cet, "spcdesc"), reqJson)

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

            repoCet.annotationTypes must have size cet.annotationTypes.size.toLong
            repoCet.specimenDescriptions must have size 1

            repoCet.specimenDescriptions.head.id.id must not be empty
            repoCet.specimenDescriptions.head must have (
              'name                        (spec.name),
              'description                 (spec.description),
              'units                       (spec.units),
              'anatomicalSourceType        (spec.anatomicalSourceType),
              'preservationType            (spec.preservationType),
              'preservationTemperatureType (spec.preservationTemperatureType),
              'specimenType                (spec.specimenType)
            )

            checkTimeStamps(repoCet, cet.timeAdded, OffsetDateTime.now)
          }
        }
      }

      it("fail when adding specimen spec and collection event type ID does not exist") {
        updateOnInvalidCeventType(
          collectionSpecimenDescriptionToJsonNoId(factory.createCollectionSpecimenDescription),
          urlAddSepecimenDescription)
      }

      it("fail when adding specimen spec and an invalid version") {
        updateWithInvalidVersion(
          collectionSpecimenDescriptionToJsonNoId(factory.createCollectionSpecimenDescription),
          urlAddSepecimenDescription)
      }

      it("not add an specimen spec on an enabled study") {
        updateOnNonDisabledStudy(
          factory.createEnabledStudy,
          collectionSpecimenDescriptionToJsonNoId(factory.createCollectionSpecimenDescription),
          urlAddSepecimenDescription)
      }

      it("not add a specimen spec on an retired study") {
        updateOnNonDisabledStudy(
          factory.createRetiredStudy,
          collectionSpecimenDescriptionToJsonNoId(factory.createCollectionSpecimenDescription),
          urlAddSepecimenDescription)
      }

      it("fail when adding specimen spec and collection event type ID is invalid") {
        updateOnInvalidCeventType(
          collectionSpecimenDescriptionToJsonNoId(factory.createCollectionSpecimenDescription),
          urlAddSepecimenDescription)
      }
    }

    describe("POST /api/studies/cetypes/spcdesc/:cetId/:specimenDescriptionId") {

      it("update an specimen description") {
        createEntities { (study, cet) =>
          val specimenDescription = factory.createCollectionSpecimenDescription
          collectionEventTypeRepository.put(cet.copy(specimenDescriptions = Set(specimenDescription)))


          val updatedSpecimenDescription =
            specimenDescription.copy(name        = nameGenerator.next[CollectionEventType],
                                description = Some(nameGenerator.next[CollectionEventType]))

          val reqJson = Json.obj("id"              -> cet.id.id,
                                 "studyId"         -> cet.studyId.id,
                                 "expectedVersion" -> Some(cet.version)) ++
          collectionSpecimenDescriptionToJsonNoId(updatedSpecimenDescription)

          val json = makeRequest(POST, uri(cet, "spcdesc") + s"/${specimenDescription.id}", reqJson)

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

            repoCet.annotationTypes must have size cet.annotationTypes.size.toLong
            repoCet.specimenDescriptions must have size 1

            repoCet.specimenDescriptions.head.id.id must not be empty
            repoCet.specimenDescriptions.head must have (
              'name                        (updatedSpecimenDescription.name),
              'description                 (updatedSpecimenDescription.description),
              'units                       (updatedSpecimenDescription.units),
              'anatomicalSourceType        (updatedSpecimenDescription.anatomicalSourceType),
              'preservationType            (updatedSpecimenDescription.preservationType),
              'preservationTemperatureType (updatedSpecimenDescription.preservationTemperatureType),
              'specimenType                (updatedSpecimenDescription.specimenType)
            )
            checkTimeStamps(repoCet, cet.timeAdded, OffsetDateTime.now)
          }
        }
      }

      it("fail when updating specimen description and collection event type ID does not exist") {
        val specimenDescription = factory.createCollectionSpecimenDescription
        updateOnInvalidCeventType(collectionSpecimenDescriptionToJson(specimenDescription),
                                  urlUpdateSpecimenDescription(specimenDescription))
      }

      it("fail when updating specimen description and an invalid version") {
        val specimenDescription = factory.createCollectionSpecimenDescription
        updateWithInvalidVersion(collectionSpecimenDescriptionToJson(specimenDescription),
                                 urlUpdateSpecimenDescription(specimenDescription))
      }

      it("not add an specimen description on an enabled study") {
        val specimenDescription = factory.createCollectionSpecimenDescription
        updateOnNonDisabledStudy(factory.createEnabledStudy,
                                 collectionSpecimenDescriptionToJson(specimenDescription),
                                 urlUpdateSpecimenDescription(specimenDescription))
      }

      it("not add an specimen description on an retired study") {
        val specimenDescription = factory.createCollectionSpecimenDescription
        updateOnNonDisabledStudy(factory.createRetiredStudy,
                                 collectionSpecimenDescriptionToJson(specimenDescription),
                                 urlUpdateSpecimenDescription(specimenDescription))
      }

      it("fail when adding specimen description and collection event type ID is invalid") {
        val specimenDescription = factory.createCollectionSpecimenDescription
        updateOnInvalidCeventType(collectionSpecimenDescriptionToJsonNoId(factory.createCollectionSpecimenDescription),
                                 urlUpdateSpecimenDescription(specimenDescription))
      }
    }

    describe("DELETE /api/studies/cetypes/spcdesc/:id/:ver/:uniqueId") {

      it("remove an specimen spec") {
        createEntities { (study, cet) =>
          val specimenDescription = factory.createCollectionSpecimenDescription
          collectionEventTypeRepository.put(cet.copy(specimenDescriptions = Set(specimenDescription)))

          val json = makeRequest(
              DELETE,
              uri + s"/spcdesc/${study.id}/${cet.id}/${cet.version}/${specimenDescription.id.id}")

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

            repoCet.annotationTypes must have size cet.annotationTypes.size.toLong
            repoCet.specimenDescriptions must have size 0

            checkTimeStamps(repoCet, cet.timeAdded, OffsetDateTime.now)
          }
        }
      }

      it("fail when removing specimen spec and an invalid version") {
        createEntities { (study, cet) =>
          val specimenDescription = factory.createCollectionSpecimenDescription
          collectionEventTypeRepository.put(cet.copy(specimenDescriptions = Set(specimenDescription)))

          val badVersion = cet.version + 1

          val json = makeRequest(
              DELETE,
              uri + s"/spcdesc/${study.id}/${cet.id}/$badVersion/${specimenDescription.id}",
              BAD_REQUEST)

          (json \ "status").as[String] must include ("error")

          (json \ "message").as[String] must include ("expected version doesn't match current version")

          ()
        }
      }

      it("fail when removing specimen spec and study ID does not exist") {
        val studyId = nameGenerator.next[Study]
        val cetId = nameGenerator.next[CollectionEventType]

        val json = makeRequest(
            DELETE,
            uri + s"/spcdesc/$studyId/$cetId/0/xyz", NOT_FOUND)

        (json \ "status").as[String] must include ("error")

        (json \ "message").as[String] must include regex ("IdNotFound.*study")
      }

      it("fail when removing specimen spec and collection event type ID does not exist") {
        val study = factory.createDisabledStudy
        studyRepository.put(study)
        val cetId = nameGenerator.next[CollectionEventType]

        val json = makeRequest(
            DELETE,
            uri + s"/spcdesc/${study.id}/$cetId/0/xyz", NOT_FOUND)

        (json \ "status").as[String] must include ("error")

        (json \ "message").as[String] must include regex ("IdNotFound.*collection event type")
      }

      it("fail when removing an specimen spec that does not exist") {
        createEntities { (study, cet) =>
          val badUniqueId = nameGenerator.next[Study]
          val specimenDescription = factory.createCollectionSpecimenDescription

          collectionEventTypeRepository.put(cet.copy(specimenDescriptions = Set(specimenDescription)))

          val json = makeRequest(
              DELETE,
              uri + s"/spcdesc/${study.id}/${cet.id}/${cet.version}/$badUniqueId",
              NOT_FOUND)

          (json \ "status").as[String] must include ("error")

          (json \ "message").as[String] must startWith ("specimen spec does not exist")

          ()
        }
      }

      it("fail when removing an specimen spec on a non disabled study") {
        List(factory.createEnabledStudy, factory.createRetiredStudy).foreach { study =>
          studyRepository.put(study)

          val specimenDescription = factory.createCollectionSpecimenDescription
          val cet = factory.createCollectionEventType.copy(studyId = study.id,
                                                           specimenDescriptions = Set(specimenDescription))
          collectionEventTypeRepository.put(cet)

          val json = makeRequest(
              DELETE,
              uri + s"/spcdesc/${study.id}/${cet.id}/${cet.version}/${specimenDescription.id}",
              BAD_REQUEST)

          (json \ "status").as[String] must include ("error")

          (json \ "message").as[String] must include regex ("InvalidStatus: study not disabled")
        }
      }

    }

  }

}
