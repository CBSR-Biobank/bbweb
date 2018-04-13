package org.biobank.domain.studies

import java.time.OffsetDateTime
import org.biobank.domain._
import org.biobank.domain.annotations._
import org.biobank.fixture.NameGenerator
import org.slf4j.LoggerFactory
import scalaz.Scalaz._

class StudySpec extends DomainSpec with AnnotationTypeSetSharedSpec[DisabledStudy] {
  import org.biobank.TestUtils._

  val log = LoggerFactory.getLogger(this.getClass)

  val nameGenerator = new NameGenerator(this.getClass)

  def createFrom(study: Study): DomainValidation[DisabledStudy] = {
    DisabledStudy.create(study.id,
                         study.version,
                         study.name,
                         study.description,
                         study.annotationTypes)
  }


  describe("A study") {

    it("be created") {
      val study = factory.createDisabledStudy
      createFrom(study).mustSucceed { s =>
        s mustBe a[DisabledStudy]
        s must have (
          'id          (study.id),
          'version     (0L),
          'name        (study.name),
          'description (study.description)
        )

        s.annotationTypes mustBe empty
        checkTimeStamps(s, OffsetDateTime.now, None)
      }
    }

    it("have it's name updated") {
      val study = factory.createDisabledStudy
      val name = nameGenerator.next[Study]

      study.withName(name) mustSucceed { updatedStudy =>
        updatedStudy must have (
          'id          (study.id),
          'version     (study.version + 1),
          'name        (name),
          'description (study.description)
        )

        checkTimeStamps(updatedStudy, OffsetDateTime.now, OffsetDateTime.now)
      }
    }

    it("have it's description updated") {
      val study = factory.createDisabledStudy
      val description = Some(nameGenerator.next[Study])

      study.withDescription(description) mustSucceed { updatedStudy =>
        updatedStudy must have (
          'id          (study.id),
          'version     (study.version + 1),
          'name        (study.name),
          'description (description)
        )

        checkTimeStamps(updatedStudy, OffsetDateTime.now, OffsetDateTime.now)
      }
    }

    it("be enabled") {
      val study = factory.createDisabledStudy
      study.enable mustSucceed { enabledStudy =>
        enabledStudy mustBe a[EnabledStudy]
        enabledStudy.timeAdded mustBe (study.timeAdded)
        ()
      }
    }

    it("when disabled, can be enabled") {
      val study = factory.createEnabledStudy
      study.disable mustSucceed { disabledStudy =>
        disabledStudy mustBe a[DisabledStudy]
        disabledStudy.timeAdded mustBe (study.timeAdded)
        ()
      }
    }

    it("be retired") {
      val study = factory.createDisabledStudy
      study.retire mustSucceed { retiredStudy =>
        retiredStudy mustBe a[RetiredStudy]
        retiredStudy.timeAdded mustBe (study.timeAdded)
        ()
      }
    }

    it("be unretired") {
      val study = factory.createRetiredStudy
      study.unretire() mustSucceed { disabledStudy =>
        disabledStudy mustBe a[DisabledStudy]
        disabledStudy.timeAdded mustBe (study.timeAdded)
        ()
      }
    }

  }

  describe("A study") {

    it("not be created with an empty id") {
      val study = factory.createDisabledStudy.copy(id = StudyId(""))
      createFrom(study) mustFail "IdRequired"
    }

    it("not be created with an invalid version") {
      val study = factory.createDisabledStudy.copy(version = -2L)
      createFrom(study) mustFail "InvalidVersion"
    }

    it("not be created with an null or empty name") {
      var study = factory.createDisabledStudy.copy(name = null)
      createFrom(study) mustFail "InvalidName"

      study = factory.createDisabledStudy.copy(name = "")
      createFrom(study) mustFail "InvalidName"
    }

    it("not be created with an empty description") {
      var study = factory.createDisabledStudy.copy(description = Some(null))
      createFrom(study) mustFail "InvalidDescription"

      study = factory.createDisabledStudy.copy(description = Some(""))
      createFrom(study) mustFail "InvalidDescription"
    }

    it("have more than one validation fail") {
      val study = factory.createDisabledStudy.copy(version = -2L, name = "")
      createFrom(study) mustFail ("InvalidVersion",  "InvalidName")
    }

  }

  override def createEntity(): DisabledStudy = {
    factory.createDisabledStudy.copy(annotationTypes = Set.empty)
  }

  override def getAnnotationTypeSet(study: DisabledStudy): Set[AnnotationType] = {
    study.annotationTypes
  }

  override def addAnnotationType(study:          DisabledStudy,
                                 annotationType: AnnotationType)
      : DomainValidation[DisabledStudy] = {
    study.withParticipantAnnotationType(annotationType)
  }

  override def removeAnnotationType(study: DisabledStudy, id: AnnotationTypeId)
      : DomainValidation[DisabledStudy] = {
    study.removeParticipantAnnotationType(id)
  }

  describe("A study's annotation type set") {

    annotationTypeSetSharedBehaviour

  }

}
