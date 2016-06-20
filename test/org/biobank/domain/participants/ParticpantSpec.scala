package org.biobank.domain.participants

import org.biobank.fixture.NameGenerator
import org.biobank.domain._

import org.slf4j.LoggerFactory
import org.joda.time.DateTime

class ParticipantSpec extends DomainFreeSpec {
  import org.biobank.TestUtils._

  val log = LoggerFactory.getLogger(this.getClass)

  val nameGenerator = new NameGenerator(this.getClass)

  def createFrom(participant: Participant): DomainValidation[Participant] =
    Participant.create(id           = participant.id,
                       studyId      = participant.studyId,
                       version      = participant.version,
                       uniqueId     = participant.uniqueId,
                       annotations  = participant.annotations)

  "A participant" - {

    "can be created" - {

      "when valid arguments are used and without annotations" in {
        val participant = factory.createParticipant.copy(version = 0L)
        createFrom(participant) mustSucceed { p =>
          p must have (
              'id           (participant.id),
              'studyId      (participant.studyId),
              'version      (participant.version),
              'uniqueId     (participant.uniqueId),
              'annotations  (participant.annotations)
          )

          checkTimeStamps(participant, p.timeAdded, None)
        }
      }

      "when valid arguments are used and with an annotation" in {
        val annotation = factory.createAnnotation
        val participant = factory.createParticipant.copy(annotations = Set(annotation),
                                                         version     = 0L)
        createFrom(participant) mustSucceed { p =>
          p must have (
              'id           (participant.id),
              'studyId      (participant.studyId),
              'version      (participant.version),
              'uniqueId     (participant.uniqueId),
              'annotations  (participant.annotations)
          )

          checkTimeStamps(participant, p.timeAdded, None)
        }
      }

    }

    "can be updated" - {

      "with a new unique ID" in {
        val participant = factory.createParticipant
        val newUniqueId = nameGenerator.next[Participant]

        participant.withUniqueId(newUniqueId) mustSucceed { p =>
          p.uniqueId must be (newUniqueId)
          p.version must be (participant.version + 1)
          checkTimeStamps(p, participant.timeAdded, DateTime.now)
        }
      }

      "with a new annotation" in {
        val participant = factory.createParticipant.copy(annotations = Set())
        val annotation = factory.createAnnotation

        participant.withAnnotation(annotation) mustSucceed { p =>
          p.annotations must have size 1
          p.version must be (participant.version + 1)
          checkTimeStamps(p, participant.timeAdded, DateTime.now)
        }
      }

      "without an annotation" in {
        val annotation = factory.createAnnotation
        val participant = factory.createParticipant.copy(annotations = Set(annotation))

        participant.withoutAnnotation(annotation.annotationTypeId) mustSucceed { p =>
          p.annotations must have size 0
          p.version must be (participant.version + 1)
          checkTimeStamps(p, participant.timeAdded, DateTime.now)
        }
      }
    }

    "cannot be created" - {

      "with an empty id" in {
        val participant = factory.createParticipant.copy(id = ParticipantId(""))
        createFrom(participant) mustFail "IdRequired"
      }

      "with an empty unique id" in {
        val participant = factory.createParticipant.copy(uniqueId = "")
        createFrom(participant) mustFail "UniqueIdRequired"
      }
    }

  }

  "cannot be updated" - {

    "with an invalid unique ID" in {
      val participant = factory.createParticipant
      participant.withUniqueId("") mustFail "UniqueIdRequired"
    }

    "to remove an annotation, with an invalid annotation type ID" in {
      val participant = factory.createParticipant
      participant.withoutAnnotation(nameGenerator.next[Participant]) mustFail "annotation does not exist:.*"
    }

  }

}
