package org.biobank.controllers.participants

import com.github.nscala_time.time.Imports._
import org.biobank.controllers._
import org.biobank.domain.participants._
import org.biobank.domain.study._
import org.biobank.domain.JsonHelper
import org.biobank.fixture.ControllerFixture
import play.api.libs.json._
import play.api.test.Helpers._

class SpecimensControllerSpec extends ControllerFixture with JsonHelper {

  import org.biobank.TestUtils._
  import org.biobank.infrastructure.JsonUtils._

  def uri(): String = "/participants/cevents/spcs"

  def uri(collectionEvent: CollectionEvent): String =
    uri + s"/${collectionEvent.id}"

  def createEntities(): (EnabledStudy, Participant, CollectionEventType, CollectionEvent) = {
    var study = factory.createEnabledStudy
    studyRepository.put(study)

    val specimenSpec = factory.createCollectionSpecimenSpec

    val ceventType = factory.createCollectionEventType.copy(studyId = study.id,
                                                            specimenSpecs = Set(specimenSpec),
                                                            annotationTypes = Set.empty)
    collectionEventTypeRepository.put(ceventType)

    val participant = factory.createParticipant.copy(studyId = study.id)
    participantRepository.put(participant)

    val cevent = factory.createCollectionEvent
    collectionEventRepository.put(cevent)

    (study, participant, ceventType, cevent)
  }

  def createEntities(fn: (EnabledStudy, Participant, CollectionEventType, CollectionEvent) => Unit): Unit = {
    val (study, participant, ceventType, cevent) = createEntities
    fn(study, participant, ceventType, cevent)
    ()
  }

  def storeSpecimens(cevent: CollectionEvent, specimens: List[Specimen]) = {
    specimens.foreach { specimen =>
      specimenRepository.put(specimen)
      ceventSpecimenRepository.put(CeventSpecimen(cevent.id, specimen.id))
    }
  }

  def createEntitiesAndSpecimens(fn: (EnabledStudy,
                                      Participant,
                                      CollectionEventType,
                                      CollectionEvent,
                                      List[Specimen]) => Unit): Unit = {
    val (study, participant, ceventType, cevent) = createEntities

    val specimens = (1 to 2).map { _ => factory.createUsableSpecimen }.toList
    storeSpecimens(cevent, specimens)
    fn(study, participant, ceventType, cevent, specimens)
    ()
  }

  def specimensToAddJson(specimens: List[Specimen]) = {
    Json.obj(
      "specimenData" ->
        specimens.map { specimen =>
          Json.obj(
            "inventoryId"    -> specimen.inventoryId,
            "specimenSpecId" -> specimen.specimenSpecId,
            "timeCreated"    -> specimen.timeCreated,
            "locationId"     -> specimen.locationId,
            "amount"         -> specimen.amount)
        }
      )
  }

  def compareObjs(jsonList: List[JsObject], specimens: List[Specimen]) = {
    val specimensMap = specimens.map { specimen => (specimen.id, specimen) }.toMap
    jsonList.foreach { jsonObj =>
      val jsonId = SpecimenId((jsonObj \ "id").as[String])
      compareObj(jsonObj, specimensMap(jsonId))
    }
  }

  "Specimens REST API" when {

    "GET /participants/cevents/spcs/:ceventId" must {

      "list none" in {
        createEntities { (study, participant, ceventType, cevent) =>
          PagedResultsSpec(this).emptyResults(uri(cevent))
        }
      }

      "lists all specimens for a collection event" in {
        createEntitiesAndSpecimens { (study, participant, ceventType, cevent, specimens) =>

          val jsonItems = PagedResultsSpec(this).multipleItemsResult(
              uri = uri(cevent),
              offset = 0,
              total = specimens.size,
              maybeNext = None,
              maybePrev = None)
          jsonItems must have size specimens.size

          compareObjs(jsonItems, specimens)
        }
      }

      "list specimens sorted by id" in {
        createEntities { (study, participant, ceventType, cevent) =>

          val specimens = List("id1", "id2").map { id =>
               factory.createUsableSpecimen.copy(id = SpecimenId(id))
            }

          storeSpecimens(cevent, specimens)

          List("asc", "desc").foreach{ ordering =>
            val jsonItems = PagedResultsSpec(this).multipleItemsResult(
                uri         = uri(cevent),
                queryParams = Map("sort" -> "inventoryId", "order" -> ordering),
                offset      = 0,
                total       = specimens.size,
                maybeNext   = None,
                maybePrev   = None)

            jsonItems must have size specimens.size
            if (ordering == "asc") {
              compareObj(jsonItems(0), specimens(0))
              compareObj(jsonItems(1), specimens(1))
            } else {
              compareObj(jsonItems(0), specimens(1))
              compareObj(jsonItems(1), specimens(0))
            }
          }
        }
      }

      "list specimens sorted by time created" in {
        createEntities { (study, participant, ceventType, cevent) =>

          val specimens = List(1, 2).map { hour =>
               factory.createUsableSpecimen.copy(timeCreated = DateTime.now.hour(hour))
            }

          storeSpecimens(cevent, specimens)

          List("asc", "desc").foreach{ ordering =>
            val jsonItems = PagedResultsSpec(this).multipleItemsResult(
                uri         = uri(cevent),
                queryParams = Map("sort" -> "timeCreated", "order" -> ordering),
                offset      = 0,
                total       = specimens.size,
                maybeNext   = None,
                maybePrev   = None)

            jsonItems must have size specimens.size
            if (ordering == "asc") {
              compareObj(jsonItems(0), specimens(0))
              compareObj(jsonItems(1), specimens(1))
            } else {
              compareObj(jsonItems(0), specimens(1))
              compareObj(jsonItems(1), specimens(0))
            }
          }
        }
      }

      "list specimens sorted by status" in {
        createEntities { (study, participant, ceventType, cevent) =>

          val specimens: List[Specimen] = List(factory.createUsableSpecimen,
                                               factory.createUnusableSpecimen)

          storeSpecimens(cevent, specimens)

          List("asc", "desc").foreach{ ordering =>
            val jsonItems = PagedResultsSpec(this).multipleItemsResult(
                uri         = uri(cevent),
                queryParams = Map("sort" -> "timeCreated", "order" -> ordering),
                offset      = 0,
                total       = specimens.size,
                maybeNext   = None,
                maybePrev   = None)

            jsonItems must have size specimens.size
            if (ordering == "asc") {
              compareObj(jsonItems(0), specimens(0))
              compareObj(jsonItems(1), specimens(1))
            } else {
              compareObj(jsonItems(0), specimens(1))
              compareObj(jsonItems(1), specimens(0))
            }
          }
        }
      }

      "list the first specimen in a paged query" in {
        createEntities { (study, participant, ceventType, cevent) =>

          val specimens = List("id1", "id2").map { id =>
               factory.createUsableSpecimen.copy(id = SpecimenId(id))
            }

          storeSpecimens(cevent, specimens)

          List("asc", "desc").foreach{ ordering =>
            val jsonItem = PagedResultsSpec(this).singleItemResult(
                uri         = uri(cevent),
                queryParams = Map("sort" -> "inventoryId", "pageSize" -> "1"),
                offset      = 0,
                total       = specimens.size,
                maybeNext   = Some(2))

            compareObj(jsonItem, specimens(0))
          }
        }
      }

      "list the last specimen in a paged query" in {
        createEntities { (study, participant, ceventType, cevent) =>

          val specimens = List("id1", "id2").map { id =>
               factory.createUsableSpecimen.copy(id = SpecimenId(id))
            }

          storeSpecimens(cevent, specimens)

          List("asc", "desc").foreach{ ordering =>
            val jsonItem = PagedResultsSpec(this).singleItemResult(
                uri         = uri(cevent),
                queryParams = Map("sort" -> "inventoryId", "page" -> "2", "pageSize" -> "1"),
                offset      = 1,
                total       = specimens.size,
                maybeNext   = None,
                maybePrev   = Some(1))

            compareObj(jsonItem, specimens(1))
          }
        }
      }

      "fail when using an invalid query parameters" in {
        createEntities { (study, participant, ceventType, cevent) =>
          val url = uri(cevent)

          PagedResultsSpec(this).failWithNegativePageNumber(url)
          PagedResultsSpec(this).failWithInvalidPageNumber(url)
          PagedResultsSpec(this).failWithNegativePageSize(url)
          PagedResultsSpec(this).failWithInvalidPageSize(url, 100);
          PagedResultsSpec(this).failWithInvalidSort(url)
        }
      }

      "fail for invalid collection event id" in {
        val cevent = factory.createCollectionEvent
        val json = makeRequest(GET, uri(cevent), BAD_REQUEST)

        (json \ "status").as[String] must include ("error")

        (json \ "message").as[String] must include regex ("IdNotFound.*collection event id")
      }

    }

    "POST /participants/cevents/spcs/:ceventId" must {

      "add a specimen to a collection event" in {
        createEntities { (study, participant, ceventType, cevent) =>
          val specimen = factory.createUsableSpecimen
          val json = makeRequest(POST, uri(cevent), specimensToAddJson(List(specimen)))

          (json \ "status").as[String] must include ("success")

          val jsonObj = (json \ "data").as[JsObject]

          compareObj(jsonObj, cevent)

          val repoSpecimens = ceventSpecimenRepository.withCeventId(cevent.id)
          repoSpecimens must have size 1
          repoSpecimens.head.ceventId must be (cevent.id)

          specimenRepository.getByKey(repoSpecimens.head.specimenId) mustSucceed { repoSpecimen =>
            log.info(s"specimen: ${repoSpecimens.head}")

            repoSpecimen must have (
              'inventoryId       (specimen.inventoryId),
              'specimenSpecId    (specimen.specimenSpecId),
              'version           (specimen.version),
              'originLocationId  (specimen.originLocationId),
              'locationId        (specimen.locationId),
              'containerId       (specimen.containerId),
              'positionId        (specimen.positionId),
              'amount            (specimen.amount)
            )

            (repoSpecimen.timeCreated to specimen.timeCreated).millis must be < TimeCoparisonMillis
            checkTimeStamps(repoSpecimen, DateTime.now, None)
          }
        }
      }

      "add more than one specimen to a collection event" in {
        createEntities { (study, participant, ceventType, cevent) =>

          val specimens = (1 to 2).map { _ => factory.createUsableSpecimen }.toList

          val json = makeRequest(POST, uri(cevent), specimensToAddJson(specimens))

          (json \ "status").as[String] must include ("success")

          val jsonObj = (json \ "data").as[JsObject]

          compareObj(jsonObj, cevent)

          val repoSpecimens = ceventSpecimenRepository.withCeventId(cevent.id)
          repoSpecimens must have size specimens.size
        }
      }
    }

  }

}
