package org.biobank.controllers.access

import java.time.OffsetDateTime
import org.biobank.controllers.PagedResultsSpec
import org.biobank.domain._
import org.biobank.domain.access._
import org.biobank.domain.centre._
import org.biobank.domain.study._
import org.biobank.domain.user._
import org.biobank.fixture.ControllerFixture
import org.scalatest.prop.TableDrivenPropertyChecks._
import play.api.libs.json._
import play.api.test.Helpers._
import org.scalatest.Inside

/**
 * Tests the memberships REST API for [[User Access]].
 *
 * Tests for [[Role]]s and [[Permissions]] in AccessControllerSpec.scala.
 */
class AccessControllerMembershipSpec
    extends ControllerFixture
    with AccessControllerSpecCommon
    with JsonHelper
    with UserFixtures
    with Inside {
  import org.biobank.TestUtils._

  class MembershipFixture {
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

  class MembershipFixtureAllStudies {
    val user       = factory.createActiveUser

    private val _membership = factory.createMembership
    private val studyData  = _membership.studyData.hasAllEntities

    val membership = _membership.copy(userIds   = Set(user.id),
                                      studyData = studyData)

    Set(user, membership).foreach(addToRepository)
  }

  class MembershipFixtureAllCentres {
    val user       = factory.createActiveUser

    private val _membership = factory.createMembership
    private val centreData  = _membership.centreData.hasAllEntities

    val membership = _membership.copy(userIds    = Set(user.id),
                                      centreData = centreData)

    Set(user, membership).foreach(addToRepository)
  }

  describe("Access REST API (memberships)") {

    describe("GET /api/access/membership") {

      val DefaultMembershipName = "All studies and all centres"

      def compareObjs(jsonList: List[JsObject], memberships: List[Membership]) = {
        val membershipsMap = memberships.map { membership => (membership.id, membership) }.toMap
        jsonList.foreach { jsonObj =>
          val jsonId = MembershipId((jsonObj \ "id").as[String])
          compareObj(jsonObj, membershipsMap(jsonId))
        }
      }

      def filterOutDefaultMembership(jsonList: List[JsObject]): List[JsObject] = {
        jsonList.filter(json => (json \ "name").as[String] != "All studies and all centres")
      }

      def multipleItemsResultWithDefaultMembership(uri:         String,
                                                   queryParams: Map[String, String] =  Map.empty,
                                                   offset:      Long,
                                                   total:       Long,
                                                   maybeNext:   Option[Int],
                                                   maybePrev:   Option[Int]) = {
        val reply = PagedResultsSpec(this).multipleItemsResult(uri,
                                                               queryParams,
                                                               offset,
                                                               total + 1, // +1 for the default membership
                                                               maybeNext,
                                                               maybePrev)
        filterOutDefaultMembership(reply)
      }

      it("lists the default membership") {
        val jsonItem = PagedResultsSpec(this).singleItemResult(uri("memberships"))

        (jsonItem \ "name").as[String] must be (DefaultMembershipName)
      }

      it("list multiple memberships") {
        val memberships = (0 until 2).map(_ => factory.createMembership).toList
        memberships.foreach(membershipRepository.put)

        val jsonItems = multipleItemsResultWithDefaultMembership(uri       = uri("memberships"),
                                                                 offset    = 0,
                                                                 total     = memberships.size.toLong,
                                                                 maybeNext = None,
                                                                 maybePrev = None)
        jsonItems must have size memberships.size.toLong
        compareObjs(jsonItems, memberships)
      }

      it("list a single membership when filtered by name") {
        val memberships = List(factory.createMembership.copy(name = "membership1"),
                               factory.createMembership.copy(name = "membership2"))
        val membership = memberships(0)
        memberships.foreach(membershipRepository.put)
        val jsonItem = PagedResultsSpec(this)
          .singleItemResult(uri("memberships"), Map("filter" -> s"name::${membership.name}"))
        compareObj(jsonItem, memberships(0))
      }

      it("list memberships sorted by name") {
        val memberships = List(factory.createMembership.copy(name = "membership3"),
                               factory.createMembership.copy(name = "membership2"),
                               factory.createMembership.copy(name = "membership1"))
        memberships.foreach(membershipRepository.put)

        val sortExprs = Table("sort by", "name", "-name")
        forAll(sortExprs) { sortExpr =>
          val jsonItems = multipleItemsResultWithDefaultMembership(uri         = uri("memberships"),
                                                                   queryParams = Map("sort" -> sortExpr),
                                                                   offset      = 0,
                                                                   total       = memberships.size.toLong,
                                                                   maybeNext   = None,
                                                                   maybePrev   = None)

          jsonItems must have size memberships.size.toLong
          if (sortExpr == sortExprs(0)) {
            compareObj(jsonItems(0), memberships(2))
            compareObj(jsonItems(1), memberships(1))
            compareObj(jsonItems(2), memberships(0))
          } else {
            compareObj(jsonItems(0), memberships(0))
            compareObj(jsonItems(1), memberships(1))
            compareObj(jsonItems(2), memberships(2))
          }
        }
      }

      it("list a single membership when using paged query") {
        val memberships = List(factory.createMembership.copy(name = "membership3"),
                               factory.createMembership.copy(name = "membership2"),
                               factory.createMembership.copy(name = "membership1"))
        memberships.foreach(membershipRepository.put)

        val jsonItem = PagedResultsSpec(this).singleItemResult(
            uri         = uri("memberships"),
            queryParams = Map("filter" -> "name:like:membership",
                              "sort"   -> "name",
                              "limit" -> "1"),
            total       = memberships.size.toLong,
            maybeNext   = Some(2))

        compareObj(jsonItem, memberships(2))
      }

      it("fail when using an invalid query parameters") {
        PagedResultsSpec(this).failWithInvalidParams(uri("memberships"))
      }

    }

    describe("GET /api/access/membership/:slug") {

      it("returns a membership") {
        val f = new MembershipFixture
        val reply = makeRequest(GET, uri("memberships", f.membership.slug))

        (reply \ "status").as[String] must be ("success")

        val jsonObj = (reply \ "data").as[JsObject]
        compareObj(jsonObj, f.membership)
      }

      it("fails for an invalid membership") {
        val f = new MembershipFixture
        membershipRepository.remove(f.membership)
        val reply = makeRequest(GET, uri("memberships", f.membership.slug), NOT_FOUND)

        (reply \ "status").as[String] must be ("error")

        (reply \ "message").as[String] must include regex("EntityCriteriaNotFound: membership slug")
      }

    }

    describe("POST /api/access/memberships") {

      it("can create a membership") {
        val f = new MembershipFixture
        membershipRepository.remove(f.membership)
        val json = Json.obj("name"        -> f.membership.name,
                            "description" -> f.membership.description,
                            "userIds"     -> f.membership.userIds.map(_.toString),
                            "allStudies"  -> false,
                            "studyIds"    -> f.membership.studyData.ids.map(_.toString),
                            "allCentres"  -> false,
                            "centreIds"   -> f.membership.centreData.ids.map(_.toString))

        val reply = makeRequest(POST, uri("memberships"), json)

        (reply \ "status").as[String] must include ("success")

        val jsonId = (reply \ "data" \ "id").as[String]
        val membershipId = MembershipId(jsonId)
        jsonId.length must be > 0

        membershipRepository.getByKey(membershipId) mustSucceed { repoMembership =>
          compareObj((reply \ "data").as[JsObject], repoMembership)
          repoMembership must have (
            'id             (membershipId),
            'version        (0L)
          )
          repoMembership.userIds must contain (f.user.id)
          checkTimeStamps(repoMembership, OffsetDateTime.now, None)
        }
      }

      it("fails when adding a second membership with a name that already exists") {
        val f = new MembershipFixture
        val json = Json.obj("name"       -> f.membership.name,
                            "userIds"    -> f.membership.userIds.map(_.toString),
                            "allStudies" -> false,
                            "studyIds"   -> f.membership.studyData.ids.map(_.toString),
                            "allCentres" -> false,
                            "centreIds"  -> f.membership.centreData.ids.map(_.toString))

        val reply = makeRequest(POST, uri("memberships"), BAD_REQUEST, json)

        (reply \ "status").as[String] must include ("error")

        (reply \ "message").as[String] must include regex("EntityCriteriaError: name already used:")
      }

      it("attempt to create membership fails if no users are added") {
        val f = new MembershipFixture
        val json = Json.obj("name"       -> f.membership.name,
                            "userIds"    -> List.empty[String],
                            "allStudies" -> false,
                            "studyIds"   -> f.membership.studyData.ids.map(_.toString),
                            "allCentres" -> false,
                            "centreIds"  -> f.membership.centreData.ids.map(_.toString))

        val reply = makeRequest(POST, uri("memberships"), BAD_REQUEST, json)

        (reply \ "status").as[String] must include ("error")

        (reply \ "message").as[String] must include regex("userIds cannot be empty")
      }

      it("attempt to create membership fails if user does not exist") {
        val f = new MembershipFixture
        userRepository.remove(f.user)
        val json = Json.obj("name"       -> f.membership.name,
                            "userIds"    -> f.membership.userIds.map(_.toString),
                            "allStudies" -> false,
                            "studyIds"   -> f.membership.studyData.ids.map(_.toString),
                            "allCentres" -> false,
                            "centreIds"  -> f.membership.centreData.ids.map(_.toString))

        val reply = makeRequest(POST, uri("memberships"), NOT_FOUND, json)

        (reply \ "status").as[String] must include ("error")

        (reply \ "message").as[String] must include regex("IdNotFound: user id")
      }

      it("attempt to create membership fails if study does not exist") {
        val f = new MembershipFixture
        studyRepository.remove(f.study)
        val json = Json.obj("name"       -> f.membership.name,
                            "userIds"    -> f.membership.userIds.map(_.toString),
                            "allStudies" -> false,
                            "studyIds"   -> f.membership.studyData.ids.map(_.toString),
                            "allCentres" -> false,
                            "centreIds"  -> f.membership.centreData.ids.map(_.toString))

        val reply = makeRequest(POST, uri("memberships"), NOT_FOUND, json)

        (reply \ "status").as[String] must include ("error")

        (reply \ "message").as[String] must include regex("IdNotFound: study id")
      }

      it("attempt to create membership fails if centre does not exist") {
        val f = new MembershipFixture
        centreRepository.remove(f.centre)
        val json = Json.obj("name"       -> f.membership.name,
                            "userIds"    -> f.membership.userIds.map(_.toString),
                            "allStudies" -> false,
                            "studyIds"   -> f.membership.studyData.ids.map(_.toString),
                            "allCentres" -> false,
                            "centreIds"  -> f.membership.centreData.ids.map(_.toString))

        val reply = makeRequest(POST, uri("memberships"), NOT_FOUND, json)

        (reply \ "status").as[String] must include ("error")

        (reply \ "message").as[String] must include regex("IdNotFound: centre id")
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

        val reply = makeRequest(POST, uri("memberships", "name", f.membership.id.id), json)

        (reply \ "status").as[String] must include ("success")

        val jsonId = (reply \ "data" \ "id").as[String]
        val membershipId = MembershipId(jsonId)
        jsonId.length must be > 0

        membershipRepository.getByKey(membershipId) mustSucceed { repoMembership =>
          compareObj((reply \ "data").as[JsObject], repoMembership)
          repoMembership must have (
            'id             (membershipId),
            'version        (f.membership.version + 1),
            'name           (newName)
          )
          checkTimeStamps(repoMembership, OffsetDateTime.now, OffsetDateTime.now)
        }
      }

      it("fails when updating to name already used by another membership") {
        val f = new MembershipFixture
        val membership = factory.createMembership
        membershipRepository.put(membership)
        val json = updateNameJson(f.membership, membership.name)

        val reply = makeRequest(POST, uri("memberships") + s"/name/${f.membership.id}", BAD_REQUEST, json)

        (reply \ "status").as[String] must include ("error")

        (reply \ "message").as[String] must include regex("EntityCriteriaError: name already used:")
      }

      it("fail when updating to something with less than 2 characters") {
        val f = new MembershipFixture
        val json = updateNameJson(f.membership, "a")
        val reply = makeRequest(POST, uri("memberships") + s"/name/${f.membership.id}", BAD_REQUEST, json)

        (reply \ "status").as[String] must include ("error")

        (reply \ "message").as[String] must startWith ("InvalidName")
      }

      it("fail when updating and membership ID does not exist") {
        val f = new MembershipFixture
        membershipRepository.remove(f.membership)
        notFound(uri("memberships") + s"/name/${f.membership.id}",
                 updateNameJson(f.membership, nameGenerator.next[Membership]))
      }

      it("fail when updating with invalid version") {
        val f = new MembershipFixture
        val json = updateNameJson(f.membership, nameGenerator.next[Membership]) ++
          Json.obj("expectedVersion" -> Some(f.membership.version + 10L))
        hasInvalidVersion(POST, uri("memberships") + s"/name/${f.membership.id}", json)
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
          val json = updateDescriptionJson(f.membership, newDescription)
          val reply = makeRequest(POST, uri("memberships") + s"/description/${f.membership.id}", json)

          (reply \ "status").as[String] must include ("success")

          val jsonId = (reply \ "data" \ "id").as[String]
          val membershipId = MembershipId(jsonId)
          jsonId.length must be > 0

          membershipRepository.getByKey(membershipId) mustSucceed { repoMembership =>
            compareObj((reply \ "data").as[JsObject], repoMembership)
            repoMembership must have (
              'id             (membershipId),
              'version        (f.membership.version + 1),
              'description    (newDescription)
            )
            checkTimeStamps(repoMembership, OffsetDateTime.now, OffsetDateTime.now)
          }
        }
      }

      it("fail when updating and membership ID does not exist") {
        val f = new MembershipFixture
        membershipRepository.remove(f.membership)
        notFound(uri("memberships") + s"/description/${f.membership.id}",
                 updateDescriptionJson(f.membership, Some(nameGenerator.next[Membership])))
      }

      it("fail when updating with invalid version") {
        val f = new MembershipFixture
        val json = updateDescriptionJson(f.membership, Some(nameGenerator.next[Membership])) ++
          Json.obj("expectedVersion" -> Some(f.membership.version + 10L))
        hasInvalidVersion(POST, uri("memberships") + s"/description/${f.membership.id}", json)
      }

    }

    describe("POST /api/access/memberships/user/:membershipId") {

      def addUserJson(membership: Membership, user: User) = {
        Json.obj("userId" -> user.id.id, "expectedVersion" -> membership.version)
      }

      it("can add a user") {
        val f = new MembershipFixture
        val user = factory.createRegisteredUser
        val json = addUserJson(f.membership, user)

        userRepository.put(user)
        val reply = makeRequest(POST, uri("memberships") + s"/user/${f.membership.id}", json)

        (reply \ "status").as[String] must include ("success")

        val jsonId = (reply \ "data" \ "id").as[String]
        val membershipId = MembershipId(jsonId)
        jsonId.length must be > 0

        membershipRepository.getByKey(membershipId) mustSucceed { repoMembership =>
          compareObj((reply \ "data").as[JsObject], repoMembership)
          repoMembership must have (
            'id             (membershipId),
            'version        (f.membership.version + 1)
          )
          repoMembership.userIds must contain (user.id)
          checkTimeStamps(repoMembership, OffsetDateTime.now, OffsetDateTime.now)
        }
      }

      it("cannot add the same user more than once") {
        val f = new MembershipFixture
        val json = addUserJson(f.membership, f.user)
        val reply = makeRequest(POST, uri("memberships") + s"/user/${f.membership.id}", BAD_REQUEST, json)

        (reply \ "status").as[String] must include ("error")

        (reply \ "message").as[String] must include ("user ID is already in membership")
      }

      it("cannot add a user that does not exist") {
        val f = new MembershipFixture
        val user = factory.createRegisteredUser
        val json = addUserJson(f.membership, user)
        val reply = makeRequest(POST, uri("memberships") + s"/user/${f.membership.id}", NOT_FOUND, json)

        (reply \ "status").as[String] must include ("error")

        (reply \ "message").as[String] must include ("IdNotFound: user id")
      }

      it("fail when updating and membership ID does not exist") {
        val f = new MembershipFixture
        val user = factory.createRegisteredUser
        val json = addUserJson(f.membership, user)
        userRepository.put(user)
        membershipRepository.remove(f.membership)
        notFound(uri("memberships") + s"/user/${f.membership.id}", json)
      }

      it("fail when updating with invalid version") {
        val f = new MembershipFixture
        val user = factory.createRegisteredUser
        val json = addUserJson(f.membership, user) ++
          Json.obj("expectedVersion" -> Some(f.membership.version + 10L))
        userRepository.put(user)
        hasInvalidVersion(POST, uri("memberships") + s"/user/${f.membership.id}", json)
      }

    }

    describe("POST /api/access/memberships/allStudies/:membershipId") {

      it("can assign a membership, for a single study, to be for all studies") {
        val f = new MembershipFixture

        f.membership.centreData.allEntities must be (false)
        f.membership.studyData.ids must not have size (0L)

        val json = Json.obj("expectedVersion" -> f.membership.version)
        val reply = makeRequest(POST, uri("memberships") + s"/allStudies/${f.membership.id}", json)

        (reply \ "status").as[String] must include ("success")

        val jsonId = (reply \ "data" \ "id").as[String]
        val membershipId = MembershipId(jsonId)
        jsonId.length must be > 0

        membershipRepository.getByKey(membershipId) mustSucceed { repoMembership =>
          compareObj((reply \ "data").as[JsObject], repoMembership)
          repoMembership must have (
            'id             (membershipId),
            'version        (f.membership.version + 1)
          )
          repoMembership.studyData.allEntities must be (true)
          repoMembership.studyData.ids must have size (0L)
          checkTimeStamps(repoMembership, OffsetDateTime.now, OffsetDateTime.now)
        }
      }

    }

    describe("POST /api/access/memberships/study/:membershipId") {

      def addStudyJson(membership: Membership, study: Study) = {
        Json.obj("studyId" -> study.id.id, "expectedVersion" -> membership.version)
      }

      def validateReply(json:         JsValue,
                        membershipId: MembershipId,
                        newVersion:   Long,
                        studyId:      StudyId) {
        val jsonId = (json \ "id").as[String]
        jsonId must be (membershipId.id)

        membershipRepository.getByKey(membershipId) mustSucceed { repoMembership =>
          compareObj(json, repoMembership)
          repoMembership must have (
            'id             (membershipId),
            'version        (newVersion)
          )
          repoMembership.studyData.allEntities must be (false)
          repoMembership.studyData.ids must contain (studyId)
          checkTimeStamps(repoMembership, OffsetDateTime.now, OffsetDateTime.now)
        }
      }

      it("can add a study") {
        val f = new MembershipFixture
        val study = factory.createEnabledStudy
        val json = addStudyJson(f.membership, study)

        studyRepository.put(study)
        val reply = makeRequest(POST, uri("memberships") + s"/study/${f.membership.id}", json)

        (reply \ "status").as[String] must include ("success")

        validateReply((reply \ "data").as[JsObject], f.membership.id, f.membership.version + 1, study.id)
      }

      it("an all studies membership can be modified to be for a single study") {
        val f = new MembershipFixtureAllStudies
        val study = factory.createEnabledStudy
        val json = addStudyJson(f.membership, study)

        studyRepository.put(study)
        val reply = makeRequest(POST, uri("memberships") + s"/study/${f.membership.id}", json)

        (reply \ "status").as[String] must include ("success")

        validateReply((reply \ "data").as[JsObject], f.membership.id, f.membership.version + 1, study.id)
      }

      it("cannot add the same study more than once") {
        val f = new MembershipFixture
        val json = addStudyJson(f.membership, f.study)
        val reply = makeRequest(POST, uri("memberships") + s"/study/${f.membership.id}", BAD_REQUEST, json)

        (reply \ "status").as[String] must include ("error")

        (reply \ "message").as[String] must include ("study ID is already in membership")
      }

      it("cannot add a study that does not exist") {
        val f = new MembershipFixture
        val study = factory.createEnabledStudy
        val json = addStudyJson(f.membership, study)
        val reply = makeRequest(POST, uri("memberships") + s"/study/${f.membership.id}", NOT_FOUND, json)

        (reply \ "status").as[String] must include ("error")

        (reply \ "message").as[String] must include ("IdNotFound: study id")
      }

      it("fail when updating and membership ID does not exist") {
        val f = new MembershipFixture
        val study = factory.createEnabledStudy
        val json = addStudyJson(f.membership, study)
        studyRepository.put(study)
        membershipRepository.remove(f.membership)
        notFound(uri("memberships") + s"/study/${f.membership.id}", json)
      }

      it("fail when updating with invalid version") {
        val f = new MembershipFixture
        val study = factory.createEnabledStudy
        val json = addStudyJson(f.membership, study) ++
          Json.obj("expectedVersion" -> Some(f.membership.version + 10L))
        studyRepository.put(study)
        hasInvalidVersion(POST, uri("memberships") + s"/study/${f.membership.id}", json)
      }

    }

    describe("POST /api/access/memberships/allCentres/:membershipId") {

      it("can assign a membership, for a single centre, to be for all centres") {
        val f = new MembershipFixture

        f.membership.centreData.allEntities must be (false)
        f.membership.centreData.ids must not have size (0L)

        val json = Json.obj("expectedVersion" -> f.membership.version)
        val reply = makeRequest(POST, uri("memberships") + s"/allCentres/${f.membership.id}", json)

        (reply \ "status").as[String] must include ("success")

        val jsonId = (reply \ "data" \ "id").as[String]
        val membershipId = MembershipId(jsonId)
        jsonId.length must be > 0

        membershipRepository.getByKey(membershipId) mustSucceed { repoMembership =>
          compareObj((reply \ "data").as[JsObject], repoMembership)
          repoMembership must have (
            'id             (membershipId),
            'version        (f.membership.version + 1)
          )
          repoMembership.centreData.allEntities must be (true)
          repoMembership.centreData.ids must have size (0L)
          checkTimeStamps(repoMembership, OffsetDateTime.now, OffsetDateTime.now)
        }
      }

    }

    describe("POST /api/access/memberships/centre/:membershipId") {

      def addCentreJson(membership: Membership, centre: Centre) = {
        Json.obj("centreId" -> centre.id.id, "expectedVersion" -> membership.version)
      }

      def validateReply(json:         JsValue,
                        membershipId: MembershipId,
                        newVersion:   Long,
                        centreId:     CentreId) {
        val jsonId = (json \ "id").as[String]
        jsonId must be (membershipId.id)

        membershipRepository.getByKey(membershipId) mustSucceed { repoMembership =>
          compareObj(json, repoMembership)
          repoMembership must have (
            'id      (membershipId),
            'version (newVersion)
          )
          repoMembership.centreData.allEntities must be (false)
          repoMembership.centreData.ids must contain (centreId)
          checkTimeStamps(repoMembership, OffsetDateTime.now, OffsetDateTime.now)
        }
      }

      it("can add a centre") {
        val f = new MembershipFixture
        val centre = factory.createEnabledCentre
        val json = addCentreJson(f.membership, centre)

        centreRepository.put(centre)
        val reply = makeRequest(POST, uri("memberships") + s"/centre/${f.membership.id}", json)

        (reply \ "status").as[String] must include ("success")

        validateReply((reply \ "data").as[JsObject], f.membership.id, f.membership.version + 1, centre.id)
      }

      it("an all centres membership can be modified to be for a single centre") {
        val f = new MembershipFixtureAllCentres
        val centre = factory.createEnabledCentre
        val json = addCentreJson(f.membership, centre)

        centreRepository.put(centre)
        val reply = makeRequest(POST, uri("memberships") + s"/centre/${f.membership.id}", json)

        (reply \ "status").as[String] must include ("success")

        validateReply((reply \ "data").as[JsObject], f.membership.id, f.membership.version + 1, centre.id)
      }

      it("cannot add the same centre more than once") {
        val f = new MembershipFixture
        val json = addCentreJson(f.membership, f.centre)
        val reply = makeRequest(POST, uri("memberships") + s"/centre/${f.membership.id}", BAD_REQUEST, json)

        (reply \ "status").as[String] must include ("error")

        (reply \ "message").as[String] must include ("centre ID is already in membership")
      }

      it("cannot add a centre that does not exist") {
        val f = new MembershipFixture
        val centre = factory.createEnabledCentre
        val json = addCentreJson(f.membership, centre)
        val reply = makeRequest(POST, uri("memberships") + s"/centre/${f.membership.id}", NOT_FOUND, json)

        (reply \ "status").as[String] must include ("error")

        (reply \ "message").as[String] must include ("IdNotFound: centre id")
      }

      it("fail when updating and membership ID does not exist") {
        val f = new MembershipFixture
        val centre = factory.createEnabledCentre
        val json = addCentreJson(f.membership, centre)
        centreRepository.put(centre)
        membershipRepository.remove(f.membership)
        notFound(uri("memberships") + s"/centre/${f.membership.id}", json)
      }

      it("fail when updating with invalid version") {
        val f = new MembershipFixture
        val centre = factory.createEnabledCentre
        val json = addCentreJson(f.membership, centre) ++
          Json.obj("expectedVersion" -> Some(f.membership.version + 10L))
        centreRepository.put(centre)
        hasInvalidVersion(POST, uri("memberships") + s"/centre/${f.membership.id}", json)
      }

    }

    describe("DELETE /api/access/memberships/user/:membershipId/:version/:userId") {

      it("can remove a user") {
        val f = new MembershipFixture
        val url = uri("memberships") + s"/user/${f.membership.id}/${f.membership.version}/${f.user.id.id}"
        val reply = makeRequest(DELETE, url)

        (reply \ "status").as[String] must include ("success")

        val jsonId = (reply \ "data" \ "id").as[String]
        val membershipId = MembershipId(jsonId)
        jsonId.length must be > 0

        membershipRepository.getByKey(membershipId) mustSucceed { repoMembership =>
          compareObj((reply \ "data").as[JsObject], repoMembership)
          repoMembership must have (
            'id             (membershipId),
            'version        (f.membership.version + 1)
          )
          repoMembership.userIds must not contain (f.user.id)
          checkTimeStamps(repoMembership, f.membership.timeAdded, OffsetDateTime.now)
        }
      }

      it("cannot remove a user not in the membership") {
        val f = new MembershipFixture
        val user = factory.createRegisteredUser
        userRepository.put(user)
        val url = uri("memberships") + s"/user/${f.membership.id}/${f.membership.version}/${user.id.id}"
        val reply = makeRequest(DELETE, url, BAD_REQUEST)

        (reply \ "status").as[String] must include ("error")

        (reply \ "message").as[String] must include ("user ID is not in membership")
      }

      it("cannot remove a user that does not exist") {
        val f = new MembershipFixture
        val user = factory.createRegisteredUser
        val url = uri("memberships") + s"/user/${f.membership.id}/${f.membership.version}/${user.id.id}"
        val reply = makeRequest(DELETE, url, NOT_FOUND)

        (reply \ "status").as[String] must include ("error")

        (reply \ "message").as[String] must include ("IdNotFound: user id")
      }

      it("fail when removing and membership ID does not exist") {
        val f = new MembershipFixture
        val user = factory.createRegisteredUser
        val url = uri("memberships") + s"/user/${f.membership.id}/${f.membership.version}/${user.id.id}"
        userRepository.put(user)
        membershipRepository.remove(f.membership)
        notFoundOnDelete(url)
      }

      it("fail when removing with invalid version") {
        val f = new MembershipFixture
        val user = factory.createRegisteredUser
        val url = uri("memberships") + s"/user/${f.membership.id}/${f.membership.version + 10L}/${user.id.id}"
        userRepository.put(user)
        hasInvalidVersion(DELETE, url)
      }

    }

    describe("DELETE /api/access/memberships/study/:membershipId/:version/:studyId") {

      it("can remove a study") {
        val f = new MembershipFixture
        val url = uri("memberships") + s"/study/${f.membership.id}/${f.membership.version}/${f.study.id.id}"
        val reply = makeRequest(DELETE, url)

        (reply \ "status").as[String] must include ("success")

        val jsonId = (reply \ "data" \ "id").as[String]
        val membershipId = MembershipId(jsonId)
        jsonId.length must be > 0

        membershipRepository.getByKey(membershipId) mustSucceed { repoMembership =>
          compareObj((reply \ "data").as[JsObject], repoMembership)
          repoMembership must have (
            'id             (membershipId),
            'version        (f.membership.version + 1)
          )
          repoMembership.studyData.ids must not contain (f.study.id)
          checkTimeStamps(repoMembership, f.membership.timeAdded, OffsetDateTime.now)
        }
      }

      it("cannot remove a study not in the membership") {
        val f = new MembershipFixture
        val study = factory.createEnabledStudy
        studyRepository.put(study)
        val url = uri("memberships") + s"/study/${f.membership.id}/${f.membership.version}/${study.id.id}"
        val reply = makeRequest(DELETE, url, BAD_REQUEST)

        (reply \ "status").as[String] must include ("error")

        (reply \ "message").as[String] must include ("study ID is not in membership")
      }

      it("cannot remove a study in an all studies membership") {
        val f = new MembershipFixtureAllStudies
        val study = factory.createEnabledStudy
        studyRepository.put(study)
        val url = uri("memberships") + s"/study/${f.membership.id}/${f.membership.version}/${study.id.id}"
        val reply = makeRequest(DELETE, url, BAD_REQUEST)

        (reply \ "status").as[String] must include ("error")

        (reply \ "message").as[String] must include ("membership is for all studies, cannot remove:")
      }

      it("cannot add a study that does not exist") {
        val f = new MembershipFixture
        val study = factory.createEnabledStudy
        val url = uri("memberships") + s"/study/${f.membership.id}/${f.membership.version}/${study.id.id}"
        val reply = makeRequest(DELETE, url, NOT_FOUND)

        (reply \ "status").as[String] must include ("error")

        (reply \ "message").as[String] must include ("IdNotFound: study id")
      }

      it("fail when removing and membership ID does not exist") {
        val f = new MembershipFixture
        val study = factory.createEnabledStudy
        val url = uri("memberships") + s"/study/${f.membership.id}/${f.membership.version}/${study.id.id}"
        studyRepository.put(study)
        membershipRepository.remove(f.membership)
        notFoundOnDelete(url)
      }

      it("fail when removing with invalid version") {
        val f = new MembershipFixture
        val study = factory.createEnabledStudy
        val url = uri("memberships") + s"/study/${f.membership.id}/${f.membership.version + 10L}/${study.id.id}"
        studyRepository.put(study)
        hasInvalidVersion(DELETE, url)
      }

    }

    describe("DELETE /api/access/memberships/centre/:membershipId/:version/:centreId") {

      it("can remove a centre") {
        val f = new MembershipFixture
        val url = uri("memberships") + s"/centre/${f.membership.id}/${f.membership.version}/${f.centre.id.id}"
        val reply = makeRequest(DELETE, url)

        (reply \ "status").as[String] must include ("success")

        val jsonId = (reply \ "data" \ "id").as[String]
        val membershipId = MembershipId(jsonId)
        jsonId.length must be > 0

        membershipRepository.getByKey(membershipId) mustSucceed { repoMembership =>
          compareObj((reply \ "data").as[JsObject], repoMembership)
          repoMembership must have (
            'id             (membershipId),
            'version        (f.membership.version + 1)
          )
          repoMembership.centreData.ids must not contain (f.centre.id)
          checkTimeStamps(repoMembership, f.membership.timeAdded, OffsetDateTime.now)
        }
      }

      it("cannot remove a centre not in the membership") {
        val f = new MembershipFixture
        val centre = factory.createEnabledCentre
        centreRepository.put(centre)
        val url = uri("memberships") + s"/centre/${f.membership.id}/${f.membership.version}/${centre.id.id}"
        val reply = makeRequest(DELETE, url, BAD_REQUEST)

        (reply \ "status").as[String] must include ("error")

        (reply \ "message").as[String] must include ("centre ID is not in membership")
      }

      it("cannot remove a centre in an all centres membership") {
        val f = new MembershipFixtureAllCentres
        val centre = factory.createEnabledCentre
        centreRepository.put(centre)
        val url = uri("memberships") + s"/centre/${f.membership.id}/${f.membership.version}/${centre.id.id}"
        val reply = makeRequest(DELETE, url, BAD_REQUEST)

        (reply \ "status").as[String] must include ("error")

        (reply \ "message").as[String] must include ("membership is for all centres, cannot remove:")
      }

      it("cannot add a centre that does not exist") {
        val f = new MembershipFixture
        val centre = factory.createEnabledCentre
        val url = uri("memberships") + s"/centre/${f.membership.id}/${f.membership.version}/${centre.id.id}"
        val reply = makeRequest(DELETE, url, NOT_FOUND)

        (reply \ "status").as[String] must include ("error")

        (reply \ "message").as[String] must include ("IdNotFound: centre id")
      }

      it("fail when removing and membership ID does not exist") {
        val f = new MembershipFixture
        val centre = factory.createEnabledCentre
        val url = uri("memberships") + s"/centre/${f.membership.id}/${f.membership.version}/${centre.id.id}"
        centreRepository.put(centre)
        membershipRepository.remove(f.membership)
        notFoundOnDelete(url)
      }

      it("fail when removing with invalid version") {
        val f = new MembershipFixture
        val centre = factory.createEnabledCentre
        val url = uri("memberships") + s"/centre/${f.membership.id}/${f.membership.version + 10L}/${centre.id.id}"
        centreRepository.put(centre)
        hasInvalidVersion(DELETE, url)
      }

    }

    describe("DELETE /api/access/memberships/:membershipId/:version") {

      it("can remove a membership") {
        val f = new MembershipFixture
        val url = uri("memberships") + s"/${f.membership.id}/${f.membership.version}"
        val reply = makeRequest(DELETE, url)

        (reply \ "status").as[String] must include ("success")

        (reply \ "data").as[Boolean] must be (true)

        membershipRepository.getByKey(f.membership.id) mustFail ("IdNotFound: membership id.*")
      }

      it("cannot remove a membership that does not exist") {
        val f = new MembershipFixture
        val url = uri("memberships") + s"/${f.membership.id}/${f.membership.version}"
        membershipRepository.remove(f.membership)
        notFoundOnDelete(url)
      }

      it("fail when removing with invalid version") {
        val f = new MembershipFixture
        val url = uri("memberships") + s"/${f.membership.id}/${f.membership.version + 10L}"
        hasInvalidVersion(DELETE, url)
      }

    }

  }

  private def notFound(url: String, json: JsValue): Unit = {
    notFound(POST, url, json, "IdNotFound.*membership")
  }

  private def notFoundOnDelete(url: String): Unit = {
    notFound(DELETE, url, JsNull, "IdNotFound.*membership")
  }

}
