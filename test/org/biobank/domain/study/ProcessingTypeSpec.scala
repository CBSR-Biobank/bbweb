package org.biobank.domain.study

import org.biobank.domain.DomainSpec
import org.biobank.fixture.NameGenerator

import javax.inject.{Inject}
import org.slf4j.LoggerFactory
import com.github.nscala_time.time.Imports._
import scalaz.Scalaz._

class ProcessingTypeSpec @Inject() (
  val processingTypeRepository: ProcessingTypeRepository)
    extends DomainSpec {
  import org.biobank.TestUtils._

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
        disabledStudy.id, processingTypeId, 0L, name, description, enabled)
      validation mustSucceed { processingType =>
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
      validation mustSucceed { pt2 =>
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
        pt2.timeModified must not be (None)
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
        studyId, processingTypeId, 0L, name, description, enabled)
      validation mustFail "IdRequired"
    }


    "not be created with an empty id" in {
      val disabledStudy = factory.defaultDisabledStudy
      val processingTypeId = ProcessingTypeId("")
      val name = nameGenerator.next[ProcessingType]
      val description = Some(nameGenerator.next[ProcessingType])
      val enabled = true

      val validation = ProcessingType.create(
        disabledStudy.id, processingTypeId, 0L, name, description, enabled)
      validation mustFail "IdRequired"
    }

    "not be created with an invalid version" in {
      val disabledStudy = factory.defaultDisabledStudy
      val processingTypeId = processingTypeRepository.nextIdentity
      val name = nameGenerator.next[ProcessingType]
      val description = Some(nameGenerator.next[ProcessingType])
      val enabled = true

      val validation = ProcessingType.create(
        disabledStudy.id, processingTypeId, -2L, name, description, enabled)
      validation mustFail "InvalidVersion"
    }

    "not be created with an null or empty name" in {
      val disabledStudy = factory.defaultDisabledStudy
      val processingTypeId = processingTypeRepository.nextIdentity
      var name: String = null
      val description = Some(nameGenerator.next[ProcessingType])
      val enabled = true

      val validation = ProcessingType.create(
        disabledStudy.id, processingTypeId, 0L, name, description, enabled)
      validation mustFail "NameRequired"

      name = ""
      val validation2 = ProcessingType.create(
        disabledStudy.id, processingTypeId, 0L, name, description, enabled)
      validation2 mustFail "NameRequired"
    }

    "not be created with an empty description option" in {
      val disabledStudy = factory.defaultDisabledStudy
      val processingTypeId = processingTypeRepository.nextIdentity
      val name = nameGenerator.next[ProcessingType]
      var description: Option[String] = Some(null)
      val enabled = true

      val validation = ProcessingType.create(
        disabledStudy.id, processingTypeId, 0L, name, description, enabled)
      validation mustFail "InvalidDescription"

      description = Some("")
      val validation2 = ProcessingType.create(
        disabledStudy.id, processingTypeId, 0L, name, description, enabled)
      validation2 mustFail "InvalidDescription"
    }

    "have more than one validation fail" in {
      val disabledStudy = factory.defaultDisabledStudy
      val processingTypeId = processingTypeRepository.nextIdentity
      val name = nameGenerator.next[ProcessingType]
      val description: Option[String] = Some(null)
      val enabled = true

      val validation = ProcessingType.create(
        disabledStudy.id, processingTypeId, -2L, name, description, enabled)
      validation.mustFail("InvalidVersion", "InvalidDescription")
    }
  }

}
