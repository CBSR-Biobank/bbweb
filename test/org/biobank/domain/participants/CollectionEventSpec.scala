package org.biobank.domain.participants

import org.biobank.fixture.NameGenerator
import org.biobank.domain._
import org.biobank.domain.study._
import org.slf4j.LoggerFactory
import org.joda.time.DateTime

class CollectionEventSpec extends DomainFreeSpec {
  import org.biobank.TestUtils._

  val log = LoggerFactory.getLogger(this.getClass)

  val nameGenerator = new NameGenerator(this.getClass)

  def createFrom(collectionEvent: CollectionEvent): DomainValidation[CollectionEvent] =
    CollectionEvent.create(id                     = collectionEvent.id,
                           participantId          = collectionEvent.participantId,
                           collectionEventTypeId  = collectionEvent.collectionEventTypeId,
                           version                = collectionEvent.version,
                           timeCompleted          = collectionEvent.timeCompleted,
                           visitNumber            = collectionEvent.visitNumber,
                           annotations            = collectionEvent.annotations)

  "A collection event" - {

    "can be created" - {

      "when valid arguments are used and with no annotations" in {
        val cevent = factory.createCollectionEvent.copy(version = 0L)
        createFrom(cevent) mustSucceed { ce =>
          ce must have (
            'id                     (cevent.id),
            'participantId          (cevent.participantId),
            'collectionEventTypeId  (cevent.collectionEventTypeId),
            'version                (0),
            'visitNumber            (cevent.visitNumber),
            'annotations            (cevent.annotations)
          )

          checkTimeStamps(cevent, ce.timeAdded, ce.timeModified)
          checkTimeStamps(cevent.timeCompleted, ce.timeCompleted)
        }
      }

      "when valid arguments are used and annotations" in {
        val annotation = factory.createAnnotation
        val cevent = factory.createCollectionEvent.copy(annotations = Set(annotation),
                                                        version     = 0L)
        createFrom(cevent) mustSucceed { ce =>
          ce must have (
            'id                     (cevent.id),
            'participantId          (cevent.participantId),
            'collectionEventTypeId  (cevent.collectionEventTypeId),
            'version                (0),
            'visitNumber            (cevent.visitNumber),
            'annotations            (cevent.annotations)
          )

          checkTimeStamps(cevent, ce.timeAdded, ce.timeModified)
          checkTimeStamps(cevent.timeCompleted, ce.timeCompleted)
        }
      }
    }

    "cannot be created with" - {

      "an empty id is used" in {
        val cevent = factory.createCollectionEvent.copy(id = CollectionEventId(""))
        createFrom(cevent) mustFail "IdRequired"
      }

      "an empty participant id is used" in {
        val cevent = factory.createCollectionEvent.copy(participantId = ParticipantId(""))
        createFrom(cevent) mustFail "ParticipantIdRequired"
      }

      "an empty collection event type id is used" in {
        val cevent = factory.createCollectionEvent.copy(collectionEventTypeId = CollectionEventTypeId(""))
        createFrom(cevent) mustFail "CollectionEventTypeIdRequired"
      }

      "an invalid visit number is used" in {
        val cevent = factory.createCollectionEvent.copy(visitNumber = 0)
        createFrom(cevent) mustFail "VisitNumberInvalid"
      }

      "an invalid version is used" in {
        val cevent = factory.createCollectionEvent.copy(version = -2)
        createFrom(cevent) mustFail "InvalidVersion"
      }

    }

    "can be updated" - {

      "with a new visit number" in {
        val cevent = factory.createCollectionEvent
        val newVisitNumber = cevent.visitNumber + 10

        cevent.withVisitNumber(newVisitNumber) mustSucceed { s =>
          s.visitNumber must be (newVisitNumber)
          s.version must be (cevent.version + 1)
          checkTimeStamps(s, cevent.timeAdded, DateTime.now)
        }
      }

      "with a new time completed" in {
        val cevent = factory.createCollectionEvent
        val newTimeCompleted = cevent.timeCompleted.minusDays(10)

        cevent.withTimeCompleted(newTimeCompleted) mustSucceed { s =>
          s.timeCompleted must be (newTimeCompleted)
          s.version must be (cevent.version + 1)
          checkTimeStamps(s, cevent.timeAdded, DateTime.now)
        }
      }

    }

    "cannot be updated" - {

      "with an invalid visit number" in {
        val cevent = factory.createCollectionEvent
        cevent.withVisitNumber(0) mustFail "VisitNumberInvalid"
        cevent.withVisitNumber(-1) mustFail "VisitNumberInvalid"
      }

    }

  }

}
