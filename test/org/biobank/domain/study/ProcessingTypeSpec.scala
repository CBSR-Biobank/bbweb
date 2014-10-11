package org.biobank.domain.study

import org.biobank.domain.DomainSpec
import org.biobank.infrastructure._
import org.biobank.fixture.NameGenerator

import org.slf4j.LoggerFactory
import com.github.nscala_time.time.Imports._
import scalaz._
import scalaz.Scalaz._

class ProcessingTypeSpec extends DomainSpec {

  val log = LoggerFactory.getLogger(this.getClass)

  val nameGenerator = new NameGenerator(this.getClass)

  "A processing type" can {

    "be created" in {
      val disabledStudy = factory.defaultDisabledStudy
      val processingTypeId = processingTypeRepository.nextIdentity
      val name = nameGenerator.next[ProcessingType]
      val description = Some(nameGenerator.next[ProcessingType])
      val enabled = true

      val validation = ProcessingType.create(
        disabledStudy.id, processingTypeId, -1L, org.joda.time.DateTime.now, name, description, enabled)
      validation mustBe ('success)
      validation map { processingType =>
        processingType mustBe a [ProcessingType]
        processingType must have (
          'studyId (disabledStudy.id),
          'version (0L),
          'name (name),
          'description (description),
          'enabled (enabled)
        )

        (processingType.timeAdded to DateTime.now).millis must be < 100L
        processingType.timeModified mustBe (None)
      }
    }

    "be updated" in {
      val processingType = factory.createProcessingType

      val name = nameGenerator.next[ProcessingType]
      val description = Some(nameGenerator.next[ProcessingType])
      val enabled = !processingType.enabled

      val validation = processingType.update(name, description, enabled)
      validation mustBe ('success)
      validation map { pt2 =>
        pt2 mustBe a [ProcessingType]
        pt2 must have (
          'studyId (processingType.studyId),
          'id (processingType.id),
          'version (processingType.version + 1),
          'name (name),
          'description (description),
          'enabled (enabled)
        )

        pt2.timeAdded mustBe (processingType.timeAdded)
        pt2.timeModified mustBe (None)
      }
    }
  }

  "A processing type" must {

    "not be created with an empty study id" in {
      val studyId = StudyId("")
      val processingTypeId = processingTypeRepository.nextIdentity
      val name = nameGenerator.next[ProcessingType]
      val description = Some(nameGenerator.next[ProcessingType])
      val enabled = true

      val validation = ProcessingType.create(
        studyId, processingTypeId, -1L, org.joda.time.DateTime.now, name, description, enabled)
      validation mustBe('failure)
      validation.swap.map { err =>
          err.list must (have length 1 and contain("IdRequired"))
      }
    }


    "not be created with an empty id" in {
      val disabledStudy = factory.defaultDisabledStudy
      val processingTypeId = ProcessingTypeId("")
      val name = nameGenerator.next[ProcessingType]
      val description = Some(nameGenerator.next[ProcessingType])
      val enabled = true

      val validation = ProcessingType.create(
        disabledStudy.id, processingTypeId, -1L, org.joda.time.DateTime.now, name, description, enabled)
      validation mustBe('failure)
      validation.swap.map { err =>
          err.list must (have length 1 and contain("IdRequired"))
      }
    }

    "not be created with an invalid version" in {
      val disabledStudy = factory.defaultDisabledStudy
      val processingTypeId = processingTypeRepository.nextIdentity
      val name = nameGenerator.next[ProcessingType]
      val description = Some(nameGenerator.next[ProcessingType])
      val enabled = true

      val validation = ProcessingType.create(
        disabledStudy.id, processingTypeId, -2L, org.joda.time.DateTime.now, name, description, enabled)
      validation mustBe('failure)
      validation.swap.map { err =>
          err.list must (have length 1 and contain("InvalidVersion"))
      }
    }

    "not be created with an null or empty name" in {
      val disabledStudy = factory.defaultDisabledStudy
      val processingTypeId = processingTypeRepository.nextIdentity
      var name: String = null
      val description = Some(nameGenerator.next[ProcessingType])
      val enabled = true

      val validation = ProcessingType.create(
        disabledStudy.id, processingTypeId, -1L, org.joda.time.DateTime.now, name, description, enabled)
      validation mustBe ('failure)
      validation.swap.map { err =>
          err.list must (have length 1 and contain("NameRequired"))
      }

      name = ""
      val validation2 = ProcessingType.create(
        disabledStudy.id, processingTypeId, -1L, org.joda.time.DateTime.now, name, description, enabled)
      validation2 mustBe ('failure)
      validation2.swap.map { err =>
          err.list must (have length 1 and contain("NameRequired"))
      }
    }

    "not be created with an empty description option" in {
      val disabledStudy = factory.defaultDisabledStudy
      val processingTypeId = processingTypeRepository.nextIdentity
      val name = nameGenerator.next[ProcessingType]
      var description: Option[String] = Some(null)
      val enabled = true

      val validation = ProcessingType.create(
        disabledStudy.id, processingTypeId, -1L, org.joda.time.DateTime.now, name, description, enabled)
      validation mustBe ('failure)
      validation.swap.map { err =>
          err.list must (have length 1 and contain("NonEmptyDescription"))
      }

      description = Some("")
      val validation2 = ProcessingType.create(
        disabledStudy.id, processingTypeId, -1L, org.joda.time.DateTime.now, name, description, enabled)
      validation2 mustBe ('failure)
      validation2.swap.map { err =>
          err.list must (have length 1 and contain("NonEmptyDescription"))
      }
    }

    "have more than one validation fail" in {
      val disabledStudy = factory.defaultDisabledStudy
      val processingTypeId = processingTypeRepository.nextIdentity
      val name = nameGenerator.next[ProcessingType]
      var description: Option[String] = Some(null)
      val enabled = true

      val validation = ProcessingType.create(
        disabledStudy.id, processingTypeId, -2L, org.joda.time.DateTime.now, name, description, enabled)
      validation mustBe ('failure)
      validation.swap.map { err =>
          err.list must have length 2
          err.list(0) mustBe ("InvalidVersion")
          err.list(1) mustBe ("NonEmptyDescription")
      }
    }
  }

}
