package org.biobank.controllers.access

import java.time.OffsetDateTime
import org.biobank.controllers._
import org.biobank.domain.Slug
import org.biobank.domain.access._
import org.biobank.domain.centres._
import org.biobank.domain.studies._
import org.biobank.domain.users._
import org.biobank.dto._
import org.biobank.dto.access._
import org.biobank.fixtures.Url
import org.biobank.matchers.PagedResultsMatchers
import org.scalatest.matchers.{MatchResult, Matcher}
import org.scalatest.prop.TableDrivenPropertyChecks._
import play.api.mvc._
import play.api.libs.json._
import play.api.test.Helpers._
import org.scalatest.Inside
import scala.concurrent.Future

/**
 * Tests the memberships REST API for [[User Access]].
 *
 * Tests for [[Role]]s and [[Permissions]] in AccessControllerSpec.scala.
 */
class AccessControllerMembershipSpec
    extends AccessControllerSpecCommon
    with UserFixtures
    with Inside
    with PagedResultsSharedSpec
    with PagedResultsMatchers {

  import org.biobank.matchers.DtoMatchers._
  import org.biobank.matchers.EntityMatchers._
  import org.biobank.matchers.JsonMatchers._

  private class MembershipFixture {
    val user   = factory.createActiveUser
    val study  = factory.createEnabledStudy
    val centre = factory.createEnabledCentre

    private val _membership = factory.createMembership
    private val studyData  = _membership.studyData.addEntity(study.id)
    private val centreData = _membership.centreData.addEntity(centre.id)

    val membership = _membership.copy(userIds    = Set(user.id),
                                      studyData  = studyData,
                                      centreData = centreData)

    Set(user, study, centre, membership).foreach(addToRepository)
  }

  private class MembershipFixtureAllStudies {
    val user       = factory.createActiveUser

    private val _membership = factory.createMembership
    private val studyData  = _membership.studyData.hasAllEntities

    val membership = _membership.copy(userIds   = Set(user.id),
                                      studyData = studyData)

    Set(user, membership).foreach(addToRepository)
  }

  private class MembershipFixtureAllCentres {
    val user       = factory.createActiveUser

    private val _membership = factory.createMembership
    private val centreData  = _membership.centreData.hasAllEntities

    val membership = _membership.copy(userIds    = Set(user.id),
                                      centreData = centreData)

    Set(user, membership).foreach(addToRepository)
  }

  private val DefaultMembershipId = MembershipId(Slug("All studies and all centres").id)

  describe("Access REST API (memberships)") {

    describe("GET /api/access/membership") {

      describe("lists the default membership") {
        listSingleMembership() { () =>
          val defaultMembership = membershipRepository.getByKey(DefaultMembershipId).toOption.value
            (new Url(uri("memberships")), defaultMembership)
        }
      }

      describe("list multiple memberships") {
        listMultipleMemberships() { () =>
          val defaultMembership = membershipRepository.getByKey(DefaultMembershipId).toOption.value
          val memberships = (0 until 2).map(_ => factory.createMembership).toList
          memberships.foreach(membershipRepository.put)
          (new Url(uri("memberships")), defaultMembership :: memberships.sortBy(_.name))
        }
      }

      describe("list a single membership when filtered by name") {
        listSingleMembership() { () =>
          val memberships = List(factory.createMembership.copy(name = "membership1"),
                                 factory.createMembership.copy(name = "membership2"))
          val membership = memberships(0)
          memberships.foreach(membershipRepository.put)
          (new Url(uri("memberships") + s"?filter=name::${membership.name}"), membership)
        }
      }

      describe("list memberships sorted by name") {

        def commonSetup = {
          val defaultMembership = membershipRepository.getByKey(DefaultMembershipId).toOption.value
          val memberships = List(factory.createMembership.copy(name = "membership3"),
                                 factory.createMembership.copy(name = "membership2"),
                                 factory.createMembership.copy(name = "membership1"))
          memberships.foreach(membershipRepository.put)
          defaultMembership :: memberships
        }

        describe("in ascending order") {
          listMultipleMemberships() { () =>
            val memberships = commonSetup
            (new Url(uri("memberships") + "?sort=name"), memberships.sortBy(_.name))
          }
        }

        describe("in descending order") {
          listMultipleMemberships() { () =>
            val memberships = commonSetup
            (new Url(uri("memberships") + "?sort=-name"), memberships.sortBy(_.name).reverse)
          }
        }
      }

      describe("list a single membership when using paged query") {
        listSingleMembership(maybeNext = Some(2)) { () =>
          val memberships = List(factory.createMembership.copy(name = "membership3"),
                                 factory.createMembership.copy(name = "membership2"),
                                 factory.createMembership.copy(name = "membership1"))
          memberships.foreach(membershipRepository.put)
          (new Url(uri("memberships") + s"?filter=name:like:membership&sort=name&limit=1"), memberships(2))
        }
      }

      describe("fail when using an invalid query parameters") {
        pagedQueryShouldFailSharedBehaviour { () =>
          new Url(uri("memberships"))
        }
      }

    }

    describe("GET /api/access/membership/:slug") {

      it("returns a membership") {
        val f = new MembershipFixture
        val reply = makeAuthRequest(GET, uri("memberships", f.membership.slug.id)).value
        reply must beOkResponseWithJsonReply

        val dto = (contentAsJson(reply) \ "data").validate[MembershipDto]
        dto must be (jsSuccess)
        dto.get must matchDtoToMembership(f.membership)
      }

      it("fails for an invalid membership") {
        val f = new MembershipFixture
        membershipRepository.remove(f.membership)
        val reply = makeAuthRequest(GET, uri("memberships", f.membership.slug.id)).value
        reply must beNotFoundWithMessage("EntityCriteriaNotFound: membership slug")
      }

    }

    describe("POST /api/access/memberships") {

      it("can create a membership") {
        val f = new MembershipFixture
        membershipRepository.remove(f.membership)
        val reqJson = membershipToAddJson(f.membership)

        val reply = makeAuthRequest(POST, uri("memberships"), reqJson).value
        reply must beOkResponseWithJsonReply

        val jsonId = (contentAsJson(reply) \ "data" \ "id").validate[MembershipId]
        jsonId must be (jsSuccess)

        val updatedMembership = f.membership.copy(id        = jsonId.get,
                                                  timeAdded = OffsetDateTime.now)
        reply must matchUpdatedMembership(updatedMembership)
      }

      it("fails when adding a second membership with a name that already exists") {
        val f = new MembershipFixture
        val reqJson = membershipToAddJson(f.membership)
        val reply = makeAuthRequest(POST, uri("memberships"), reqJson).value
        reply must beBadRequestWithMessage("EntityCriteriaError: name already used:")
      }

      it("attempt to create membership fails if no users are added") {
        val f = new MembershipFixture
        val reqJson = membershipToAddJson(f.membership.copy(userIds = Set.empty[UserId]))
        val reply = makeAuthRequest(POST, uri("memberships"), reqJson).value
        reply must beBadRequestWithMessage("userIds cannot be empty")
      }

      it("attempt to create membership fails if user does not exist") {
        val f = new MembershipFixture
        userRepository.remove(f.user)
        val reqJson = membershipToAddJson(f.membership)
        val reply = makeAuthRequest(POST, uri("memberships"), reqJson).value
        reply must beNotFoundWithMessage("IdNotFound: user id")
      }

      it("attempt to create membership fails if study does not exist") {
        val f = new MembershipFixture
        studyRepository.remove(f.study)
        val reqJson = membershipToAddJson(f.membership)
        val reply = makeAuthRequest(POST, uri("memberships"), reqJson).value
        reply must beNotFoundWithMessage("IdNotFound: study id")
      }

      it("attempt to create membership fails if centre does not exist") {
        val f = new MembershipFixture
        centreRepository.remove(f.centre)
        val reqJson = membershipToAddJson(f.membership)
        val reply = makeAuthRequest(POST, uri("memberships"), reqJson).value
        reply must beNotFoundWithMessage("IdNotFound: centre id")
      }

    }

    describe("GET /api/access/memberships/names") {
      val createEntity = (name: String) => factory.createMembership.copy(name = name)
      val baseUrl = uri("memberships", "names")

      it should behave like accessEntityNameSharedBehaviour(createEntity, baseUrl) {
        (dtos: List[EntityInfoDto], memberships: List[Membership]) =>
        (dtos zip memberships).foreach { case (dto, membership) =>
          dto must matchEntityInfoDtoToMembership(membership)
        }
      }
    }

    describe("POST /api/access/memberships/name/:membershipId") {

      def updateNameJson(membership: Membership, name: String) = {
        Json.obj("expectedVersion" -> membership.version, "name" -> name)
      }

      it("can update the name") {
        val f = new MembershipFixture
        val newName = nameGenerator.next[String]
        val json = updateNameJson(f.membership, newName)
        val reply = makeAuthRequest(POST, uri("memberships", "name", f.membership.id.id), json).value
        reply must beOkResponseWithJsonReply

        val updatedMembership = f.membership.copy(version      = f.membership.version + 1,
                                                  slug         = Slug(newName),
                                                  name         = newName ,
                                                  timeModified = Some(OffsetDateTime.now))
        reply must matchUpdatedMembership(updatedMembership)
      }

      it("fails when updating to name already used by another membership") {
        val f = new MembershipFixture
        val membership = factory.createMembership
        membershipRepository.put(membership)
        val reqJson = updateNameJson(f.membership, membership.name)
        val reply = makeAuthRequest(POST, uri("memberships") + s"/name/${f.membership.id}", reqJson).value
        reply must beBadRequestWithMessage("EntityCriteriaError: name already used:")
      }

      it("fail when updating to something with less than 2 characters") {
        val f = new MembershipFixture
        val reqJson = updateNameJson(f.membership, "a")
        val reply = makeAuthRequest(POST, uri("memberships") + s"/name/${f.membership.id}", reqJson).value
        reply must beBadRequestWithMessage("InvalidName")
      }

      it("fail when updating and membership ID does not exist") {
        val f = new MembershipFixture
        membershipRepository.remove(f.membership)
        val reply = makeAuthRequest(POST,
                                    uri("memberships") + s"/name/${f.membership.id}",
                                    updateNameJson(f.membership, nameGenerator.next[Membership]))
        reply.value must beNotFoundWithMessage("IdNotFound.*membership")
      }

      it("fail when updating with invalid version") {
        val f = new MembershipFixture
        val reqJson = updateNameJson(f.membership, nameGenerator.next[Membership]) ++
        Json.obj("expectedVersion" -> (f.membership.version + 10L))

        val reply = makeAuthRequest(POST, uri("memberships") + s"/name/${f.membership.id}", reqJson).value
        reply must beBadRequestWithMessage("expected version doesn't match current version")
      }

    }

    describe("POST /api/access/memberships/description/:membershipId") {

      def updateDescriptionJson(membership: Membership, description: Option[String]) = {
        Json.obj("expectedVersion" -> membership.version) ++
        JsObject(
          Seq[(String, JsValue)]() ++
            description.map("description" -> Json.toJson(_)))
      }

      it("update a description") {
        val descriptionValues = Table("descriptions", Some(nameGenerator.next[String]), None)
        forAll(descriptionValues) { newDescription =>
          val f = new MembershipFixture
          val reqJson = updateDescriptionJson(f.membership, newDescription)
          val url = uri("memberships") + s"/description/${f.membership.id}"
          val reply = makeAuthRequest(POST, url, reqJson).value
          reply must beOkResponseWithJsonReply

          val updatedMembership = f.membership.copy(version      = f.membership.version + 1,
                                                    description  = newDescription,
                                                    timeModified = Some(OffsetDateTime.now))
          reply must matchUpdatedMembership(updatedMembership)
        }
      }

      it("fail when updating and membership ID does not exist") {
        val f = new MembershipFixture
        membershipRepository.remove(f.membership)
        val reply = makeAuthRequest(POST,
                                    uri("memberships") + s"/description/${f.membership.id}",
                                    updateDescriptionJson(f.membership,
                                                          Some(nameGenerator.next[Membership])))
        reply.value must beNotFoundWithMessage("IdNotFound.*membership")
      }

      it("fail when updating with invalid version") {
        val f = new MembershipFixture
        val reqJson = updateDescriptionJson(f.membership, Some(nameGenerator.next[Membership])) ++
        Json.obj("expectedVersion" -> Some(f.membership.version + 10L))
        val url = uri("memberships") + s"/description/${f.membership.id}"
        val reply = makeAuthRequest(POST, url, reqJson).value
        reply must beBadRequestWithMessage("expected version doesn't match current version")
      }

    }

    describe("POST /api/access/memberships/user/:membershipId") {

      def addUserJson(membership: Membership, user: User) = {
        Json.obj("userId" -> user.id.id, "expectedVersion" -> membership.version)
      }

      it("can add a user") {
        val f = new MembershipFixture
        val user = factory.createRegisteredUser
        val reqJson = addUserJson(f.membership, user)

        userRepository.put(user)
        val url = uri("memberships") + s"/user/${f.membership.id}"
        val reply = makeAuthRequest(POST, url, reqJson).value
        reply must beOkResponseWithJsonReply

        val updatedMembership = f.membership.copy(version      = f.membership.version + 1,
                                                  userIds      = f.membership.userIds + user.id,
                                                  timeModified = Some(OffsetDateTime.now))
        reply must matchUpdatedMembership(updatedMembership)
      }

      it("cannot add the same user more than once") {
        val f = new MembershipFixture
        val reqJson = addUserJson(f.membership, f.user)
        val url = uri("memberships") + s"/user/${f.membership.id}"
        val reply = makeAuthRequest(POST, url, reqJson).value
        reply must beBadRequestWithMessage("user ID is already in membership")
      }

      it("cannot add a user that does not exist") {
        val f = new MembershipFixture
        val user = factory.createRegisteredUser
        val reqJson = addUserJson(f.membership, user)
        val url = uri("memberships") + s"/user/${f.membership.id}"
        val reply = makeAuthRequest(POST, url, reqJson).value
        reply must beNotFoundWithMessage("IdNotFound: user id")
      }

      it("fail when updating and membership ID does not exist") {
        val f = new MembershipFixture
        val user = factory.createRegisteredUser
        val reqJson = addUserJson(f.membership, user)
        userRepository.put(user)
        membershipRepository.remove(f.membership)

        val reply = makeAuthRequest(POST,
                                    uri("memberships") + s"/user/${f.membership.id}",
                                    reqJson)
        reply.value must beNotFoundWithMessage("IdNotFound.*membership")
      }

      it("fail when updating with invalid version") {
        val f = new MembershipFixture
        val user = factory.createRegisteredUser
        val reqJson = addUserJson(f.membership, user) ++
        Json.obj("expectedVersion" -> Some(f.membership.version + 10L))
        userRepository.put(user)

        val reply = makeAuthRequest(POST, uri("memberships") + s"/user/${f.membership.id}", reqJson).value
        reply must beBadRequestWithMessage("expected version doesn't match current version")
      }

    }

    describe("POST /api/access/memberships/allStudies/:membershipId") {

      it("can assign a membership, for a single study, to be for all studies") {
        val f = new MembershipFixture

        f.membership.centreData.allEntities must be (false)
        f.membership.studyData.ids must not have size (0L)

        val reqJson = Json.obj("expectedVersion" -> f.membership.version)
        val url = uri("memberships") + s"/allStudies/${f.membership.id}"
        val reply = makeAuthRequest(POST, url, reqJson).value
        reply must beOkResponseWithJsonReply

        val updatedMembership = f.membership.copy(version      = f.membership.version + 1,
                                                  studyData    = MembershipEntitySet(true, Set.empty[StudyId]),
                                                  timeModified = Some(OffsetDateTime.now))
        reply must matchUpdatedMembership(updatedMembership)
      }

    }

    describe("POST /api/access/memberships/study/:membershipId") {

      def addStudyJson(membership: Membership, study: Study) = {
        Json.obj("studyId" -> study.id.id, "expectedVersion" -> membership.version)
      }

      it("can add a study") {
        val f = new MembershipFixture
        val study = factory.createEnabledStudy
        val reqJson = addStudyJson(f.membership, study)

        studyRepository.put(study)
        val reply = makeAuthRequest(POST,
                                    uri("memberships") + s"/study/${f.membership.id}",
                                    reqJson).value
        reply must beOkResponseWithJsonReply

        val newStudyData = f.membership.studyData.copy(ids = f.membership.studyData.ids + study.id)
        val updatedMembership = f.membership.copy(version      = f.membership.version + 1,
                                                  studyData    = newStudyData,
                                                  timeModified = Some(OffsetDateTime.now))
        reply must matchUpdatedMembership(updatedMembership)
      }

      it("an all studies membership can be modified to be for a single study") {
        val f = new MembershipFixtureAllStudies
        val study = factory.createEnabledStudy
        val reqJson = addStudyJson(f.membership, study)

        studyRepository.put(study)
        val reply = makeAuthRequest(POST, uri("memberships") + s"/study/${f.membership.id}", reqJson).value
        reply must beOkResponseWithJsonReply

        val newStudyData = f.membership.studyData.copy(allEntities = false, ids = Set(study.id))
        val updatedMembership = f.membership.copy(version      = f.membership.version + 1,
                                                  studyData    = newStudyData,
                                                  timeModified = Some(OffsetDateTime.now))
        reply must matchUpdatedMembership(updatedMembership)
      }

      it("cannot add the same study more than once") {
        val f = new MembershipFixture
        val reqJson = addStudyJson(f.membership, f.study)
        val reply = makeAuthRequest(POST, uri("memberships") + s"/study/${f.membership.id}", reqJson).value
        reply must beBadRequestWithMessage("study ID is already in membership")
      }

      it("cannot add a study that does not exist") {
        val f = new MembershipFixture
        val study = factory.createEnabledStudy
        val reqJson = addStudyJson(f.membership, study)
        val reply = makeAuthRequest(POST, uri("memberships") + s"/study/${f.membership.id}", reqJson).value
        reply must beNotFoundWithMessage("IdNotFound: study id")
      }

      it("fail when updating and membership ID does not exist") {
        val f = new MembershipFixture
        val study = factory.createEnabledStudy
        val reqJson = addStudyJson(f.membership, study)
        studyRepository.put(study)
        membershipRepository.remove(f.membership)
        val reply = makeAuthRequest(POST,
                                    uri("memberships") + s"/study/${f.membership.id}",
                                    reqJson)
        reply.value must beNotFoundWithMessage("IdNotFound.*membership")
      }

      it("fail when updating with invalid version") {
        val f = new MembershipFixture
        val study = factory.createEnabledStudy
        val reqJson = addStudyJson(f.membership, study) ++
        Json.obj("expectedVersion" -> Some(f.membership.version + 10L))
        studyRepository.put(study)
        val reply = makeAuthRequest(POST, uri("memberships") + s"/study/${f.membership.id}", reqJson).value
        reply must beBadRequestWithMessage("expected version doesn't match current version")
      }

    }

    describe("POST /api/access/memberships/allCentres/:membershipId") {

      it("can assign a membership, for a single centre, to be for all centres") {
        val f = new MembershipFixture

        f.membership.centreData.allEntities must be (false)
        f.membership.centreData.ids must not have size (0L)

        val reqJson = Json.obj("expectedVersion" -> f.membership.version)
        val reply = makeAuthRequest(POST,
                                    uri("memberships") + s"/allCentres/${f.membership.id}",
                                    reqJson).value
        reply must beOkResponseWithJsonReply

        val updatedMembership = f.membership.copy(version      = f.membership.version + 1,
                                                  centreData   = MembershipEntitySet(true, Set.empty[CentreId]),
                                                  timeModified = Some(OffsetDateTime.now))
        reply must matchUpdatedMembership(updatedMembership)
      }

    }

    describe("POST /api/access/memberships/centre/:membershipId") {

      def addCentreJson(membership: Membership, centre: Centre) = {
        Json.obj("centreId" -> centre.id.id, "expectedVersion" -> membership.version)
      }

      it("can add a centre") {
        val f = new MembershipFixture
        val centre = factory.createEnabledCentre
        val reqJson = addCentreJson(f.membership, centre)

        centreRepository.put(centre)
        val reply = makeAuthRequest(POST,
                                    uri("memberships") + s"/centre/${f.membership.id}",
                                    reqJson).value
        reply must beOkResponseWithJsonReply

        val newCentreData = f.membership.centreData.copy(ids = f.membership.centreData.ids + centre.id)
        val updatedMembership = f.membership.copy(version      = f.membership.version + 1,
                                                  centreData   = newCentreData,
                                                  timeModified = Some(OffsetDateTime.now))
        reply must matchUpdatedMembership(updatedMembership)
      }

      it("an all centres membership can be modified to be for a single centre") {
        val f = new MembershipFixtureAllCentres
        val centre = factory.createEnabledCentre
        val reqJson = addCentreJson(f.membership, centre)

        centreRepository.put(centre)
        val reply = makeAuthRequest(POST,
                                    uri("memberships") + s"/centre/${f.membership.id}",
                                    reqJson).value
        reply must beOkResponseWithJsonReply

        val newCentreData = f.membership.centreData.copy(allEntities = false, ids = Set(centre.id))
        val updatedMembership = f.membership.copy(version      = f.membership.version + 1,
                                                  centreData   = newCentreData,
                                                  timeModified = Some(OffsetDateTime.now))
        reply must matchUpdatedMembership(updatedMembership)
      }

      it("cannot add the same centre more than once") {
        val f = new MembershipFixture
        val reqJson = addCentreJson(f.membership, f.centre)
        val reply = makeAuthRequest(POST,
                                    uri("memberships") + s"/centre/${f.membership.id}",
                                    reqJson).value
        reply must beBadRequestWithMessage("centre ID is already in membership")
      }

      it("cannot add a centre that does not exist") {
        val f = new MembershipFixture
        val centre = factory.createEnabledCentre
        val reqJson = addCentreJson(f.membership, centre)
        val reply = makeAuthRequest(POST,
                                    uri("memberships") + s"/centre/${f.membership.id}",
                                    reqJson).value
        reply must beNotFoundWithMessage("IdNotFound: centre id")
      }

      it("fail when updating and membership ID does not exist") {
        val f = new MembershipFixture
        val centre = factory.createEnabledCentre
        val reqJson = addCentreJson(f.membership, centre)
        centreRepository.put(centre)
        membershipRepository.remove(f.membership)

        val reply = makeAuthRequest(POST,
                                    uri("memberships") + s"/centre/${f.membership.id}",
                                    reqJson)
        reply.value must beNotFoundWithMessage("IdNotFound.*membership")
      }

      it("fail when updating with invalid version") {
        val f = new MembershipFixture
        val centre = factory.createEnabledCentre
        val reqJson = addCentreJson(f.membership, centre) ++
        Json.obj("expectedVersion" -> Some(f.membership.version + 10L))
        centreRepository.put(centre)
        val reply = makeAuthRequest(POST,
                                    uri("memberships") + s"/centre/${f.membership.id}",
                                    reqJson).value
        reply must beBadRequestWithMessage("expected version doesn't match current version")
      }

    }

    describe("DELETE /api/access/memberships/user/:membershipId/:version/:userId") {

      it("can remove a user") {
        val f = new MembershipFixture
        val url = uri("memberships") + s"/user/${f.membership.id}/${f.membership.version}/${f.user.id.id}"
        val reply = makeAuthRequest(DELETE, url).value
        reply must beOkResponseWithJsonReply

        val updatedMembership = f.membership.copy(version      = f.membership.version + 1,
                                                  userIds      = f.membership.userIds - f.user.id,
                                                  timeModified = Some(OffsetDateTime.now))
        reply must matchUpdatedMembership(updatedMembership)
      }

      it("cannot remove a user not in the membership") {
        val f = new MembershipFixture
        val user = factory.createRegisteredUser
        userRepository.put(user)
        val url = uri("memberships") + s"/user/${f.membership.id}/${f.membership.version}/${user.id.id}"
        val reply = makeAuthRequest(DELETE, url).value
        reply must beBadRequestWithMessage("user ID is not in membership")
      }

      it("cannot remove a user that does not exist") {
        val f = new MembershipFixture
        val user = factory.createRegisteredUser
        val url = uri("memberships") + s"/user/${f.membership.id}/${f.membership.version}/${user.id.id}"
        val reply = makeAuthRequest(DELETE, url).value
        reply must beNotFoundWithMessage("IdNotFound: user id")
      }

      it("fail when removing and membership ID does not exist") {
        val f = new MembershipFixture
        val user = factory.createRegisteredUser
        val url = uri("memberships") + s"/user/${f.membership.id}/${f.membership.version}/${user.id.id}"
        userRepository.put(user)
        membershipRepository.remove(f.membership)

        val reply = makeAuthRequest(DELETE, url)
        reply.value must beNotFoundWithMessage("IdNotFound.*membership")
      }

      it("fail when removing with invalid version") {
        val f = new MembershipFixture
        val user = factory.createRegisteredUser
        val url = uri("memberships") + s"/user/${f.membership.id}/${f.membership.version + 10L}/${user.id.id}"
        userRepository.put(user)
        val reply = makeAuthRequest(DELETE, url).value
        reply must beBadRequestWithMessage("expected version doesn't match current version")
      }

    }

    describe("DELETE /api/access/memberships/study/:membershipId/:version/:studyId") {

      it("can remove a study") {
        val f = new MembershipFixture
        val url = uri("memberships") + s"/study/${f.membership.id}/${f.membership.version}/${f.study.id.id}"
        val reply = makeAuthRequest(DELETE, url).value
        reply must beOkResponseWithJsonReply

        val newStudyData = f.membership.studyData.copy(ids = f.membership.studyData.ids - f.study.id)
        val updatedMembership = f.membership.copy(version      = f.membership.version + 1,
                                                  studyData    = newStudyData,
                                                  timeModified = Some(OffsetDateTime.now))
        reply must matchUpdatedMembership(updatedMembership)
      }

      it("cannot remove a study not in the membership") {
        val f = new MembershipFixture
        val study = factory.createEnabledStudy
        studyRepository.put(study)
        val url = uri("memberships") + s"/study/${f.membership.id}/${f.membership.version}/${study.id.id}"
        val reply = makeAuthRequest(DELETE, url).value
        reply must beBadRequestWithMessage("study ID is not in membership")
      }

      it("cannot remove a study in an all studies membership") {
        val f = new MembershipFixtureAllStudies
        val study = factory.createEnabledStudy
        studyRepository.put(study)
        val url = uri("memberships") + s"/study/${f.membership.id}/${f.membership.version}/${study.id.id}"
        val reply = makeAuthRequest(DELETE, url).value
        reply must beBadRequestWithMessage("membership is for all studies, cannot remove:")
      }

      it("cannot add a study that does not exist") {
        val f = new MembershipFixture
        val study = factory.createEnabledStudy
        val url = uri("memberships") + s"/study/${f.membership.id}/${f.membership.version}/${study.id.id}"
        val reply = makeAuthRequest(DELETE, url).value
        reply must beNotFoundWithMessage("IdNotFound: study id")
      }

      it("fail when removing and membership ID does not exist") {
        val f = new MembershipFixture
        val study = factory.createEnabledStudy
        val url = uri("memberships") + s"/study/${f.membership.id}/${f.membership.version}/${study.id.id}"
        studyRepository.put(study)
        membershipRepository.remove(f.membership)
        val reply = makeAuthRequest(DELETE, url)
        reply.value must beNotFoundWithMessage("IdNotFound.*membership")
      }

      it("fail when removing with invalid version") {
        val f = new MembershipFixture
        val study = factory.createEnabledStudy
        val url = uri("memberships") + s"/study/${f.membership.id}/${f.membership.version + 10L}/${study.id.id}"
        studyRepository.put(study)
        val reply = makeAuthRequest(DELETE, url).value
        reply must beBadRequestWithMessage("expected version doesn't match current version")

      }

    }

    describe("DELETE /api/access/memberships/centre/:membershipId/:version/:centreId") {

      it("can remove a centre") {
        val f = new MembershipFixture
        val url = uri("memberships") + s"/centre/${f.membership.id}/${f.membership.version}/${f.centre.id.id}"
        val reply = makeAuthRequest(DELETE, url).value
        reply must beOkResponseWithJsonReply

        val newCentreData = f.membership.centreData.copy(ids = f.membership.centreData.ids - f.centre.id)
        val updatedMembership = f.membership.copy(version      = f.membership.version + 1,
                                                  centreData   = newCentreData,
                                                  timeModified = Some(OffsetDateTime.now))
        reply must matchUpdatedMembership(updatedMembership)
      }

      it("cannot remove a centre not in the membership") {
        val f = new MembershipFixture
        val centre = factory.createEnabledCentre
        centreRepository.put(centre)
        val url = uri("memberships") + s"/centre/${f.membership.id}/${f.membership.version}/${centre.id.id}"
        val reply = makeAuthRequest(DELETE, url).value
        reply must beBadRequestWithMessage("centre ID is not in membership")
      }

      it("cannot remove a centre in an all centres membership") {
        val f = new MembershipFixtureAllCentres
        val centre = factory.createEnabledCentre
        centreRepository.put(centre)
        val url = uri("memberships") + s"/centre/${f.membership.id}/${f.membership.version}/${centre.id.id}"
        val reply = makeAuthRequest(DELETE, url).value
        reply must beBadRequestWithMessage("membership is for all centres, cannot remove:")
      }

      it("cannot add a centre that does not exist") {
        val f = new MembershipFixture
        val centre = factory.createEnabledCentre
        val url = uri("memberships") + s"/centre/${f.membership.id}/${f.membership.version}/${centre.id.id}"
        val reply = makeAuthRequest(DELETE, url).value
        reply must beNotFoundWithMessage("IdNotFound: centre id")
      }

      it("fail when removing and membership ID does not exist") {
        val f = new MembershipFixture
        val centre = factory.createEnabledCentre
        val url = uri("memberships") + s"/centre/${f.membership.id}/${f.membership.version}/${centre.id.id}"
        centreRepository.put(centre)
        membershipRepository.remove(f.membership)
        val reply = makeAuthRequest(DELETE, url)
        reply.value must beNotFoundWithMessage("IdNotFound.*membership")
      }

      it("fail when removing with invalid version") {
        val f = new MembershipFixture
        val centre = factory.createEnabledCentre
        val url = uri("memberships") + s"/centre/${f.membership.id}/${f.membership.version + 10L}/${centre.id.id}"
        centreRepository.put(centre)
        val reply = makeAuthRequest(DELETE, url).value
        reply must beBadRequestWithMessage("expected version doesn't match current version")
      }

    }

    describe("DELETE /api/access/memberships/:membershipId/:version") {

      it("can remove a membership") {
        val f = new MembershipFixture
        val url = uri("memberships") + s"/${f.membership.id}/${f.membership.version}"
        val reply = makeAuthRequest(DELETE, url).value
        reply must beOkResponseWithJsonReply

        val result = (contentAsJson(reply) \ "data").validate[Boolean]
        result must be (jsSuccess)
        result.get must be (true)
      }

      it("cannot remove a membership that does not exist") {
        val f = new MembershipFixture
        val url = uri("memberships") + s"/${f.membership.id}/${f.membership.version}"
        membershipRepository.remove(f.membership)
        val reply = makeAuthRequest(DELETE, url)
        reply.value must beNotFoundWithMessage("IdNotFound.*membership")
      }

      it("fail when removing with invalid version") {
        val f = new MembershipFixture
        val url = uri("memberships") + s"/${f.membership.id}/${f.membership.version + 10L}"
        val reply = makeAuthRequest(DELETE, url).value
        reply must beBadRequestWithMessage("expected version doesn't match current version")
      }

    }

  }

  private def listSingleMembership(offset:    Long = 0,
                                   maybeNext: Option[Int] = None,
                                   maybePrev: Option[Int] = None)
                                  (setupFunc: () => (Url, Membership)) = {

    it("list single membership") {
      val (url, expectedMembership) = setupFunc()
      val reply = makeAuthRequest(GET, url.path).value
      reply must beOkResponseWithJsonReply

      val reqJson = contentAsJson(reply)
      reqJson must beSingleItemResults(offset, maybeNext, maybePrev)

      val replyMemberships = (reqJson \ "data" \ "items").validate[List[MembershipDto]]
      replyMemberships must be (jsSuccess)
      replyMemberships.get.foreach { _ must matchDtoToMembership(expectedMembership) }
    }
  }

  private def listMultipleMemberships(offset:    Long = 0,
                                     maybeNext: Option[Int] = None,
                                     maybePrev: Option[Int] = None)
                                    (setupFunc: () => (Url, List[Membership])) = {

    it("list multiple memberships") {
      val (url, expectedMemberships) = setupFunc()

      val reply = makeAuthRequest(GET, url.path).value
      reply must beOkResponseWithJsonReply

      val reqJson = contentAsJson(reply)
      reqJson must beMultipleItemResults(offset = offset,
                                      total = expectedMemberships.size.toLong,
                                      maybeNext = maybeNext,
                                      maybePrev = maybePrev)

      val replyMemberships = (reqJson \ "data" \ "items").validate[List[MembershipDto]]
      replyMemberships must be (jsSuccess)

      (replyMemberships.get zip expectedMemberships).foreach { case (replyMembership, membership) =>
        replyMembership must matchDtoToMembership(membership)
      }
    }

  }

  private def matchUpdatedMembership(membership: Membership) =
    new Matcher[Future[Result]] {
      def apply (left: Future[Result]) = {
        val replyMembership = (contentAsJson(left) \ "data").validate[MembershipDto]
        val jsSuccessMatcher = jsSuccess(replyMembership)

        if (!jsSuccessMatcher.matches) {
          jsSuccessMatcher
        } else {
          val replyMatcher = matchDtoToMembership(membership)(replyMembership.get)

          if (!replyMatcher.matches) {
            MatchResult(false,
                        s"reply does not match expected: ${replyMatcher.failureMessage}",
                        s"reply matches expected: ${replyMatcher.failureMessage}")
          } else {
            matchRepositoryMembership(membership)
          }
        }
      }
    }

  private def matchRepositoryMembership =
    new Matcher[Membership] {
      def apply (left: Membership) = {
        membershipRepository.getByKey(left.id).fold(
          err => {
            MatchResult(false, s"not found in repository: ${err.head}", "")

          },
          repoCet => {
            val repoMatcher = matchMembership(left)(repoCet)
            MatchResult(repoMatcher.matches,
                        s"repository membership does not match expected: ${repoMatcher.failureMessage}",
                        s"repository membership matches expected: ${repoMatcher.failureMessage}")
          }
        )
      }
    }

  private def membershipToAddJson(membership: Membership) = {
    Json.obj("name"        -> membership.name,
             "userIds"     -> membership.userIds.map(_.toString),
             "allStudies"  -> membership.studyData.allEntities,
             "studyIds"    -> membership.studyData.ids.map(_.toString),
             "allCentres"  -> membership.centreData.allEntities,
             "centreIds"   -> membership.centreData.ids.map(_.toString)) ++
    JsObject(
          Seq[(String, JsValue)]() ++
            membership.description.map("description" -> Json.toJson(_)))

  }

}
