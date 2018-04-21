package org.biobank.domain.participants

import java.time.OffsetDateTime
import org.biobank.fixture.NameGenerator
import org.biobank.domain._
import org.biobank.domain.annotations._
import org.slf4j.LoggerFactory

class ParticipantSpec extends DomainSpec {
  import org.biobank.TestUtils._
  import org.biobank.matchers.EntityMatchers._

  val log = LoggerFactory.getLogger(this.getClass)

  val nameGenerator = new NameGenerator(this.getClass)

  def createFrom(participant: Participant): DomainValidation[Participant] =
    Participant.create(id           = participant.id,
                       studyId      = participant.studyId,
                       version      = participant.version,
                       uniqueId     = participant.uniqueId,
                       annotations  = participant.annotations,
                       timeAdded    = OffsetDateTime.now)

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

          p must beEntityWithTimeStamps(participant.timeAdded, None, 5L)
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

          p must beEntityWithTimeStamps(participant.timeAdded, None, 5L)
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
          p must beEntityWithTimeStamps(participant.timeAdded, Some(OffsetDateTime.now), 5L)
       }
      }

      it("with a new annotation") {
        val participant = factory.createParticipant.copy(annotations = Set())
        val annotation = factory.createAnnotation

        participant.withAnnotation(annotation) mustSucceed { p =>
          p.annotations must have size 1
          p.version must be (participant.version + 1)
          p must beEntityWithTimeStamps(participant.timeAdded, Some(OffsetDateTime.now), 5L)
        }
      }

      it("without an annotation") {
        val annotation = factory.createAnnotation
        val participant = factory.createParticipant.copy(annotations = Set(annotation))

        participant.withoutAnnotation(annotation.annotationTypeId) mustSucceed { p =>
          p.annotations must have size 0
          p.version must be (participant.version + 1)
          p must beEntityWithTimeStamps(participant.timeAdded, Some(OffsetDateTime.now), 5L)
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
      participant.withoutAnnotation(AnnotationTypeId(nameGenerator.next[Participant]))
        .mustFail("annotation does not exist:.*")
    }

  }

}
