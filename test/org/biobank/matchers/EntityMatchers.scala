package org.biobank.matchers

import java.time.OffsetDateTime
import gnieh.diffson.playJson._
import org.biobank.domain.{ConcurrencySafeEntity, Location}
import org.biobank.domain.access._
import org.biobank.domain.annotations._
import org.biobank.domain.centres._
import org.biobank.domain.participants._
import org.biobank.domain.studies._
import org.biobank.domain.users._
import org.scalatest.matchers.{MatchResult, Matcher}
import play.api.libs.json._

trait EntityMatchers {

  import DateMatchers._

  def beEntityWithTimeAddedWithinSeconds(time: OffsetDateTime, diffSeconds: Long) =
    beTimeWithinSeconds(time, diffSeconds) compose { (e: ConcurrencySafeEntity[_]) => e.timeAdded }

  def beEntityWithTimeModifiedWithinSeconds(time: Option[OffsetDateTime], diffSeconds: Long) =
    beOptionalTimeWithinSeconds(time, diffSeconds) compose {
      (e: ConcurrencySafeEntity[_]) => e.timeModified
    }

  def beEntityWithTimeStamps[T <: ConcurrencySafeEntity[_]](timeAdded:    OffsetDateTime,
                                                            timeModified: Option[OffsetDateTime],
                                                            diffSeconds:  Long) =
    new Matcher[T] {
      def apply(left: T) = {
        val timeAddedMatches = beEntityWithTimeAddedWithinSeconds(timeAdded, diffSeconds)(left)
        if (!timeAddedMatches.matches) {
          MatchResult(false,
                      "timeAdded exceeds difference of {0} seconds: timeAdded: {1}, expected: {2}",
                      "timeAdded within difference of {0} seconds: timeAdded: {1}, expected: {2}",
                      IndexedSeq(diffSeconds, left.timeAdded, timeAdded))
        } else {
          val timeModifiedMatches = beEntityWithTimeModifiedWithinSeconds(timeModified, diffSeconds)(left)
          MatchResult(timeModifiedMatches.matches,
                      s"timeModified: ${timeModifiedMatches.failureMessage}",
                      s"timeModified: ${timeModifiedMatches.negatedFailureMessage}")
        }
      }
    }

  /**
   * This matcher allows for time differences in `timeAdded` and `timeModified` of 5 seconds.
   *
   * The `equals` matcher, from scalatest, cannot be used since ConcurrencySafeEntity overrides `equals`
   * and `hashCode`.
   */
  def matchCentre(centre: Centre) =
    new Matcher[Centre] {
      def apply(left: Centre) = {
        val matchers = Map(
            ("id"          -> (left.id equals centre.id)),
            ("slug"        -> (left.slug equals centre.slug)),
            ("state"       -> (left.state equals centre.state)),
            ("name"        -> (left.name equals centre.name)),
            ("description" -> (left.description equals centre.description)),
            ("studyIds"    -> (left.studyIds equals centre.studyIds)),
            ("locations"   -> (locationsMatch(left.locations, centre.locations)))) ++
        entityAttrsMatch(centre, left)

        val nonMatching = matchers filter { case (k, v) => !v } keys

        MatchResult(nonMatching.size <= 0,
                    "centres do not match for the folowing attributes: {0},\n: actual {1},\nexpected: {2}",
                    "centres match: actual: {1},\nexpected: {2}",
                    IndexedSeq(nonMatching.mkString(", "), left, centre))
      }
    }

  /**
   * This matcher allows for time differences in `timeAdded` and `timeModified` of 5 seconds.
   *
   * The `equals` matcher, from scalatest, cannot be used since ConcurrencySafeEntity overrides `equals`
   * and `hashCode`.
   */
  def matchStudy(study: Study) =
    new Matcher[Study] {
      def apply(left: Study) = {
        val matchers = Map(
            ("id"              -> (left.id equals study.id)),
            ("slug"            -> (left.slug equals study.slug)),
            ("state"           -> (left.state equals study.state)),
            ("name"            -> (left.name equals study.name)),
            ("description"     -> (left.description equals study.description)),
            ("annotationTypes" -> (annotationTypesMatch(left.annotationTypes, study.annotationTypes)))) ++
        entityAttrsMatch(study, left)

        val nonMatching = matchers filter { case (k, v) => !v } keys

        MatchResult(nonMatching.size <= 0,
                    "studies do not match for the folowing attributes: {0},\n: actual {1},\nexpected: {2}",
                    "studies match: actual: {1},\nexpected: {2}",
                    IndexedSeq(nonMatching.mkString(", "), left, study))
      }
    }

  /**
   * This matcher allows for time differences in `timeAdded` and `timeModified` of 5 seconds.
   *
   * The `equals` matcher, from scalatest, cannot be used since ConcurrencySafeEntity overrides `equals`
   * and `hashCode`.
   */
  def matchCollectionEventType(eventType: CollectionEventType) =
    new Matcher[CollectionEventType] {
      def apply(left: CollectionEventType) = {
        val matchers = Map(
            ("id"              -> (left.id equals eventType.id)),
            ("slug"            -> (left.slug equals eventType.slug)),
            ("name"            -> (left.name equals eventType.name)),
            ("description"     -> (left.description equals eventType.description)),
            ("recurring"       -> (left.recurring equals eventType.recurring)),
            ("specimenDefinitions" -> collectionSpecimenDefinitionsMatch(left.specimenDefinitions,
                                                                        eventType.specimenDefinitions)),
            ("annotationTypes" -> (annotationTypesMatch(left.annotationTypes,
                                                       eventType.annotationTypes)))) ++
        entityAttrsMatch(eventType, left)

        val nonMatching = matchers filter { case (k, v) => !v } keys

        MatchResult(nonMatching.size <= 0,
                    "event types do not match for the folowing attributes: {0},\n: actual {1},\nexpected: {2}",
                    "event types match: actual: {1},\nexpected: {2}",
                    IndexedSeq(nonMatching.mkString(", "), left, eventType))
      }
    }

  /**
   * This matcher allows for time differences in `timeAdded` and `timeModified` of 5 seconds.
   *
   * The `equals` matcher, from scalatest, cannot be used since ConcurrencySafeEntity overrides `equals`
   * and `hashCode`.
   */
  def matchProcessingType(processingType: ProcessingType) =
    new Matcher[ProcessingType] {
      def apply(left: ProcessingType) = {
        val matchers = Map(
            ("id"                 -> (left.id          equals processingType.id)),
            ("slug"               -> (left.slug        equals processingType.slug)),
            ("name"               -> (left.name        equals processingType.name)),
            ("description"        -> (left.description equals processingType.description)),
            ("enabled"            -> (left.enabled     equals processingType.enabled)),
            ("annotationTypes"    -> (annotationTypesMatch(left.annotationTypes,
                                                          processingType.annotationTypes)))
          ) ++
        inputSpecimenInfosMatch(left, processingType) ++
        outputSpecimenInfosMatch(left, processingType) ++
        entityAttrsMatch(processingType, left)

        val nonMatching = matchers filter { case (k, v) => !v } keys

        MatchResult(nonMatching.size <= 0,
                    "event types do not match for the folowing attributes: {0},\n: diff: {1}",
                    "event types match: actual: {1},\nexpected: {2}",
                    IndexedSeq(nonMatching.mkString(", "),
                               JsonDiff.diff(Json.toJson(processingType), Json.toJson(left), true)))
      }
    }

  /**
   * This matcher allows for time differences in `timeAdded` and `timeModified` of 5 seconds.
   *
   * The `equals` matcher, from scalatest, cannot be used since ConcurrencySafeEntity overrides `equals`
   * and `hashCode`.
   */
  def matchUser(user: User) =
    new Matcher[User] {
      def apply(left: User) = {
        val matchers = Map(
            ("id"           -> (left.id equals user.id)),
            ("slug"         -> (left.slug equals user.slug)),
            ("state"        -> (left.state equals user.state)),
            ("name"         -> (left.name equals user.name)),
            ("email"        -> (left.email equals user.email)),
            ("avatarUrl"    -> (left.avatarUrl equals user.avatarUrl))) ++
          entityAttrsMatch(user, left)

        val nonMatching = matchers filter { case (k, v) => !v } keys

        MatchResult(nonMatching.size <= 0,
                    "users do not match for the following attributes: {0},\n: actual {1},\nexpected: {2}",
                    "users match: actual: {1},\nexpected: {2}",
                    IndexedSeq(nonMatching.mkString(", "), left, user))
      }
    }

  /**
   * This matcher allows for time differences in `timeAdded` and `timeModified` of 5 seconds.
   *
   * The `equals` matcher, from scalatest, cannot be used since ConcurrencySafeEntity overrides `equals`
   * and `hashCode`.
   */
  def matchShipment(shipment: Shipment) =
    new Matcher[Shipment] {
      def apply(left: Shipment) = {
        val matchers = Map(
            ("id"             -> (left.id equals shipment.id)),
            ("state"          -> (left.state equals shipment.state)),
            ("courierName"    -> (left.courierName equals shipment.courierName)),
            ("trackingNumber" -> (left.trackingNumber equals shipment.trackingNumber)),
            ("fromCentreId"   -> (left.fromCentreId equals shipment.fromCentreId)),
            ("fromLocationId" -> (left.fromLocationId equals shipment.fromLocationId)),
            ("toCentreId"     -> (left.toCentreId equals shipment.toCentreId)),
            ("toLocationId"   -> (left.toLocationId equals shipment.toLocationId)),
            ("timePacked"     -> (left.timePacked equals shipment.timePacked)),
            ("timeSent"       -> (left.timeSent equals shipment.timeSent)),
            ("timeReceived"   -> (left.timeReceived equals shipment.timeReceived)),
            ("timeUnpacked"   -> (left.timeUnpacked equals shipment.timeUnpacked)),
            ("timeCompleted"  -> (left.timeCompleted equals shipment.timeCompleted))) ++
          entityAttrsMatch(shipment, left)

        val nonMatching = matchers filter { case (k, v) => !v } keys

        MatchResult(nonMatching.size <= 0,
                    "shipments do not match for the following attributes: {0},\n: actual {1},\nexpected: {2}",
                    "shipments match: actual: {1},\nexpected: {2}",
                    IndexedSeq(nonMatching.mkString(", "), left, shipment))
      }
    }

  /**
   * This matcher allows for time differences in `timeAdded` and `timeModified` of 5 seconds.
   *
   * The `equals` matcher, from scalatest, cannot be used since ConcurrencySafeEntity overrides `equals`
   * and `hashCode`.
   */
  def matchShipmentSpecimen(shSpc: ShipmentSpecimen) =
    new Matcher[ShipmentSpecimen] {
      def apply(left: ShipmentSpecimen) = {
        val matchers = Map(
            ("id"                  -> (left.id equals shSpc.id)),
            ("state"               -> (left.state equals shSpc.state)),
            ("shipmentId"          -> (left.shipmentId equals shSpc.shipmentId)),
            ("specimenId"          -> (left.specimenId equals shSpc.specimenId)),
            ("state"               -> (left.state equals shSpc.state)),
            ("shipmentContainerId" -> (left.shipmentContainerId equals shSpc.shipmentContainerId))) ++
          entityAttrsMatch(shSpc, left)

        val nonMatching = matchers filter { case (k, v) => !v } keys

        MatchResult(nonMatching.size <= 0,
                    "shipments do not match for the following attributes: {0},\n: actual {1},\nexpected: {2}",
                    "shipments match: actual: {1},\nexpected: {2}",
                    IndexedSeq(nonMatching.mkString(", "), left, shSpc))
      }
    }

  /**
   * This matcher allows for time differences in `timeAdded` and `timeModified` of 5 seconds.
   *
   * The `equals` matcher, from scalatest, cannot be used since ConcurrencySafeEntity overrides `equals`
   * and `hashCode`.
   */
  def matchParticipant(participant: Participant) =
    new Matcher[Participant] {
      def apply(left: Participant) = {
        val matchers = Map(
            ("id"           -> (left.id equals participant.id)),
            ("slug"         -> (left.slug equals participant.slug)),
            ("uniqueId"     -> (left.uniqueId equals participant.uniqueId)),
            ("annotations"  -> annotationsMatch(left.annotations, participant.annotations))) ++
          entityAttrsMatch(participant, left)

        val nonMatching = matchers filter { case (k, v) => !v } keys

        MatchResult(nonMatching.size <= 0,
                    "participants do not match for the following attributes: {0},\n: actual {1},\nexpected: {2}",
                    "participants match: actual: {1},\nexpected: {2}",
                    IndexedSeq(nonMatching.mkString(", "), left, participant))
      }
    }

  /**
   * This matcher allows for time differences in `timeAdded` and `timeModified` of 5 seconds.
   *
   * The `equals` matcher, from scalatest, cannot be used since ConcurrencySafeEntity overrides `equals`
   * and `hashCode`.
   */
  def matchCollectionEvent(collectionEvent: CollectionEvent) =
    new Matcher[CollectionEvent] {
      def apply(left: CollectionEvent) = {
        val matchers = Map(
            ("id"            -> (left.id equals collectionEvent.id)),
            ("slug"          -> (left.slug equals collectionEvent.slug)),
            ("participantId" -> (left.participantId equals collectionEvent.participantId)),
            ("eventTypeId"   -> (left.collectionEventTypeId equals collectionEvent.collectionEventTypeId)),
            ("visitNumber"   -> (left.visitNumber equals collectionEvent.visitNumber)),
            ("timeCompleted" -> (left.timeCompleted equals collectionEvent.timeCompleted)),
            ("annotations"   -> annotationsMatch(left.annotations, collectionEvent.annotations))) ++
          entityAttrsMatch(collectionEvent, left)

        val nonMatching = matchers filter { case (k, v) => !v } keys

        MatchResult(nonMatching.size <= 0,
                    "collectionEvents do not match for the following attributes: {0},\n: actual {1},\nexpected: {2}",
                    "collectionEvents match: actual: {1},\nexpected: {2}",
                    IndexedSeq(nonMatching.mkString(", "), left, collectionEvent))
      }
    }

  /**
   * This matcher allows for time differences in `timeAdded` and `timeModified` of 5 seconds.
   *
   * The `equals` matcher, from scalatest, cannot be used since ConcurrencySafeEntity overrides `equals`
   * and `hashCode`.
   */
  def matchSpecimen(specimen: Specimen) =
    new Matcher[Specimen] {
      def apply(left: Specimen) = {
        val matchers = Map(
            ("id"                   -> (left.id equals specimen.id)),
            ("slug"                 -> (left.slug equals specimen.slug)),
            ("inventoryId"          -> (left.inventoryId equals specimen.inventoryId)),
            ("specimenDefinitionId" -> (left.specimenDefinitionId equals specimen.specimenDefinitionId)),
            ("originLocationId"     -> (left.originLocationId equals specimen.originLocationId)),
            ("locationId"           -> (left.locationId equals specimen.locationId)),
            ("containerId"          -> (left.containerId equals specimen.containerId)),
            ("timeCreated"          -> (left.timeCreated equals specimen.timeCreated)),
            ("amount"               -> (left.amount equals specimen.amount))) ++
          entityAttrsMatch(specimen, left)

        val nonMatching = matchers filter { case (k, v) => !v } keys

        MatchResult(nonMatching.size <= 0,
                    "specimens do not match for the following attributes: {0},\n: actual {1},\nexpected: {2}",
                    "specimens match: actual: {1},\nexpected: {2}",
                    IndexedSeq(nonMatching.mkString(", "), left, specimen))
      }
    }

  /**
   * This matcher allows for time differences in `timeAdded` and `timeModified` of 5 seconds.
   *
   * The `equals` matcher, from scalatest, cannot be used since ConcurrencySafeEntity overrides `equals`
   * and `hashCode`.
   */
  def matchRole(role: Role) =
    new Matcher[Role] {
      def apply(left: Role) = {
        val matchers = Map(("userIds" -> (left.userIds equals role.userIds))) ++
        accessItemsMatch(left, role) ++
        entityAttrsMatch(role, left)

        val nonMatching = matchers filter { case (k, v) => !v } keys

        MatchResult(nonMatching.size <= 0,
                    "roles do not match for the following attributes: {0},\n: actual {1},\nexpected: {2}",
                    "roles match: actual: {1},\nexpected: {2}",
                    IndexedSeq(nonMatching.mkString(", "), left, role))
      }
    }

  def matchAccessItem(accessItem: AccessItem) =
    new Matcher[AccessItem] {
      def apply(left: AccessItem) = {
        val matchers = accessItemsMatch(left, accessItem) ++ entityAttrsMatch(accessItem, left)
        val nonMatching = matchers filter { case (k, v) => !v } keys

        MatchResult(nonMatching.size <= 0,
                    "accessItems do not match for the following attributes: {0},\n: actual {1},\nexpected: {2}",
                    "accessItems match: actual: {1},\nexpected: {2}",
                    IndexedSeq(nonMatching.mkString(", "), left, accessItem))
      }
    }

  def matchMembership(membership: Membership) =
    new Matcher[Membership] {
      def apply(left: Membership) = {
        val matchers = Map(
            ("id"          -> (left.id          equals membership.id)),
            ("slug"        -> (left.slug        equals membership.slug)),
            ("name"        -> (left.name        equals membership.name)),
            ("description" -> (left.description equals membership.description)),
            ("userIds"     -> (left.userIds     equals membership.userIds)),
            ("studyData"   -> (left.studyData   equals membership.studyData)),
            ("centreData"  -> (left.centreData  equals membership.centreData))
          )

        val nonMatching = matchers filter { case (k, v) => !v } keys

        MatchResult(nonMatching.size <= 0,
                    "memberships do not match for the following attributes: {0},\n: actual {1},\nexpected: {2}",
                    "memberships match: actual: {1},\nexpected: {2}",
                    IndexedSeq(nonMatching.mkString(", "), left, membership))
      }
    }

  private def annotationTypesMatch(a: Set[AnnotationType], b: Set[AnnotationType]): Boolean = {
    val maybeMatch = a.map { atToFind =>
        b.exists { atOther =>
          ((atOther.id            equals atToFind.id) &&
             (atOther.slug          equals atToFind.slug) &&
             (atOther.name          equals atToFind.name) &&
             (atOther.description   equals atToFind.description) &&
             (atOther.valueType     equals atToFind.valueType) &&
             (atOther.maxValueCount equals atToFind.maxValueCount) &&
             (atOther.options       equals atToFind.options) &&
             (atOther.required      equals atToFind.required))
        }
      }

    maybeMatch.foldLeft(true) { (result, found) => result && found }
  }

  private def annotationsMatch(a: Set[Annotation], b: Set[Annotation]): Boolean = {
    val maybeMatch = a.map { atToFind =>
        b.exists { atOther =>
          ((atOther.annotationTypeId  equals atToFind.annotationTypeId)  &&
             (atOther.stringValue    equals atToFind.stringValue) &&
             (atOther.numberValue    equals atToFind.numberValue) &&
             (atOther.selectedValues equals atToFind.selectedValues))
        }
      }

    maybeMatch.foldLeft(true) { (result, found) => result && found }
  }

  private def collectionSpecimenDefinitionsMatch(a: Set[CollectionSpecimenDefinition],
                                                 b: Set[CollectionSpecimenDefinition]): Boolean = {
    val maybeMatch = a.map { toFind =>
        b.exists { other =>
          ((other.id            equals toFind.id) &&
             (other.slug          equals toFind.slug) &&
             (other.name          equals toFind.name) &&
             (other.description   equals toFind.description) &&
             (other.units                   equals toFind.units) &&
             (other.anatomicalSourceType    equals toFind.anatomicalSourceType) &&
             (other.preservationType        equals toFind.preservationType) &&
             (other.preservationTemperature equals toFind.preservationTemperature) &&
             (other.specimenType            equals toFind.specimenType) &&
             (other.maxCount                equals toFind.maxCount) &&
             (other.amount                  equals toFind.amount))
        }
      }

    maybeMatch.foldLeft(true) { (result, found) => result && found }
  }

  private def locationsMatch(a: Set[Location], b: Set[Location]): Boolean = {
    val maybeMatch = a.map { toFind =>
        b.exists { other =>
          ((other.id               equals toFind.id) &&
             (other.slug           equals toFind.slug) &&
             (other.name           equals toFind.name) &&
             (other.street         equals toFind.street) &&
             (other.city           equals toFind.city) &&
             (other.province       equals toFind.province) &&
             (other.postalCode     equals toFind.postalCode) &&
             (other.poBoxNumber    equals toFind.poBoxNumber) &&
             (other.countryIsoCode equals toFind.countryIsoCode))
        }
      }

    maybeMatch.foldLeft(true) { (result, found) => result && found }
  }

  private def entityAttrsMatch(a: ConcurrencySafeEntity[_], b: ConcurrencySafeEntity[_]) = {
    val timeAddedMatcher = beTimeWithinSeconds(a.timeAdded, 5L)(b.timeAdded)

    val timeModifiedMatcher = beOptionalTimeWithinSeconds(a.timeModified, 5L)(b.timeModified)

    Map(("version"      -> (a.version equals b.version)),
        ("timeAdded"    -> (timeAddedMatcher.matches)),
        ("timeModified" -> (timeModifiedMatcher.matches)))
  }

  private def accessItemsMatch(a: AccessItem, b: AccessItem) = {
    Map(
      ("id"          -> (a.id          equals b.id)),
      ("slug"        -> (a.slug        equals b.slug)),
      ("name"        -> (a.name        equals b.name)),
      ("description" -> (a.description equals b.description)),
      ("parentIds"   -> (a.parentIds   equals b.parentIds)),
      ("childrenIds" -> (a.childrenIds equals b.childrenIds))
    )
  }

  private def inputSpecimenInfosMatch(a: ProcessingType, b: ProcessingType) = {
    val aIsi = a.input
    val bIsi = b.input
    Map(
      ("input.expectedChange"       -> (aIsi.expectedChange       equals bIsi.expectedChange)),
      ("input.count"                -> (aIsi.count                equals bIsi.count)),
      ("input.containerTypeId"      -> (aIsi.containerTypeId      equals bIsi.containerTypeId)),
      ("input.definitionType"       -> (aIsi.definitionType       equals bIsi.definitionType)),
      ("input.entityId"             -> (aIsi.entityId             equals bIsi.entityId)),
      ("input.specimenDefinitionId" -> (aIsi.specimenDefinitionId equals bIsi.specimenDefinitionId)),
    )
  }

  private def outputSpecimenInfosMatch(a: ProcessingType, b: ProcessingType) = {
    val aOsi = a.output
    val bOsi = b.output
    Map(
      ("output.expectedChange"  -> (aOsi.expectedChange equals bOsi.expectedChange)),
      ("output.count"           -> (aOsi.count equals bOsi.count)),
      ("output.containerTypeId" -> (aOsi.containerTypeId equals bOsi.containerTypeId)),
    ) ++
    outputSpecimenDefinitionsMatch(a, b)
  }

  private def outputSpecimenDefinitionsMatch(a: ProcessingType, b: ProcessingType) = {
    val aOsd = a.output.specimenDefinition
    val bOsd = b.output.specimenDefinition
    Map(
      ("output.specimenDefinition.id"                      ->
         (aOsd.id                      equals bOsd.id)),
      ("output.specimenDefinition.slug"                    ->
         (aOsd.slug                    equals bOsd.slug)),
      ("output.specimenDefinition.name"                    ->
         (aOsd.name                    equals bOsd.name)),
      ("output.specimenDefinition.description"             ->
         (aOsd.description             equals bOsd.description)),
      ("output.specimenDefinition.units"                   ->
         (aOsd.units                   equals bOsd.units)),
      ("output.specimenDefinition.anatomicalSourceType"    ->
         (aOsd.anatomicalSourceType    equals bOsd.anatomicalSourceType)),
      ("output.specimenDefinition.preservationType"        ->
         (aOsd.preservationType        equals bOsd.preservationType)),
      ("output.specimenDefinition.preservationTemperature" ->
         (aOsd.preservationTemperature equals bOsd.preservationTemperature)),
      ("output.specimenDefinition.specimenType"            ->
         (aOsd.specimenType            equals bOsd.specimenType))
    )
  }

}

object EntityMatchers extends EntityMatchers
