package org.biobank.domain.participants

import org.biobank.fixture.NameGenerator
import org.biobank.domain._
import org.biobank.domain.study._

import org.slf4j.LoggerFactory
import org.joda.time.DateTime

class CollectionEventSpec extends DomainSpec {
  import org.biobank.TestUtils._

  val log = LoggerFactory.getLogger(this.getClass)

  val nameGenerator = new NameGenerator(this.getClass)

  "A collection event" can {

    "be created" when {

      "valid arguments are used" in {
        val cevent = factory.createCollectionEvent

        val v = CollectionEvent.create(
          id                     = cevent.id,
          participantId          = cevent.participantId,
          collectionEventTypeId  = cevent.collectionEventTypeId,
          version                = 0,
          timeCompleted          = cevent.timeCompleted,
          visitNumber            = cevent.visitNumber,
          annotations            = cevent.annotations
        )

        v mustSucceed { ce =>
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

    "not be created" when {

      "an empty id is used" in {
        val v = CollectionEvent.create(
          id                     = CollectionEventId(""),
          participantId          = ParticipantId(nameGenerator.next[ParticipantId]),
          collectionEventTypeId  = CollectionEventTypeId(nameGenerator.next[CollectionEventTypeId]),
          version                = 0L,
          timeCompleted          = DateTime.now,
          visitNumber            = 1,
          annotations            = Set(factory.createAnnotation)
        )
        v mustFail "IdRequired"
      }

      "an empty participant id is used" in {
        val v = CollectionEvent.create(
          id                     = CollectionEventId(nameGenerator.next[CollectionEventId]),
          participantId          = ParticipantId(""),
          collectionEventTypeId  = CollectionEventTypeId(nameGenerator.next[CollectionEventTypeId]),
          version                = 0L,
          timeCompleted          = DateTime.now,
          visitNumber            = 1,
          annotations            = Set(factory.createAnnotation)
        )
        v mustFail "ParticipantIdRequired"
      }

      "an empty collection event type id is used" in {
        val v = CollectionEvent.create(
          id                     = CollectionEventId(nameGenerator.next[CollectionEventId]),
          participantId          = ParticipantId(nameGenerator.next[ParticipantId]),
          collectionEventTypeId  = CollectionEventTypeId(""),
          version                = 0L,
          timeCompleted          = DateTime.now,
          visitNumber            = 1,
          annotations            = Set(factory.createAnnotation)
        )
        v mustFail "CollectionEventTypeIdRequired"
      }

      "an invalid visit number is used" in {
        val v = CollectionEvent.create(
          id                     = CollectionEventId(nameGenerator.next[CollectionEventId]),
          participantId          = ParticipantId(nameGenerator.next[ParticipantId]),
          collectionEventTypeId  = CollectionEventTypeId(nameGenerator.next[CollectionEventTypeId]),
          version                = 0L,
          timeCompleted          = DateTime.now,
          visitNumber            = 0,
          annotations            = Set(factory.createAnnotation)
        )
        v mustFail "VisitNumberInvalid"
      }

      "an invalid version is used" in {
        val v = CollectionEvent.create(
          id                     = CollectionEventId(nameGenerator.next[CollectionEventId]),
          participantId          = ParticipantId(nameGenerator.next[ParticipantId]),
          collectionEventTypeId  = CollectionEventTypeId(nameGenerator.next[CollectionEventTypeId]),
          version                = -2,
          timeCompleted          = DateTime.now,
          visitNumber            = 1,
          annotations            = Set(factory.createAnnotation)
        )
        v mustFail "InvalidVersion"
      }

    }

  }

}
