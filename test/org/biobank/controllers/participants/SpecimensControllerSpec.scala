package org.biobank.controllers.participants

import com.github.nscala_time.time.Imports._
import org.biobank.controllers._
import org.biobank.domain.participants._
import org.biobank.domain.JsonHelper
import org.biobank.domain.processing.{
  ProcessingEventId,
  ProcessingEventInputSpecimen,
  ProcessingEventInputSpecimenId
}
import org.biobank.fixture.ControllerFixture
import play.api.libs.json._
import play.api.test.Helpers._
import scala.language.reflectiveCalls

class SpecimensControllerSpec extends ControllerFixture with JsonHelper {

  import org.biobank.TestUtils._
  import org.biobank.infrastructure.JsonUtils._

  def uri(): String = "/participants/cevents/spcs"

  def uri(collectionEvent: CollectionEvent): String =
    uri + s"/${collectionEvent.id}"

  def uri(cevent: CollectionEvent, specimen: Specimen, version: Long): String =
    uri(cevent) + s"/${specimen.id.id}/$version"

  def createEntities() = {
    val _centre = factory.createEnabledCentre.copy(locations = Set(factory.createLocation))
    val _study = factory.createEnabledStudy
    val _specimenSpec = factory.createCollectionSpecimenSpec
    val _ceventType = factory.createCollectionEventType.copy(studyId = _study.id,
                                                             specimenSpecs = Set(_specimenSpec),
                                                             annotationTypes = Set.empty)
    val _participant = factory.createParticipant.copy(studyId = _study.id)
    val _cevent = factory.createCollectionEvent

    centreRepository.put(_centre)
    studyRepository.put(_study)
    collectionEventTypeRepository.put(_ceventType)
    participantRepository.put(_participant)
    collectionEventRepository.put(_cevent)

    new {
      val centre       = _centre
      val study        = _study
      val specimenSpec = _specimenSpec
      val ceventType   = _ceventType
      val participant  = _participant
      val cevent       = _cevent
    }
  }

  def createEntitiesAndSpecimens() = {
    val entities = createEntities

    val _specimens = (1 to 2).map { _ => factory.createUsableSpecimen }.toList
    storeSpecimens(entities.cevent, _specimens)

    new {
      val centre      = entities.centre
      val study       = entities.study
      val participant = entities.participant
      val ceventType  = entities.ceventType
      val cevent      = entities.cevent
      val specimens   = _specimens
    }
  }

  def storeSpecimens(cevent: CollectionEvent, specimens: List[Specimen]) = {
    specimens.foreach { specimen =>
      specimenRepository.put(specimen)
      ceventSpecimenRepository.put(CeventSpecimen(cevent.id, specimen.id))
    }
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
        val e = createEntities
        PagedResultsSpec(this).emptyResults(uri(e.cevent))
      }

      "lists all specimens for a collection event" in {
        val e = createEntitiesAndSpecimens

        val jsonItems = PagedResultsSpec(this).multipleItemsResult(
            uri       = uri(e.cevent),
            offset    = 0,
            total     = e.specimens.size,
            maybeNext = None,
            maybePrev = None)
        jsonItems must have size e.specimens.size

        compareObjs(jsonItems, e.specimens)
      }

      "list specimens sorted by id" in {
        val e = createEntities
        val specimens = List("id1", "id2").map { id =>
            factory.createUsableSpecimen.copy(id = SpecimenId(id))
          }

        storeSpecimens(e.cevent, specimens)

        List("asc", "desc").foreach { ordering =>
          val jsonItems = PagedResultsSpec(this).multipleItemsResult(
              uri         = uri(e.cevent),
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

      "list specimens sorted by time created" in {
        val e = createEntities
        val specimens = List(1, 2).map { hour =>
            factory.createUsableSpecimen.copy(timeCreated = DateTime.now.hour(hour))
          }

        storeSpecimens(e.cevent, specimens)

        List("asc", "desc").foreach { ordering =>
          val jsonItems = PagedResultsSpec(this).multipleItemsResult(
              uri         = uri(e.cevent),
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

      "list specimens sorted by status" in {
        val e = createEntities
        val specimens: List[Specimen] = List(factory.createUsableSpecimen,
                                             factory.createUnusableSpecimen)

        storeSpecimens(e.cevent, specimens)

        List("asc", "desc").foreach{ ordering =>
          val jsonItems = PagedResultsSpec(this).multipleItemsResult(
              uri         = uri(e.cevent),
              queryParams = Map("sort" -> "status", "order" -> ordering),
              offset      = 0,
              total       = specimens.size,
              maybeNext   = None,
              maybePrev   = None)

          jsonItems must have size specimens.size
          if (ordering == "asc") {
            compareObj(jsonItems(0), specimens(1))
            compareObj(jsonItems(1), specimens(0))
          } else {
            compareObj(jsonItems(0), specimens(0))
            compareObj(jsonItems(1), specimens(1))
          }
        }
      }

      "list the first specimen in a paged query" in {
        val e = createEntities
        val specimens = List("id1", "id2").map { id =>
            factory.createUsableSpecimen.copy(id = SpecimenId(id))
          }

        storeSpecimens(e.cevent, specimens)

        val jsonItem = PagedResultsSpec(this).singleItemResult(
            uri         = uri(e.cevent),
            queryParams = Map("sort" -> "inventoryId", "pageSize" -> "1"),
            offset      = 0,
            total       = specimens.size,
            maybeNext   = Some(2))

        compareObj(jsonItem, specimens(0))
      }

      "list the last specimen in a paged query" in {
        val e = createEntities
        val specimens = List("id1", "id2").map { id =>
            factory.createUsableSpecimen.copy(id = SpecimenId(id))
          }

        storeSpecimens(e.cevent, specimens)

        val jsonItem = PagedResultsSpec(this).singleItemResult(
            uri         = uri(e.cevent),
            queryParams = Map("sort" -> "inventoryId", "page" -> "2", "pageSize" -> "1"),
            offset      = 1,
            total       = specimens.size,
            maybeNext   = None,
            maybePrev   = Some(1))

        compareObj(jsonItem, specimens(1))
      }

      "fail when using an invalid query parameters" in {
        val e = createEntities
        val url = uri(e.cevent)

        PagedResultsSpec(this).failWithNegativePageNumber(url)
        PagedResultsSpec(this).failWithInvalidPageNumber(url)
        PagedResultsSpec(this).failWithNegativePageSize(url)
        PagedResultsSpec(this).failWithInvalidPageSize(url, 100);
        PagedResultsSpec(this).failWithInvalidSort(url)
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
        val e = createEntities
        val specimen = factory.createUsableSpecimen
        val json = makeRequest(POST, uri(e.cevent), specimensToAddJson(List(specimen)))

        (json \ "status").as[String] must include ("success")

        val jsonObj = (json \ "data").as[JsObject]

        compareObj(jsonObj, e.cevent)

        val repoSpecimens = ceventSpecimenRepository.withCeventId(e.cevent.id)
        repoSpecimens must have size 1
        repoSpecimens.head.ceventId must be (e.cevent.id)

          specimenRepository.getByKey(repoSpecimens.head.specimenId) mustSucceed { repoSpecimen =>

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

      "add more than one specimen to a collection event" in {
        val e = createEntities
        val specimens = (1 to 2).map(_ => factory.createUsableSpecimen).toList

        val json = makeRequest(POST, uri(e.cevent), specimensToAddJson(specimens))

        (json \ "status").as[String] must include ("success")

        val jsonObj = (json \ "data").as[JsObject]

        compareObj(jsonObj, e.cevent)

        val repoSpecimens = ceventSpecimenRepository.withCeventId(e.cevent.id)
        repoSpecimens must have size specimens.size
      }
    }

    "DELETE  /participants/cevents/spcs/:ceventId/:spcId/:ver" must {

      "remove a specimen from a collection event" in {
        val e = createEntities
        val specimen = factory.createUsableSpecimen
        specimenRepository.put(specimen)
        ceventSpecimenRepository.put(CeventSpecimen(e.cevent.id, specimen.id))

        val json = makeRequest(DELETE, uri(e.cevent, specimen, specimen.version))

        (json \ "status").as[String] must include ("success")
      }

      "not remove a specimen which has been processed" in {
        val e = createEntities
        val specimen = factory.createUsableSpecimen
        specimenRepository.put(specimen)
        ceventSpecimenRepository.put(CeventSpecimen(e.cevent.id, specimen.id))

        val peis = ProcessingEventInputSpecimen(ProcessingEventInputSpecimenId("abc"),
                                                ProcessingEventId("def"),
                                                specimen.id)
        processingEventInputSpecimenRepository.put(peis)

        val json = makeRequest(DELETE, uri(e.cevent, specimen, specimen.version), BAD_REQUEST)

        (json \ "status").as[String] must include ("error")

        (json \ "message").as[String] must include regex("specimen has child specimens.*")
        }
    }

  }

}
