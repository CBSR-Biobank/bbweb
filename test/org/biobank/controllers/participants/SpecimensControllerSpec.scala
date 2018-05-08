package org.biobank.controllers.participants

import java.time.OffsetDateTime
import org.biobank.fixtures.Url
import org.biobank.controllers._
import org.biobank.domain.Slug
import org.biobank.domain.participants._
import org.biobank.domain.processing.{ProcessingEventId, ProcessingEventInputSpecimen, ProcessingEventInputSpecimenId }
import org.biobank.dto.SpecimenDto
import org.biobank.fixtures.ControllerFixture
import org.biobank.matchers.PagedResultsMatchers
import org.scalatest.matchers.{MatchResult, Matcher}
import play.api.mvc._
import play.api.libs.json._
import play.api.test.Helpers._
import scala.language.reflectiveCalls
import scala.concurrent.Future

class SpecimensControllerSpec
    extends ControllerFixture
    with PagedResultsMatchers
    with PagedResultsSharedSpec
    with SpecimenSpecFixtures {

  import org.biobank.matchers.EntityMatchers._
  import org.biobank.matchers.DtoMatchers._
  import org.biobank.matchers.JsonMatchers._

  private def uri(): String = "/api/participants/cevents/spcs"

  private def uri(collectionEvent: CollectionEvent): String =
    uri + s"/${collectionEvent.id.id}"

  private def uriSlug(collectionEvent: CollectionEvent): String =
    uri + s"/${collectionEvent.slug}"

  private def uri(specimen: Specimen): String =
    uri + s"/get/${specimen.slug}"

  private def uri(cevent: CollectionEvent, specimen: Specimen, version: Long): String =
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

  private def storeSpecimens(cevent: CollectionEvent, specimens: List[Specimen]) = {
    specimens.foreach { specimen =>
      specimenRepository.put(specimen)
      ceventSpecimenRepository.put(CeventSpecimen(cevent.id, specimen.id))
    }
  }

  private def specimensToAddJson(specimens: List[Specimen]) = {
    Json.obj("specimenData" -> specimens.map { specimen =>
               Json.obj(
                 "inventoryId"          -> specimen.inventoryId,
                 "specimenDefinitionId" -> specimen.specimenDefinitionId,
                 "timeCreated"          -> specimen.timeCreated,
                 "locationId"           -> specimen.locationId.id,
                 "amount"               -> specimen.amount)
             }
    )
  }

  describe("Specimens REST API") {

    describe("GET /api/participants/cevents/spcs/get/:slug") {

      it("return a specimen") {
        val f = createEntitiesAndSpecimens
        val specimen = f.specimens.head
        val reply = makeAuthRequest(GET, uri(specimen)).value
        reply must beOkResponseWithJsonReply

        val dto = (contentAsJson(reply) \ "data").validate[SpecimenDto]
        dto must be (jsSuccess)
        dto.get must matchDtoToSpecimen(specimen)
      }

      it("fails for an invalid specimen ID") {
        val f = createEntitiesAndSpecimens
        val specimen = f.specimens.head.copy(slug = Slug(nameGenerator.next[Specimen]))

        val reply = makeAuthRequest(GET, uri(specimen)).value
        reply must beNotFoundWithMessage("EntityCriteriaNotFound: specimen slug")
      }

    }

    describe("GET /api/participants/cevents/spcs/:ceventId") {

      it("list none") {
        val f = createEntities
        new Url(uriSlug(f.cevent)) must beEmptyResults
      }

      describe("lists all specimens for a collection event") {
        listMultipleSpecimens() { () =>
          val e = createEntitiesAndSpecimens
          (new Url(uriSlug(e.cevent)), e.specimens.sortBy(_.inventoryId))
        }
      }

      describe("list specimens sorted by inventory id") {

        def commonSetup = {
          val e = createEntities
          val specimens = List("id1", "id2").map { id =>
              factory.createUsableSpecimen.copy(id = SpecimenId(id))
            }
          storeSpecimens(e.cevent, specimens)
          (e.cevent, specimens)
        }

        describe("sorted in ascending order") {
          listMultipleSpecimens() { () =>
            val (cevent, specimens) = commonSetup
            (new Url(uriSlug(cevent) + "?sort=inventoryId"), specimens.sortBy(_.inventoryId))
          }
        }

        describe("sorted in descending order") {
          listMultipleSpecimens() { () =>
            val (cevent, specimens) = commonSetup
            (new Url(uriSlug(cevent) + "?sort=-inventoryId"), specimens.sortBy(_.inventoryId).reverse)
          }
        }

      }

      describe("list specimens sorted by time created") {

        def commonSetup = {
          val e = createEntities
          val specimens = List(1, 2).map { hour =>
              factory.createUsableSpecimen.copy(timeCreated = OffsetDateTime.now.withHour(hour))
            }
          storeSpecimens(e.cevent, specimens)
          (e.cevent, specimens)
        }

        describe("sorted in ascending order") {
          listMultipleSpecimens() { () =>
            val (cevent, specimens) = commonSetup
            (new Url(uriSlug(cevent) + "?sort=timeCreated"), specimens.sortBy(_.timeCreated))
          }
        }

        describe("sorted in descending order") {
          listMultipleSpecimens() { () =>
            val (cevent, specimens) = commonSetup
            (new Url(uriSlug(cevent) + "?sort=-timeCreated"), specimens.sortBy(_.timeCreated).reverse)
          }
        }

      }


      describe("list specimens sorted by state") {

        def commonSetup = {
          val e = createEntities
          val specimens = List(factory.createUsableSpecimen, factory.createUnusableSpecimen)
          storeSpecimens(e.cevent, specimens)
          (e.cevent, specimens)
        }

        describe("sorted in ascending order") {
          listMultipleSpecimens() { () =>
            val (cevent, specimens) = commonSetup
            (new Url(uriSlug(cevent) + "?sort=state"), specimens.sortBy(_.state.id))
          }
        }

        describe("sorted in descending order") {
          listMultipleSpecimens() { () =>
            val (cevent, specimens) = commonSetup
            (new Url(uriSlug(cevent) + "?sort=-state"), specimens.sortBy(_.state.id).reverse)
          }
        }

      }

      describe("list the first specimen in a paged query") {
        listSingleSpecimen(maybeNext = Some(2)) { () =>
          val e = createEntities
          val specimens = List("id1", "id2").map { id =>
              factory.createUsableSpecimen.copy(id = SpecimenId(id))
            }
          storeSpecimens(e.cevent, specimens)
          (new Url(uriSlug(e.cevent) + "?sort=inventoryId&limit=1"), specimens(0))
        }
      }

      describe("list the last specimen in a paged query") {
        listSingleSpecimen(offset = 1, maybePrev = Some(1)) { () =>
          val e = createEntities
          val specimens = List("id1", "id2").map { id =>
              factory.createUsableSpecimen.copy(id = SpecimenId(id))
            }
          storeSpecimens(e.cevent, specimens)
          (new Url(uriSlug(e.cevent) + "?sort=inventoryId&page=2&limit=1"), specimens(1))
        }
      }

      describe("fail when using an invalid query parameters") {
        pagedQueryShouldFailSharedBehaviour { () =>
          val e = createEntities
          new Url(uriSlug(e.cevent))
        }
      }


      it("fail for invalid collection event id") {
        val cevent = factory.createCollectionEvent
        val reply = makeAuthRequest(GET, uriSlug(cevent)).value
        reply must beNotFoundWithMessage("EntityCriteriaNotFound: collection event slug")
      }

    }

    describe("POST /api/participants/cevents/spcs/:ceventId") {

      it("add a specimen to a collection event") {
        val e = createEntities
        val specimen = factory.createUsableSpecimen
        val reply = makeAuthRequest(POST, uri(e.cevent), specimensToAddJson(List(specimen))).value
        reply must beOkResponseWithJsonReply

        val replyCevent = (contentAsJson(reply) \ "data").validate[CollectionEvent]
        replyCevent must be (jsSuccess)
        replyCevent.get must matchCollectionEvent(e.cevent)

        val repoSpecimens = ceventSpecimenRepository.withCeventId(e.cevent.id)
        repoSpecimens must have size 1
        val repoSpecimen = repoSpecimens.head
        repoSpecimen.ceventId must be (e.cevent.id)

        val updatedSpecimen = specimen.copy(id = repoSpecimen.specimenId,
                                            timeAdded = OffsetDateTime.now)
        updatedSpecimen must matchRepositorySpecimen
      }

      it("add more than one specimen to a collection event") {
        val e = createEntities
        val specimens = (1 to 2).map(_ => factory.createUsableSpecimen).toList

        val reply = makeAuthRequest(POST, uri(e.cevent), specimensToAddJson(specimens)).value
        reply must beOkResponseWithJsonReply

        val replyCevent = (contentAsJson(reply) \ "data").validate[CollectionEvent]
        replyCevent must be (jsSuccess)
        replyCevent.get must matchCollectionEvent(e.cevent)

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

        val reply = makeAuthRequest(DELETE, uri(e.cevent, specimen, specimen.version)).value
        reply must beOkResponseWithJsonReply

        val repoSpecimens = ceventSpecimenRepository.withCeventId(e.cevent.id)
        repoSpecimens must have size 0
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

        val reply = makeAuthRequest(DELETE, uri(e.cevent, specimen, specimen.version)).value
        reply must beBadRequestWithMessage("specimen has child specimens.*")
        }
    }

  }

  private def listSingleSpecimen(offset:    Long = 0,
                                 maybeNext: Option[Int] = None,
                                 maybePrev: Option[Int] = None)
                                (setupFunc: () => (Url, Specimen)) = {

    it("list single specimen") {
      val (url, expectedSpecimen) = setupFunc()
      val reply = makeAuthRequest(GET, url.path).value
      reply must beOkResponseWithJsonReply

      val json = contentAsJson(reply)
      json must beSingleItemResults(offset, maybeNext, maybePrev)

      val dto = (json \ "data" \ "items").validate[List[SpecimenDto]]
      dto must be (jsSuccess)
      dto.get.foreach { _ must matchDtoToSpecimen(expectedSpecimen) }
    }
  }

  private def listMultipleSpecimens(offset:    Long = 0,
                                    maybeNext: Option[Int] = None,
                                    maybePrev: Option[Int] = None)
                                   (setupFunc: () => (Url, List[Specimen])) = {

    it("list multiple specimens") {
      val (url, expectedSpecimens) = setupFunc()

      val reply = makeAuthRequest(GET, url.path).value
      reply must beOkResponseWithJsonReply

      val json = contentAsJson(reply)
      json must beMultipleItemResults(offset = offset,
                                      total = expectedSpecimens.size.toLong,
                                      maybeNext = maybeNext,
                                      maybePrev = maybePrev)

      val dtos = (json \ "data" \ "items").validate[List[SpecimenDto]]
      dtos must be (jsSuccess)

      (dtos.get zip expectedSpecimens).foreach { case (dto, specimen) =>
        dto must matchDtoToSpecimen(specimen)
      }
    }

  }

  def matchUpdatedSpecimen(specimen: Specimen) =
    new Matcher[Future[Result]] {
      def apply (left: Future[Result]) = {
        val dto = (contentAsJson(left) \ "data").validate[SpecimenDto]
        val jsSuccessMatcher = jsSuccess(dto)

        if (!jsSuccessMatcher.matches) {
          jsSuccessMatcher
        } else {
          val replyMatcher = matchDtoToSpecimen(specimen)(dto.get)

          if (!replyMatcher.matches) {
            MatchResult(false,
                        s"reply does not match expected: ${replyMatcher.failureMessage}",
                        s"reply matches expected: ${replyMatcher.failureMessage}")
          } else {
            matchDtoToRepositorySpecimen(dto.get)
          }
        }
      }
    }

  def matchDtoToRepositorySpecimen =
    new Matcher[SpecimenDto] {
      def apply (left: SpecimenDto) = {
        specimenRepository.getByKey(SpecimenId(left.id)).fold(
          err => {
            MatchResult(false, s"not found in repository: ${err.head}", "")

          },
          repoCet => {
            val repoMatcher = matchDtoToSpecimen(repoCet)(left)
            MatchResult(repoMatcher.matches,
                        s"repository specimen does not match expected: ${repoMatcher.failureMessage}",
                        s"repository specimen matches expected: ${repoMatcher.failureMessage}")
          }
        )
      }
    }

  def matchRepositorySpecimen =
    new Matcher[Specimen] {
      def apply (left: Specimen) = {
        specimenRepository.getByKey(left.id).fold(
          err => {
            MatchResult(false, s"not found in repository: ${err.head}", "")

          },
          repoCet => {
            val repoMatcher = matchSpecimen(repoCet)(left)
            MatchResult(repoMatcher.matches,
                        s"repository specimen does not match expected: ${repoMatcher.failureMessage}",
                        s"repository specimen matches expected: ${repoMatcher.failureMessage}")
          }
        )
      }
    }
}
