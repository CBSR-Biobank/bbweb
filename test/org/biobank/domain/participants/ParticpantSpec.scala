package org.biobank.domain.participants

import org.biobank.fixture.NameGenerator
import org.biobank.domain._

import org.slf4j.LoggerFactory
import org.joda.time.DateTime

class ParticipantSpec extends DomainSpec {
  import org.biobank.TestUtils._

  val log = LoggerFactory.getLogger(this.getClass)

  val nameGenerator = new NameGenerator(this.getClass)

  def createFrom(participant: Participant): DomainValidation[Participant] =
    Participant.create(id           = participant.id,
                       studyId      = participant.studyId,
                       version      = participant.version,
                       uniqueId     = participant.uniqueId,
                       annotations  = participant.annotations,
                       timeAdded    = DateTime.now)

  describe("A participant") {

    describe("can be created") {

      it("when valid arguments are used and without annotations") {
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

      it("when valid arguments are used and with an annotation") {
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

    describe("can be updated") {

      it("with a new unique ID") {
        val participant = factory.createParticipant
        val newUniqueId = nameGenerator.next[Participant]

        participant.withUniqueId(newUniqueId) mustSucceed { p =>
          p.uniqueId must be (newUniqueId)
          p.version must be (participant.version + 1)
          checkTimeStamps(p, participant.timeAdded, DateTime.now)
        }
      }

      it("with a new annotation") {
        val participant = factory.createParticipant.copy(annotations = Set())
        val annotation = factory.createAnnotation

        participant.withAnnotation(annotation) mustSucceed { p =>
          p.annotations must have size 1
          p.version must be (participant.version + 1)
          checkTimeStamps(p, participant.timeAdded, DateTime.now)
        }
      }

      it("without an annotation") {
        val annotation = factory.createAnnotation
        val participant = factory.createParticipant.copy(annotations = Set(annotation))

        participant.withoutAnnotation(annotation.annotationTypeId) mustSucceed { p =>
          p.annotations must have size 0
          p.version must be (participant.version + 1)
          checkTimeStamps(p, participant.timeAdded, DateTime.now)
        }
      }
    }

    describe("cannot be created") {

      it("with an empty id") {
        val participant = factory.createParticipant.copy(id = ParticipantId(""))
        createFrom(participant) mustFail "IdRequired"
      }

      it("with an empty unique id") {
        val participant = factory.createParticipant.copy(uniqueId = "")
        createFrom(participant) mustFail "UniqueIdRequired"
      }
    }

  }

  describe("cannot be updated") {

    it("with an invalid unique ID") {
      val participant = factory.createParticipant
      participant.withUniqueId("") mustFail "UniqueIdRequired"
    }

    it("to remove an annotation, with an invalid annotation type ID") {
      val participant = factory.createParticipant
      participant.withoutAnnotation(nameGenerator.next[Participant]) mustFail "annotation does not exist:.*"
    }

  }

}
