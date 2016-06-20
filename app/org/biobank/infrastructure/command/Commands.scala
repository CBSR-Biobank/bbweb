package org.biobank.infrastructure.command

object Commands {

  trait Command

  trait HasUserId {
    val userId: String
  }

  trait HasOptionalUserId {
    val userId: Option[String]
  }

  trait HasExpectedVersion {

    /** A command that must include the version of the object the command applies to. */
    val expectedVersion: Long
  }
  trait HasIdentity {

    /** A command that includes the ID of the object it references. */
    val id: String

  }

  trait HasStudyIdentity {

    /** A command that includes the study ID that it is related to. */
    val studyId: String

  }

  trait HasCollectionEventTypeIdentity {

    /** A command that includes the collection event type ID that it is related to. */
    val collectionEventTypeId: String

  }

  trait HasParticipantIdentity {

    /** A command that includes the participant ID that it is related to. */
    val participantId: String

  }

  trait HasCollectionEventIdentity {

    /** A command that includes the participant ID that it is related to. */
    val collectionEventId: String

  }

  trait HasSpecimenIdentity {

    /** A command that includes the specimen ID that it is related to. */
    val specimenId: String

  }

  trait HasCentreIdentity {

    /** A command that includes the centre ID that it is related to. */
    val centreId: String

  }

  trait HasProcessingTypeIdentity {

    /** A command that includes the processing type ID that it is related to. */
    val processingTypeId: String

  }

  trait HasShipmentIdentity {

    /** A command that includes the shipment ID that it is related to. */
    val shipmentId: String

  }

}
