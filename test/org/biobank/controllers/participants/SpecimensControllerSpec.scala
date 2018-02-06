package org.biobank.controllers.participants

import java.time.OffsetDateTime
import org.biobank.controllers._
import org.biobank.domain.JsonHelper
import org.biobank.domain.participants._
import org.biobank.domain.processing.{ProcessingEventId, ProcessingEventInputSpecimen, ProcessingEventInputSpecimenId }
import org.biobank.dto.SpecimenDto
import org.biobank.fixture.ControllerFixture
import org.scalatest.prop.TableDrivenPropertyChecks._
import play.api.libs.json._
import play.api.test.Helpers._
import scala.language.reflectiveCalls

class SpecimensControllerSpec extends ControllerFixture with JsonHelper with SpecimenSpecFixtures {

  import org.biobank.TestUtils._

  def uri(): String = "/api/participants/cevents/spcs"

  def uri(collectionEvent: CollectionEvent): String =
    uri + s"/${collectionEvent.id.id}"

  def uriSlug(collectionEvent: CollectionEvent): String =
    uri + s"/${collectionEvent.slug}"

  def uri(specimen: Specimen): String =
    uri + s"/get/${specimen.slug}"

  def uri(cevent: CollectionEvent, specimen: Specimen, version: Long): String =
    uri(cevent) + s"/${specimen.id.id}/$version"

  override def createEntities() = {
    val f = super.createEntities
    centreRepository.put(f.centre)
    studyRepository.put(f.study)
    collectionEventTypeRepository.put(f.ceventType)
    participantRepository.put(f.participant)
    collectionEventRepository.put(f.cevent)
    f
  }

  override def createEntitiesAndSpecimens() = {
    val f = super.createEntitiesAndSpecimens
    storeSpecimens(f.cevent, f.specimens)
    f
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
            "inventoryId"           -> specimen.inventoryId,
            "specimenDescriptionId" -> specimen.specimenDescriptionId,
            "timeCreated"           -> specimen.timeCreated,
            "locationId"            -> specimen.locationId.id,
            "amount"                -> specimen.amount)
        }
      )
  }

  def compareObjs(jsonList: List[JsObject], specimens: List[SpecimenDto]) = {
    val specimensMap = specimens.map { specimen => (specimen.id, specimen) }.toMap
    jsonList.foreach { jsonObj =>
      val jsonId = (jsonObj \ "id").as[String]
      compareObj(jsonObj, specimensMap(jsonId))
    }
  }

  describe("Specimens REST API") {

    describe("GET /api/participants/cevents/spcs/get/:slug") {

      it("return a specimen") {
        val f = createEntitiesAndSpecimens
        val specimen = f.specimens.head
        val specimenDto = f.specimenDtos.head
        val json = makeRequest(GET, uri(specimen))

        (json \ "status").as[String] must include ("success")

        compareObj((json \ "data").get, specimenDto)
      }

      it("fails for an invalid specimen ID") {
        val f = createEntitiesAndSpecimens
        val specimen = f.specimens.head.copy(slug = nameGenerator.next[Specimen])

        val json = makeRequest(GET, uri(specimen), NOT_FOUND)

        (json \ "status").as[String] must include ("error")

        (json \ "message").as[String] must include regex("EntityCriteriaNotFound: specimen slug")
      }

    }

    describe("GET /api/participants/cevents/spcs/:ceventId") {

      it("list none") {
        val e = createEntities
        PagedResultsSpec(this).emptyResults(uriSlug(e.cevent))
      }

      it("lists all specimens for a collection event") {
        val e = createEntitiesAndSpecimens

        val jsonItems = PagedResultsSpec(this).multipleItemsResult(
            uri       = uriSlug(e.cevent),
            offset    = 0,
            total     = e.specimens.size.toLong,
            maybeNext = None,
            maybePrev = None)
        jsonItems must have size e.specimens.size.toLong

        compareObjs(jsonItems, e.specimenDtos)
      }

      it("list specimens sorted by id") {
        val e = createEntities
        val specimens = List("id1", "id2").map { id =>
            factory.createUsableSpecimen.copy(id = SpecimenId(id))
          }

        val specimenDtos = specimensToDtos(specimens,
                                           e.cevent,
                                           e.ceventType,
                                           e.specimenDescription,
                                           e.centreLocationInfo,
                                           e.centreLocationInfo)

        storeSpecimens(e.cevent, specimens)

        val sortExprs = Table("sort expressions", "inventoryId", "-inventoryId")
        forAll(sortExprs) { sortExpr =>
          val jsonItems = PagedResultsSpec(this).multipleItemsResult(
              uri         = uriSlug(e.cevent),
              queryParams = Map("sort" -> sortExpr),
              offset      = 0,
              total       = specimens.size.toLong,
              maybeNext   = None,
              maybePrev   = None)

          jsonItems must have size specimens.size.toLong
          if (sortExpr == sortExprs(0)) {
            compareObj(jsonItems(0), specimenDtos(0))
            compareObj(jsonItems(1), specimenDtos(1))
          } else {
            compareObj(jsonItems(0), specimenDtos(1))
            compareObj(jsonItems(1), specimenDtos(0))
          }
        }
      }

      it("list specimens sorted by time created") {
        val e = createEntities
        val specimens = List(1, 2).map { hour =>
            factory.createUsableSpecimen.copy(timeCreated = OffsetDateTime.now.withHour(hour))
          }

        storeSpecimens(e.cevent, specimens)

        val specimenDtos = specimensToDtos(specimens,
                                           e.cevent,
                                           e.ceventType,
                                           e.specimenDescription,
                                           e.centreLocationInfo,
                                           e.centreLocationInfo)

        val sortExprs = Table("sort expressions", "timeCreated", "-timeCreated")
        forAll(sortExprs) { sortExpr =>
          val jsonItems = PagedResultsSpec(this).multipleItemsResult(
              uri         = uriSlug(e.cevent),
              queryParams = Map("sort" -> sortExpr),
              offset      = 0,
              total       = specimens.size.toLong,
              maybeNext   = None,
              maybePrev   = None)

          jsonItems must have size specimens.size.toLong
          if (sortExpr == sortExprs(0)) {
            compareObj(jsonItems(0), specimenDtos(0))
            compareObj(jsonItems(1), specimenDtos(1))
          } else {
            compareObj(jsonItems(0), specimenDtos(1))
            compareObj(jsonItems(1), specimenDtos(0))
          }
        }
      }

      it("list specimens sorted by state") {
        val e = createEntities
        val specimens: List[Specimen] = List(factory.createUsableSpecimen,
                                             factory.createUnusableSpecimen)

        storeSpecimens(e.cevent, specimens)

        val specimenDtos = specimensToDtos(specimens,
                                           e.cevent,
                                           e.ceventType,
                                           e.specimenDescription,
                                           e.centreLocationInfo,
                                           e.centreLocationInfo)

        val sortExprs = Table("sort expressions", "state", "-state")
        forAll(sortExprs) { sortExpr =>
          val jsonItems = PagedResultsSpec(this).multipleItemsResult(
              uri         = uriSlug(e.cevent),
              queryParams = Map("sort" -> sortExpr),
              offset      = 0,
              total       = specimens.size.toLong,
              maybeNext   = None,
              maybePrev   = None)

          jsonItems must have size specimens.size.toLong
          if (sortExpr == sortExprs(0)) {
            compareObj(jsonItems(0), specimenDtos(1))
            compareObj(jsonItems(1), specimenDtos(0))
          } else {
            compareObj(jsonItems(0), specimenDtos(0))
            compareObj(jsonItems(1), specimenDtos(1))
          }
        }
      }

      it("list the first specimen in a paged query") {
        val e = createEntities
        val specimens = List("id1", "id2").map { id =>
            factory.createUsableSpecimen.copy(id = SpecimenId(id))
          }

        storeSpecimens(e.cevent, specimens)

        val specimenDto = specimens(0).createDto(e.cevent,
                                                 e.ceventType.name,
                                                 e.specimenDescription,
                                                 e.centreLocationInfo,
                                                 e.centreLocationInfo)

        val jsonItem = PagedResultsSpec(this).singleItemResult(
            uri         = uriSlug(e.cevent),
            queryParams = Map("sort" -> "inventoryId", "limit" -> "1"),
            offset      = 0,
            total       = specimens.size.toLong,
            maybeNext   = Some(2))

        compareObj(jsonItem, specimenDto)
      }

      it("list the last specimen in a paged query") {
        val e = createEntities
        val specimens = List("id1", "id2").map { id =>
            factory.createUsableSpecimen.copy(id = SpecimenId(id))
          }

        storeSpecimens(e.cevent, specimens)

        val specimenDto = specimens(1).createDto(e.cevent,
                                                 e.ceventType.name,
                                                 e.specimenDescription,
                                                 e.centreLocationInfo,
                                                 e.centreLocationInfo)

        val jsonItem = PagedResultsSpec(this).singleItemResult(
            uri         = uriSlug(e.cevent),
            queryParams = Map("sort" -> "inventoryId", "page" -> "2", "limit" -> "1"),
            offset      = 1,
            total       = specimens.size.toLong,
            maybeNext   = None,
            maybePrev   = Some(1))

        compareObj(jsonItem, specimenDto)
      }

      it("fail when using an invalid query parameters") {
        val e = createEntities
        val url = uriSlug(e.cevent)

        PagedResultsSpec(this).failWithNegativePageNumber(url)
        PagedResultsSpec(this).failWithInvalidPageNumber(url)
        PagedResultsSpec(this).failWithNegativePageSize(url)
        PagedResultsSpec(this).failWithInvalidPageSize(url, 100);
        PagedResultsSpec(this).failWithInvalidSort(url)
      }

      it("fail for invalid collection event id") {
        val cevent = factory.createCollectionEvent
        val json = makeRequest(GET, uriSlug(cevent), NOT_FOUND)

        (json \ "status").as[String] must include ("error")

        (json \ "message").as[String] must include regex ("EntityCriteriaNotFound: collection event slug")
      }

    }

    describe("POST /api/participants/cevents/spcs/:ceventId") {

      it("add a specimen to a collection event") {
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
            'inventoryId           (specimen.inventoryId),
            'specimenDescriptionId (specimen.specimenDescriptionId),
            'version               (specimen.version),
            'originLocationId      (specimen.originLocationId),
            'locationId            (specimen.locationId),
            'containerId           (specimen.containerId),
            'positionId            (specimen.positionId),
            'amount                (specimen.amount)
          )

          checkTimeStamps(repoSpecimen.timeCreated, specimen.timeCreated, TimeCoparisonSeconds)
          checkTimeStamps(repoSpecimen, OffsetDateTime.now, None)
        }
      }

      it("add more than one specimen to a collection event") {
        val e = createEntities
        val specimens = (1 to 2).map(_ => factory.createUsableSpecimen).toList

        val json = makeRequest(POST, uri(e.cevent), specimensToAddJson(specimens))

        (json \ "status").as[String] must include ("success")

        val jsonObj = (json \ "data").as[JsObject]

        compareObj(jsonObj, e.cevent)

        val repoSpecimens = ceventSpecimenRepository.withCeventId(e.cevent.id)
        repoSpecimens must have size specimens.size.toLong
      }
    }

    describe("DELETE  /participants/cevents/spcs/:ceventId/:spcId/:ver") {

      it("remove a specimen from a collection event") {
        val e = createEntities
        val specimen = factory.createUsableSpecimen
        specimenRepository.put(specimen)
        ceventSpecimenRepository.put(CeventSpecimen(e.cevent.id, specimen.id))

        val json = makeRequest(DELETE, uri(e.cevent, specimen, specimen.version))

        (json \ "status").as[String] must include ("success")
      }

      it("not remove a specimen which has been processed") {
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
