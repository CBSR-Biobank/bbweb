package org.biobank.domain.study

import org.biobank.domain.{
  AnnotationType,
  AnnotationTypeSetSharedSpec,
  DomainSpec,
  DomainValidation
}
import org.biobank.fixture.NameGenerator

import org.slf4j.LoggerFactory
import com.github.nscala_time.time.Imports._
import org.scalatest.Tag
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


  "A study" can {

    "be created" in {
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
        checkTimeStamps(s, DateTime.now, None)
      }
    }

    "have it's name updated" in {
      val study = factory.createDisabledStudy
      val name = nameGenerator.next[Study]

      study.withName(name) mustSucceed { updatedStudy =>
        updatedStudy must have (
          'id          (study.id),
          'version     (study.version + 1),
          'name        (name),
          'description (study.description)
        )

        checkTimeStamps(updatedStudy, DateTime.now, DateTime.now)
      }
    }

    "have it's description updated" in {
      val study = factory.createDisabledStudy
      val description = Some(nameGenerator.next[Study])

      study.withDescription(description) mustSucceed { updatedStudy =>
        updatedStudy must have (
          'id          (study.id),
          'version     (study.version + 1),
          'name        (study.name),
          'description (description)
        )

        checkTimeStamps(updatedStudy, DateTime.now, DateTime.now)
      }
    }

    "be enabled" in {
      val study = factory.createDisabledStudy
      study.enable mustSucceed { enabledStudy =>
        enabledStudy mustBe a[EnabledStudy]
        enabledStudy.timeAdded mustBe (study.timeAdded)
      }
    }

    "when disabled, can be enabled" in {
      val study = factory.createEnabledStudy
      study.disable mustSucceed { disabledStudy =>
        disabledStudy mustBe a[DisabledStudy]
        disabledStudy.timeAdded mustBe (study.timeAdded)
      }
    }

    "be retired" in {
      val study = factory.createDisabledStudy
      study.retire mustSucceed { retiredStudy =>
        retiredStudy mustBe a[RetiredStudy]
        retiredStudy.timeAdded mustBe (study.timeAdded)
      }
    }

    "be unretired" in {
      val study = factory.createRetiredStudy
      study.unretire() mustSucceed { disabledStudy =>
        disabledStudy mustBe a[DisabledStudy]
        disabledStudy.timeAdded mustBe (study.timeAdded)
      }
    }

  }

  "A study" must {

    "not be created with an empty id" in {
      val study = factory.createDisabledStudy.copy(id = StudyId(""))
      createFrom(study) mustFail "IdRequired"
    }

    "not be created with an invalid version" in {
      val study = factory.createDisabledStudy.copy(version = -2L)
      createFrom(study) mustFail "InvalidVersion"
    }

    "not be created with an null or empty name" in {
      var study = factory.createDisabledStudy.copy(name = null)
      createFrom(study) mustFail "InvalidName"

      study = factory.createDisabledStudy.copy(name = "")
      createFrom(study) mustFail "InvalidName"
    }

    "not be created with an empty description" in {
      var study = factory.createDisabledStudy.copy(description = Some(null))
      createFrom(study) mustFail "InvalidDescription"

      study = factory.createDisabledStudy.copy(description = Some(""))
      createFrom(study) mustFail "InvalidDescription"
    }

    "have more than one validation fail" in {
      val study = factory.createDisabledStudy.copy(version = -2L, name = "")
      createFrom(study) mustFail ("InvalidVersion",  "InvalidName")
    }

    "not be enabled without prior configuration" in {
      val study = factory.createDisabledStudy
      study.enable mustFail "no collection event types"
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

  override def removeAnnotationType(study:    DisabledStudy,
                                    uniqueId: String)
      : DomainValidation[DisabledStudy] = {
    study.removeParticipantAnnotationType(uniqueId)
  }

  "A study's annotation type set" must {

    annotationTypeSetSharedBehaviour

  }

}
