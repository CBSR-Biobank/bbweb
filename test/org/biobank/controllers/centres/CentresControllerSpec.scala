package org.biobank.controllers.centres

import java.time.OffsetDateTime
import org.biobank.controllers.PagedResultsSharedSpec
import org.biobank.domain.{JsonHelper, Location, Slug}
import org.biobank.domain.centres._
import org.biobank.domain.studies.{Study, StudyId}
import org.biobank.dto.{CentreDto, NameAndStateDto}
import org.biobank.fixture.{ControllerFixture, Url}
import org.biobank.matchers.PagedResultsMatchers
import org.biobank.services.centres.{CentreCountsByStatus, CentreLocation, CentreLocationInfo}
import org.scalatest.prop.TableDrivenPropertyChecks._
import play.api.libs.json._
import play.api.test.Helpers._

/**
  * Tests the REST API for [[Centre]]s.
  */
class CentresControllerSpec
    extends ControllerFixture
    with JsonHelper
    with PagedResultsSharedSpec
    with PagedResultsMatchers {

  import org.biobank.TestUtils._
  import org.biobank.matchers.JsonMatchers._
  import org.biobank.matchers.EntityMatchers._

  private def uri(paths: String*): String = {
    val basePath = "/api/centres"
    if (paths.isEmpty) basePath
    else s"$basePath/" + paths.mkString("/")
  }

  def uri(centre: Centre): String = uri(centre.id.id)

  def uri(centre: Centre, path: String): String = uri(path, centre.id.id)

  def centreLocationToJson(centre: Centre, location: Location): JsObject = {
    Json.obj(
      "expectedVersion" -> centre.version,
      "name"            -> location.name,
      "street"          -> location.street,
      "city"            -> location.city,
      "province"        -> location.province,
      "postalCode"      -> location.postalCode,
      "poBoxNumber"     -> location.poBoxNumber,
      "countryIsoCode"  -> location.countryIsoCode)
  }

  def centreLocationToUpdateJson(centre: Centre, location: Location): JsObject = {
    centreLocationToJson(centre, location) ++ Json.obj("locationId" -> location.id.id)
  }

  def compareObjs(jsonList: List[JsObject], centres: List[Centre]) = {
    val centresMap = centres.map { centre => (centre.id, centre) }.toMap
    jsonList.foreach { jsonObj =>
      val jsonId = CentreId((jsonObj \ "id").as[String])
      compareObj(jsonObj, centresMap(jsonId))
    }
  }

  describe("Centre REST API") {

    describe("GET /api/centres/:slug") {

      it("retrieve a centre") {
        val centre = factory.createDisabledCentre
        centreRepository.put(centre)
        val reply = makeAuthRequest(GET, uri(centre.slug.id)).value
        reply must beOkResponseWithJsonReply

        val json = contentAsJson(reply)
        val centreDto = (json \ "data").validate[CentreDto]
        centreDto must be (jsSuccess)
        centreDto.get must matchCentre(centre)
      }

      it("not read an invalid centre") {
        val centre = factory.createDisabledCentre
        val reply = makeAuthRequest(GET, uri(centre)).value
        reply must beNotFoundWithMessage("EntityCriteriaNotFound: centre slug")
      }
    }

    describe("GET /api/centres/search") {

      it("list none") {
        val url = new Url(uri("search"))
        url must beEmptyResults
      }

      describe("list a centre") {

        listSingleCentre() { () =>
          val centre = factory.createDisabledCentre
          centreRepository.put(centre)

          (new Url(uri("search")), centre)
        }
      }

      describe("list multiple centres") {

        listMultipleCentres() { () =>
          val centres = List(factory.createDisabledCentre, factory.createDisabledCentre)
          centres.foreach(centreRepository.put)

          (new Url(uri("search")), centres.sortWith(_.name < _.name))
        }

      }

      describe("list a single centre when filtered by name") {

        listSingleCentre() { () =>
          val centres = List(factory.createDisabledCentre, factory.createEnabledCentre)
          val centre = centres(0)
          centres.foreach(centreRepository.put)

          (new Url(uri("search") + s"?filter=name::${centre.name}"), centre)
        }

      }

      describe("list a single disabled centre when filtered by state") {

        listSingleCentre() { () =>
          val centres = List(factory.createDisabledCentre,
                             factory.createEnabledCentre,
                             factory.createEnabledCentre)
          centres.foreach(centreRepository.put)
          (new Url(uri("search") + s"?filter=state::disabled"), centres(0))
        }
      }

      describe("list centres when filtered by state") {

        def commonSetup = {
          val centres = List(factory.createDisabledCentre,
                             factory.createDisabledCentre,
                             factory.createEnabledCentre,
                             factory.createEnabledCentre)
          centres.foreach(centreRepository.put)
          centres
        }

        describe("for disabled") {

          listMultipleCentres() { () =>
            val centres = commonSetup
            (new Url(uri("search") + s"?filter=state::disabled"),
             centres.filter { c => c.state == Centre.disabledState  }.sortWith(_.name < _.name))
          }

        }

        describe("for enabled") {

          listMultipleCentres() { () =>
            val centres = commonSetup
            (new Url(uri("search") + s"?filter=state::enabled"),
             centres.filter { c => c.state == Centre.enabledState  }.sortWith(_.name < _.name))
          }

        }

        it("fail ith an invalid state name") {
          val invalidStateName = "state::" + nameGenerator.next[Study]
          val reply = makeAuthRequest(GET, uri("search") + s"?filter=$invalidStateName").value
          reply must beNotFoundWithMessage ("InvalidState: entity state does not exist")
        }

      }

      describe("list centres sorted by name") {

        def commonSetup = {
          val centres = List(factory.createDisabledCentre.copy(name = "CTR3"),
                             factory.createDisabledCentre.copy(name = "CTR2"),
                             factory.createEnabledCentre.copy(name = "CTR1"),
                             factory.createEnabledCentre.copy(name = "CTR0"))
          centres.foreach(centreRepository.put)
          centres
        }

        describe("in ascending order") {

          listMultipleCentres() { () =>
            val centres = commonSetup
            (new Url(uri("search") + s"?sort=name"), centres.sortWith(_.name < _.name))
          }

        }

        describe("in decending order") {

          listMultipleCentres() { () =>
            val centres = commonSetup
            (new Url(uri("search") + s"?sort=-name"), centres.sortWith(_.name > _.name))
          }
        }

      }

      describe("list centres sorted by state") {

        def commonSetup = {
          val centres = List(factory.createEnabledCentre, factory.createDisabledCentre)
          centres.foreach(centreRepository.put)
          centres
        }

        describe("in ascending order") {

          listMultipleCentres() { () =>
            val centres = commonSetup
            (new Url(uri("search") + s"?sort=state"), centres.sortWith(_.state.id < _.state.id))
          }

        }

        describe("in decending order") {

          listMultipleCentres() { () =>
            val centres = commonSetup
            (new Url(uri("search") + s"?sort=-state"), centres.sortWith(_.state.id > _.state.id))
          }
        }

        it("fail on attempt to list centres filtered by an invalid state name") {
          val invalidStateName = "state::" + nameGenerator.next[Study]
          val reply = makeAuthRequest(GET, uri("search") + s"?filter=$invalidStateName")
          reply.value must beNotFoundWithMessage("InvalidState: entity state does not exist")
        }

      }

      describe("list a single centre when using paged query") {

        def commonSetup = {
          val centres = List(factory.createDisabledCentre.copy(name = "CTR3"),
                             factory.createDisabledCentre.copy(name = "CTR2"),
                             factory.createEnabledCentre.copy(name = "CTR1"),
                             factory.createEnabledCentre.copy(name = "CTR0"))
          centres.foreach(centreRepository.put)
          centres
        }

        describe("fist page") {

          listSingleCentre(maybeNext = Some(2)) { () =>
            val centres = commonSetup
            (new Url(uri("search") + s"?sort=name&limit=1"), centres(3))
          }

        }

        describe("last page") {

          listSingleCentre(offset = 3, maybePrev = Some(3)) { () =>
            val centres = commonSetup
            (new Url(uri("search") + s"?sort=name&page=4&limit=1"), centres(0))
          }

        }

      }

      describe("fail when using an invalid query parameters") {

        pagedQueryShouldFailSharedBehaviour(uri("search"))

      }

    }

    describe("GET /api/centres/counts") {

      it("return empty counts") {
        val reply = makeAuthRequest(GET, uri("counts")).value
        reply must beOkResponseWithJsonReply
        val counts = (contentAsJson(reply) \ "data").validate[CentreCountsByStatus]
        counts must be (jsSuccess)
        counts.get must equal (CentreCountsByStatus(0, 0, 0))
      }

      it("return valid counts") {
        val centres = List(factory.createDisabledCentre,
                           factory.createDisabledCentre,
                           factory.createEnabledCentre)
        centres.foreach(centreRepository.put)
        val reply = makeAuthRequest(GET, uri("counts")).value
        reply must beOkResponseWithJsonReply
        val counts = (contentAsJson(reply) \ "data").validate[CentreCountsByStatus]
        counts must be (jsSuccess)
        counts.get must equal (CentreCountsByStatus(3, 2, 1))
      }

    }

    describe("POST /api/centres") {

      it("add a centre") {
        val centre = factory.createDisabledCentre
        val addJson = Json.obj("name" -> centre.name, "description" -> centre.description)
        val reply = makeAuthRequest(POST, uri(""), addJson).value
        reply must beOkResponseWithJsonReply

        val json = contentAsJson(reply)
        val replyCentre = (json \ "data").validate[CentreDto]
        replyCentre must be (jsSuccess)

        val newCentreId = CentreId(replyCentre.get.id)
        val updatedCentre = centre.copy(id = newCentreId)

        centreRepository.getByKey(newCentreId) mustSucceed { repoCentre =>
          repoCentre must equal (updatedCentre)
        }
      }

      it("add a centre with no description") {
        val addJson = Json.obj("name" -> nameGenerator.next[String])
        val json = makeRequest(POST, uri(""), addJson)

        (json \ "status").as[String] must include ("success")
      }

      it("not add a centre with a name that is too short") {
        val reply = makeAuthRequest(POST, uri(""), Json.obj("name" -> "A")).value
        reply must beBadRequestWithMessage("InvalidName")
      }

      it("fail when adding a centre with a duplicate name") {
        val centre = factory.createDisabledCentre
        centreRepository.put(centre)

        val reply = makeAuthRequest(POST, uri(""), Json.obj("name" -> centre.name)).value
        reply must beForbiddenRequestWithMessage("EntityCriteriaError: centre with name already exists")
      }
    }

    describe("POST /api/centres/name/:id") {

      it("update a centre's name") {
        val newName = nameGenerator.next[Centre]
        val centre = factory.createDisabledCentre
        centreRepository.put(centre)

        val updateJson = Json.obj("expectedVersion" -> Some(centre.version),
                                  "name"            -> newName)
        val reply = makeAuthRequest(POST, uri(centre, "name"), updateJson).value
        reply must beOkResponseWithJsonReply

        val json = contentAsJson(reply)
        val replyCentre = (json \ "data").validate[CentreDto]
        replyCentre must be (jsSuccess)

        val updatedCentre = centre.copy(version      = centre.version + 1,
                                        name         = newName,
                                        slug         = Slug(newName),
                                        timeModified = Some(OffsetDateTime.now))
        replyCentre.get must matchCentre(updatedCentre)
        centreRepository.getByKey(centre.id) mustSucceed { repoCentre =>
          repoCentre must equal (updatedCentre)
        }
      }

      it("not update a centre with a duplicate name") {
        val centres = (1 to 2).map { _ =>
            val centre = factory.createDisabledCentre
            centreRepository.put(centre)
            centre
          }

        val duplicateName = centres(0).name
        val updateJson = Json.obj("expectedVersion" -> Some(centres(1).version),
                                  "name"            -> duplicateName)
        val reply = makeAuthRequest(POST, uri(centres(1), "name"), updateJson).value
        reply must beForbiddenRequestWithMessage("EntityCriteriaError: centre with name already exists")
      }

      describe("not update name on an invalid centre") {
        invalidCentreIdSharedBehaviour("name", Json.obj("name" -> nameGenerator.next[Study]))
      }

      describe("fail when updating name with invalid version") {
        updateWithInvalidVersionSharedBehaviour("name", Json.obj("name" -> nameGenerator.next[Study]))
      }

      describe("fail when updating name on an enabled centre") {
        updateEnabledCentreSharedBehaviour { centre =>
          (uri(centre, "name"), Json.obj("name" -> nameGenerator.next[Centre]))
        }
      }

    }

    describe("POST /api/centres/description/:id") {

      it("update a centre's description") {
        val newDescription = nameGenerator.next[Centre]
        val centre = factory.createDisabledCentre
        centreRepository.put(centre)

        val updateJson = Json.obj("expectedVersion" -> Some(centre.version),
                                  "description"     -> newDescription)
        val reply = makeAuthRequest(POST, uri(centre, "description"), updateJson).value
        reply must beOkResponseWithJsonReply

        val json = contentAsJson(reply)
        val replyCentre = (json \ "data").validate[CentreDto]
        replyCentre must be (jsSuccess)

        val updatedCentre = centre.copy(version      = centre.version + 1,
                                        description  = Some(newDescription),
                                        timeModified = Some(OffsetDateTime.now))
        replyCentre.get must matchCentre(updatedCentre)

        centreRepository.getByKey(centre.id) mustSucceed { repoCentre =>
          repoCentre must equal (updatedCentre)
        }
      }

      describe("not update description an invalid centre") {
        invalidCentreIdSharedBehaviour("description", Json.obj("description" -> nameGenerator.next[Study]))
      }

      describe("fail when updating name with invalid version") {
        updateWithInvalidVersionSharedBehaviour("description",
                                                Json.obj("description" -> nameGenerator.next[Study]))
      }

      describe("fail when updating description on an enabled centre") {
        updateEnabledCentreSharedBehaviour { centre =>
          (uri(centre, "description"), Json.obj("description" -> nameGenerator.next[Centre]))
        }
      }

    }

    describe("POST /api/centres/enable/:id") {

      it("enable a centre with at least one location") {
        val location = factory.createLocation
        val centre = factory.createDisabledCentre.copy(locations = Set(location))
        centreRepository.put(centre)

        val updateJson = Json.obj("expectedVersion" -> Some(centre.version))
        val reply = makeAuthRequest(POST, uri(centre, "enable"), updateJson).value
        reply must beOkResponseWithJsonReply

        val json = contentAsJson(reply)
        val replyCentre = (json \ "data").validate[CentreDto]
        replyCentre must be (jsSuccess)

        centre.enable mustSucceed { updatedCentre =>
          replyCentre.get must matchCentre(updatedCentre)
          centreRepository.getByKey(centre.id) mustSucceed { repoCentre =>
            repoCentre must equal (updatedCentre)
          }
        }
      }

      it("not enable a centre without locations") {
        val centre = factory.createDisabledCentre
        centreRepository.put(centre)

        val updateJson = Json.obj("expectedVersion" -> Some(centre.version))
        val reply = makeAuthRequest(POST, uri(centre, "enable"), updateJson).value
        reply must beBadRequestWithMessage("EntityCriteriaError.*not have locations")
      }

      it("not enable an already enabled centre") {
        val centre = factory.createEnabledCentre
        centreRepository.put(centre)

        val updateJson = Json.obj("expectedVersion" -> Some(centre.version))
        val reply = makeAuthRequest(POST, uri(centre, "enable"), updateJson).value
        reply must beBadRequestWithMessage("InvalidStatus: centre is not disabled")
      }

      it("not enable an invalid centre") {
        val centre = factory.createDisabledCentre
        val updateJson = Json.obj("expectedVersion" -> Some(0L))
        val reply = makeAuthRequest(POST, uri(centre, "enable"), updateJson).value
        reply must beNotFoundWithMessage("IdNotFound.*centre")
      }

    }

    describe("POST /api/centres/disable/:id") {

      it("disable a centre") {
        val centre = factory.createEnabledCentre
        centreRepository.put(centre)

        val updateJson = Json.obj("expectedVersion" -> Some(centre.version))
        val reply = makeAuthRequest(POST, uri(centre, "disable"), updateJson).value
        reply must beOkResponseWithJsonReply

        val json = contentAsJson(reply)
        val replyCentre = (json \ "data").validate[CentreDto]
        replyCentre must be (jsSuccess)

        centre.disable mustSucceed { updatedCentre =>
          replyCentre.get must matchCentre(updatedCentre)
          centreRepository.getByKey(centre.id) mustSucceed { repoCentre =>
            repoCentre must equal (updatedCentre)
          }
        }
      }

      it("not disable an already disabled centre") {
        val centre = factory.createDisabledCentre
        centreRepository.put(centre)

        val updateJson = Json.obj("expectedVersion" -> Some(centre.version))
        val reply = makeAuthRequest(POST, uri(centre, "disable"), updateJson).value
        reply must beBadRequestWithMessage("InvalidStatus: centre is not enabled")
      }

      it("not disable an invalid centre") {
        val centre = factory.createDisabledCentre

        val updateJson = Json.obj("expectedVersion" -> Some(0L))
        val reply = makeAuthRequest(POST, uri(centre, "disable"), updateJson).value
        reply must beNotFoundWithMessage("IdNotFound.*centre")
      }

    }

    describe("POST /api/centres/locations/:id") {

      it("add a location to a disabled centre") {
        val centre = factory.createDisabledCentre
        centreRepository.put(centre)

        val location = factory.createLocation
        val reply = makeAuthRequest(POST,
                                    uri(centre, "locations"),
                                    centreLocationToJson(centre, location)).value
        reply must beOkResponseWithJsonReply

        val replyDto = (contentAsJson(reply) \ "data").validate[CentreDto]
        replyDto must be (jsSuccess)

        val newLocationId = replyDto.get.locations.head.id
        val updatedCentre = centre.copy(version      = centre.version + 1,
                                        locations    = Set(location.copy(id = newLocationId)),
                                        timeModified = Some(OffsetDateTime.now))
        replyDto.get must matchCentre(updatedCentre)

        centreRepository.getByKey(centre.id) mustSucceed { repoCentre =>
          repoCentre must equal (updatedCentre)
        }
      }

      it("fail on attempt to add a location to an invalid centre") {
        val centre = factory.createDisabledCentre
        val location = factory.createLocation
        val reply = makeAuthRequest(POST,
                                    uri(centre, "locations"),
                                    centreLocationToJson(centre, location)).value
        reply must beNotFoundWithMessage("IdNotFound.*centre")
      }

      describe("fail when adding a location on an enabled centre") {
        updateEnabledCentreSharedBehaviour { centre =>
          (uri(centre, "locations"), centreLocationToJson(centre, factory.createLocation))
        }
      }
    }

    describe("POST /api/centres/locations/:id/:locationId") {

      it("update a location on a disabled centre") {
        val location = factory.createLocation
        val centre = factory.createDisabledCentre.copy(locations = Set(location))
        centreRepository.put(centre)

        val locationWithNewName = location.copy(name = nameGenerator.next[String])
        val reply = makeAuthRequest(POST,
                                    uri(centre, "locations") + s"/${location.id}",
                                    centreLocationToUpdateJson(centre, locationWithNewName)).value

        reply must beOkResponseWithJsonReply

        val json = contentAsJson(reply)
        val replyDto = (json \ "data").validate[CentreDto]
        replyDto must be (jsSuccess)

        val updatedCentre = centre.copy(version      = centre.version + 1,
                                        locations    = Set(location),
                                        timeModified = Some(OffsetDateTime.now))
        replyDto.get must matchCentre(updatedCentre)

        centreRepository.getByKey(centre.id) mustSucceed { repoCentre =>
          repoCentre must equal (updatedCentre)
        }
      }

      it("fail on attempt to update a location on an invalid centre") {
        val location = factory.createLocation
        val centre = factory.createEnabledCentre.copy(locations = Set(location))
        val reply = makeAuthRequest(POST,
                                    uri(centre, "locations") + s"/${location.id}",
                                    centreLocationToUpdateJson(centre, location)).value
        reply must beNotFoundWithMessage("IdNotFound.*centre")
      }

      describe("fail when updating a location on an enabled centre") {
        updateEnabledCentreSharedBehaviour { centre =>
          val location = factory.createLocation
          (uri("locations", centre.id.id, location.id.id), centreLocationToUpdateJson(centre, location))
        }
      }
    }

    describe("DELETE /api/centres/locations/:centreId/:ver/:locationId") {

      it("delete a location from a centre") {
        val locations = List(factory.createLocation, factory.createLocation)
        var locationsSet = locations.toSet
        val centre = factory.createDisabledCentre.copy(locations = locationsSet)
        centreRepository.put(centre)

        locations.zipWithIndex.foreach { case (location, index) =>
          val expectedVersion = centre.version + index
          val url = uri(centre, "locations") + s"/$expectedVersion/${location.id}"
          val reply = makeAuthRequest(DELETE, url).value
          reply must beOkResponseWithJsonReply

          locationsSet = locationsSet - location

          val updatedCentre = centre.copy(version      = expectedVersion + 1,
                                          locations    = locationsSet,
                                          timeModified = Some(OffsetDateTime.now))
          val json = contentAsJson(reply)
          val replyDto = (json \ "data").validate[CentreDto]
          replyDto must be (jsSuccess)
          replyDto.get must matchCentre(updatedCentre)

          centreRepository.getByKey(centre.id) mustSucceed { repoCentre =>
            repoCentre must equal (updatedCentre)
          }
        }
      }

      it("fail when deleting a location from an invalid centre") {
        val location = factory.createLocation
        val centre = factory.createDisabledCentre.copy(locations = Set(location))
        centreRepository.put(centre)

        val centre2 = factory.createDisabledCentre
        val url = uri(centre2, "locations") + s"/${centre.version}/${location.id}"
        val reply = makeAuthRequest(DELETE, url).value
        reply must beNotFoundWithMessage("IdNotFound.*centre")
      }

      it("fail when deleting an invalid location from a centre") {
        val location = factory.createLocation
        val centre = factory.createDisabledCentre.copy(locations = Set.empty)
        centreRepository.put(centre)

        val url = uri(centre, "locations") + s"/${centre.version}/${location.id}"
        val reply = makeAuthRequest(DELETE, url).value
        reply must beNotFoundWithMessage("location.*does not exist")
      }

    }

    describe("POST /api/centres/studies/:centerId") {

      it("add a study to a centre") {
        val centre = factory.createDisabledCentre.copy(studyIds = Set.empty)
        centreRepository.put(centre)

        val study = factory.createDisabledStudy
        studyRepository.put(study)

        val reqJson = Json.obj("expectedVersion" -> centre.version,
                               "studyId"         -> study.id)
        val reply = makeAuthRequest(POST, uri(centre, "studies"), reqJson).value
        reply must beOkResponseWithJsonReply

        val replyDto = (contentAsJson(reply) \ "data").validate[CentreDto]
        replyDto must be (jsSuccess)

        val updatedCentre = centre.copy(version      = centre.version + 1,
                                        studyIds     = Set(study.id),
                                        timeModified = Some(OffsetDateTime.now))
        replyDto.get must matchCentre(updatedCentre)

        centreRepository.getByKey(centre.id) mustSucceed { repoCentre =>
          repoCentre must equal (updatedCentre)
        }
      }

      it("fail when adding a study that does not exist") {
        val study = factory.createDisabledStudy
        val centre = factory.createDisabledCentre.copy(studyIds = Set.empty)
        centreRepository.put(centre)

        val reqJson = Json.obj("expectedVersion" -> centre.version,
                               "studyId"         -> study.id)
        val reply = makeAuthRequest(POST, uri(centre, "studies"), reqJson).value
        reply must beNotFoundWithMessage("IdNotFound.*study")
      }

      describe("fail when adding a study on an enabled centre") {
        updateEnabledCentreSharedBehaviour { centre =>
          val study = factory.createDisabledStudy
          (uri(centre, "studies"), Json.obj("studyId" -> study.id))
        }
      }

    }

    describe("DELETE /api/centres/studies/:id/:ver/:studyId") {

      it("remove a study") {
        val study = factory.createDisabledStudy
        studyRepository.put(study)

        val centre = factory.createDisabledCentre.copy(studyIds = Set(study.id))
        centreRepository.put(centre)

        val url = uri(centre, "studies") + s"/${centre.version}/${study.id.id}"
        val reply = makeAuthRequest(DELETE, url).value
        reply must beOkResponseWithJsonReply

        val updatedCentre = centre.copy(version      = centre.version + 1,
                                        studyIds     = Set.empty[StudyId],
                                        timeModified = Some(OffsetDateTime.now))

        val replyDto = (contentAsJson(reply) \ "data").validate[CentreDto]
        replyDto must be (jsSuccess)
        replyDto.get must matchCentre(updatedCentre)

        centreRepository.getByKey(centre.id) mustSucceed { repoCentre =>
          repoCentre must equal (updatedCentre)
        }
      }

      it("fail on attempt remove an invalid study from a centre") {
        val invalidStudyId = nameGenerator.next[Study]
        val centre = factory.createDisabledCentre.copy(studyIds = Set.empty)
        centreRepository.put(centre)

        val url = uri(centre, "studies") + s"/${centre.version}/$invalidStudyId"
        val reply = makeAuthRequest(DELETE, url).value
        reply must beNotFoundWithMessage("IdNotFound.*study")
      }

      it("fail when removing a study on an enabled centre") {
        val study = factory.createDisabledStudy
        studyRepository.put(study)

        val centre = factory.createEnabledCentre.copy(studyIds = Set(study.id))
        centreRepository.put(centre)

        val url = uri(centre, "studies") + s"/${centre.version}/${study.id.id}"
        val reply = makeAuthRequest(DELETE, url).value
        reply must beBadRequestWithMessage("centre is not disabled")
      }

    }

    describe("GET /api/centres/names") {

      it("must return centre names") {
        val centres = (1 to 2).map {_ => factory.createDisabledCentre }
        val nameDtos = centres.map(_.nameDto).toSeq
        centres.foreach(centreRepository.put)

        val sortTable = Table(
            ("order", "dtos"),
            ("name",  nameDtos.sortWith { (a, b) => (a.name compareToIgnoreCase b.name) < 0 }),
            ("-name", nameDtos.sortWith { (a, b) => (a.name compareToIgnoreCase b.name) > 0 }))

        forAll(sortTable) { (order, sortedDtos) =>
          val reply = makeAuthRequest(GET, uri("names") + s"?sort=$order").value
          reply must beOkResponseWithJsonReply

          val replyDtos = (contentAsJson(reply) \ "data").validate[List[NameAndStateDto]]
          replyDtos must be (jsSuccess)

          replyDtos.get.size must be (nameDtos.size)
          replyDtos.get.zip(sortedDtos).foreach { case (replyDto, nameDto) =>
            replyDto must equal (nameDto)
          }
        }
      }

      it("must return centre names filtered by name") {
        val centres = (1 to 2).map {_ => factory.createDisabledCentre }
        centres.foreach(centreRepository.put)
        val centre = centres.head

        val reply = makeAuthRequest(GET, uri("names") + s"?filter=name::${centre.name}").value
        reply must beOkResponseWithJsonReply

        val replyDtos = (contentAsJson(reply) \ "data").validate[List[NameAndStateDto]]
        replyDtos must be (jsSuccess)

        replyDtos.get.size must be (1)
        replyDtos.get.foreach { replyDto =>
          replyDto must equal (NameAndStateDto(centre))
        }
      }

    }

    describe("POST /api/centres/locations") {

      it("return centre locations") {
        val location = factory.createLocation
        val centre = factory.createDisabledCentre.copy(locations = Set(location))
        centreRepository.put(centre)

        val reqJson = Json.obj("filter" -> "", "limit" -> 10)
        val reply = makeAuthRequest(POST, uri("locations"), reqJson).value
        reply must beOkResponseWithJsonReply

        val replyLocations = (contentAsJson(reply) \ "data").validate[List[CentreLocationInfo]]
        replyLocations must be (jsSuccess)
        replyLocations.get.size must be (1)
        replyLocations.get.foreach { replyLocation =>
          replyLocation must equal (CentreLocationInfo(centre.id.id,
                                                       location.id.id,
                                                       centre.name,
                                                       location.name))
        }
      }

      it("return centre locations filtered by name") {
        val centres = (1 to 2).map {_ =>
            factory.createDisabledCentre.copy(locations = Set(factory.createLocation))
          }
        val centre = centres(0)
        val location = centre.locations.head
        centres.foreach(centreRepository.put)

        val reqJson = Json.obj("filter" -> location.name, "limit" -> 10)
        val reply = makeAuthRequest(POST, uri("locations"), reqJson).value
        reply must beOkResponseWithJsonReply

        val replyLocations = (contentAsJson(reply) \ "data").validate[List[CentreLocationInfo]]
        replyLocations must be (jsSuccess)
        replyLocations.get.size must be (1)
        replyLocations.get.foreach { replyLocation =>
          replyLocation must equal (CentreLocationInfo(centre.id.id,
                                                       location.id.id,
                                                       centre.name,
                                                       location.name))
        }
      }

      it("return centre locations sorted by name") {
        val entities = (1 to 2).map { _ =>
            val location = factory.createLocation
            val centre = factory.createDisabledCentre.copy(locations = Set(location))
            val centreLocation = CentreLocation(centre.id.id,
                                                location.id.id,
                                                centre.name,
                                                location.name)
            centreRepository.put(centre)
            (centre, location, centreLocation)
          }

        val centreLocations = entities.map(t => t._3)
          .sortWith { (a, b) => (a.locationName compareToIgnoreCase b.locationName) < 0 }

        val reqJson = Json.obj("filter" -> "", "limit" -> 10)
        val reply = makeAuthRequest(POST, uri("locations"), reqJson).value
        reply must beOkResponseWithJsonReply

        val replyLocations = (contentAsJson(reply) \ "data").validate[List[CentreLocationInfo]]
        replyLocations must be (jsSuccess)
        replyLocations.get.size must be (centreLocations.size)

        replyLocations.get.zip(centreLocations).foreach { case (replyLocation, centreLocation) =>
          replyLocation must equal (CentreLocationInfo(centreLocation))
        }
      }

    }

  }

  private def invalidCentreIdSharedBehaviour(path: String, json: JsObject) = {

    it("shoud return not found") {
      val invalidCentreId = nameGenerator.next[Centre]
      val requestJson = json ++ Json.obj("id"              -> nameGenerator.next[Centre],
                                         "expectedVersion" -> 0L)
      val reply = makeAuthRequest(POST, uri(path) + s"/$invalidCentreId", requestJson)
      reply.value must beNotFoundWithMessage("IdNotFound.*centre")
    }
  }



  private def updateWithInvalidVersionSharedBehaviour(path: String, json: JsObject) = {

    it("should return bad request") {
      val centre = factory.createDisabledCentre
      centreRepository.put(centre)

      val reqJson = Json.obj("id"              -> centre.id.id,
                             "expectedVersion" -> Some(centre.version + 1)) ++ json

      val reply = makeAuthRequest(POST, uri(centre, path), reqJson)
      reply.value must beBadRequestWithMessage("expected version doesn't match current version")
    }
  }

  private def updateEnabledCentreSharedBehaviour(func: Centre => (String, JsObject)) = {

    it("should return bad request") {
      val centre = factory.createEnabledCentre
      centreRepository.put(centre)
      val (url, json) = func(centre)

      val reqJson = Json.obj("expectedVersion" -> Some(centre.version)) ++ json
      val reply = makeAuthRequest(POST, url, reqJson)
      reply.value must beBadRequestWithMessage("centre is not disabled")
    }
  }

  private def listSingleCentre(offset:    Long = 0,
                               maybeNext: Option[Int] = None,
                               maybePrev: Option[Int] = None)
                              (setupFunc: () => (Url, Centre)) = {

    it("list single centre") {
      val (url, expectedCentre) = setupFunc()
      val reply = makeAuthRequest(GET, url.path).value
      reply must beOkResponseWithJsonReply

      val json = contentAsJson(reply)
      json must beSingleItemResults(offset, maybeNext, maybePrev)

      val dtosValidation = (json \ "data" \ "items").validate[List[CentreDto]]
      dtosValidation must be (jsSuccess)
      dtosValidation.get.foreach { _ must matchCentre(expectedCentre) }
    }
  }

  private def listMultipleCentres(offset:    Long = 0,
                                  maybeNext: Option[Int] = None,
                                  maybePrev: Option[Int] = None)
                                 (setupFunc: () => (Url, List[Centre])) = {

    it("list multiple centres") {
      val (url, expectedCentres) = setupFunc()

      val reply = makeAuthRequest(GET, url.path).value
      reply must beOkResponseWithJsonReply

      val json = contentAsJson(reply)
      json must beMultipleItemResults(offset = offset,
                                      total = expectedCentres.size.toLong,
                                      maybeNext = maybeNext,
                                      maybePrev = maybePrev)

      val dtosValidation = (json \ "data" \ "items").validate[List[CentreDto]]
      dtosValidation must be (jsSuccess)

      (dtosValidation.get zip expectedCentres).foreach { case (dto, centre) =>
        dto must matchCentre(centre)
      }
    }

  }

}
