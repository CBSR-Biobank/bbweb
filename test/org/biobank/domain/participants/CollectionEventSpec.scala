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

  "A collection event" should {

    "can be created" when {

      "valid arguments are used" in {
        val cevent = factory.createCollectionEvent

        val v = CollectionEvent.create(
          id                     = cevent.id,
          participantId          = cevent.participantId,
          collectionEventTypeId  = cevent.collectionEventTypeId,
          version                = cevent.version,
          dateTime               = cevent.timeAdded,
          timeCompleted          = cevent.timeCompleted,
          visitNumber            = cevent.visitNumber,
          annotations            = cevent.annotations
        )

        v mustSucceed { cevent =>
          cevent must have (
            'id                     (cevent.id),
            'participantId          (cevent.participantId),
            'collectionEventTypeId  (cevent.collectionEventTypeId),
            'version                (cevent.version),
            'timeAdded              (cevent.timeAdded),
            'timeModified           (None),
            'timeCompleted          (cevent.timeCompleted),
            'visitNumber            (cevent.visitNumber),
            'annotations            (cevent.annotations)
          )
        }
      }

    }

    "not be created" when {

      "an empty id is used" in {
        val v = CollectionEvent.create(
          id                     = CollectionEventId(""),
          participantId          = ParticipantId(nameGenerator.next[ParticipantId]),
          collectionEventTypeId  = CollectionEventTypeId(nameGenerator.next[CollectionEventTypeId]),
          version                = -1,
          dateTime               = DateTime.now,
          timeCompleted          = DateTime.now,
          visitNumber            = 1,
          annotations            = Set(factory.createCollectionEventAnnotation)
        )
        v mustFail "IdRequired"
      }

      "an empty participant id is used" in {
        val v = CollectionEvent.create(
          id                     = CollectionEventId(nameGenerator.next[CollectionEventId]),
          participantId          = ParticipantId(""),
          collectionEventTypeId  = CollectionEventTypeId(nameGenerator.next[CollectionEventTypeId]),
          version                = -1,
          dateTime               = DateTime.now,
          timeCompleted          = DateTime.now,
          visitNumber            = 1,
          annotations            = Set(factory.createCollectionEventAnnotation)
        )
        v mustFail "ParticipantIdRequired"
      }

      "an empty collection event type id is used" in {
        val v = CollectionEvent.create(
          id                     = CollectionEventId(nameGenerator.next[CollectionEventId]),
          participantId          = ParticipantId(nameGenerator.next[ParticipantId]),
          collectionEventTypeId  = CollectionEventTypeId(""),
          version                = -1,
          dateTime               = DateTime.now,
          timeCompleted          = DateTime.now,
          visitNumber            = 1,
          annotations            = Set(factory.createCollectionEventAnnotation)
        )
        v mustFail "CollectinEventTypeIdRequired"
      }

      "an invalid visit number is used" in {
        val v = CollectionEvent.create(
          id                     = CollectionEventId(nameGenerator.next[CollectionEventId]),
          participantId          = ParticipantId(nameGenerator.next[ParticipantId]),
          collectionEventTypeId  = CollectionEventTypeId(nameGenerator.next[CollectionEventTypeId]),
          version                = -1,
          dateTime               = DateTime.now,
          timeCompleted          = DateTime.now,
          visitNumber            = 0,
          annotations            = Set(factory.createCollectionEventAnnotation)
        )
        v mustFail "VisitNumberInvalid"
      }

      "an invalid version is used" in {
        val v = CollectionEvent.create(
          id                     = CollectionEventId(nameGenerator.next[CollectionEventId]),
          participantId          = ParticipantId(nameGenerator.next[ParticipantId]),
          collectionEventTypeId  = CollectionEventTypeId(nameGenerator.next[CollectionEventTypeId]),
          version                = -2,
          dateTime               = DateTime.now,
          timeCompleted          = DateTime.now,
          visitNumber            = 1,
          annotations            = Set(factory.createCollectionEventAnnotation)
        )
        v mustFail "InvalidVersion"
      }

    }

  }

}
