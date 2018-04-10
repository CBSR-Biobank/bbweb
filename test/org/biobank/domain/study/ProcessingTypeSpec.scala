package org.biobank.domain.studies

import java.time.OffsetDateTime
import org.biobank.domain.DomainSpec
import org.biobank.fixture.NameGenerator
import org.biobank.TestUtils
import javax.inject.Inject
import org.slf4j.LoggerFactory

class ProcessingTypeSpec @Inject() (val processingTypeRepository: ProcessingTypeRepository)
    extends DomainSpec {
  import org.biobank.TestUtils._

  val log = LoggerFactory.getLogger(this.getClass)

  val nameGenerator = new NameGenerator(this.getClass)

  describe("A processing type") {

    it("be created") {
      val disabledStudy = factory.defaultDisabledStudy
      val processingTypeId = processingTypeRepository.nextIdentity
      val name = nameGenerator.next[ProcessingType]
      val description = Some(nameGenerator.next[ProcessingType])
      val enabled = true

      val validation = ProcessingType.create(disabledStudy.id,
                                             processingTypeId,
                                             0L,
                                             name,
                                             description,
                                             enabled)

      validation mustSucceed { processingType =>
        processingType mustBe a [ProcessingType]
        processingType must have (
          'studyId (disabledStudy.id),
          'version (0L),
          'name (name),
          'description (description),
          'enabled (enabled)
        )

        TestUtils.checkTimeStamps(processingType.timeAdded, OffsetDateTime.now)
        processingType.timeModified mustBe (None)
        ()
      }
    }

    it("be updated") {
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
        ()
      }
    }
  }

  describe("A processing type") {

    it("not be created with an empty study id") {
      val studyId = StudyId("")
      val processingTypeId = processingTypeRepository.nextIdentity
      val name = nameGenerator.next[ProcessingType]
      val description = Some(nameGenerator.next[ProcessingType])
      val enabled = true

      val validation = ProcessingType.create(studyId,
                                             processingTypeId,
                                             0L,
                                             name,
                                             description,
                                             enabled)
      validation mustFail "IdRequired"
    }


    it("not be created with an empty id") {
      val disabledStudy = factory.defaultDisabledStudy
      val processingTypeId = ProcessingTypeId("")
      val name = nameGenerator.next[ProcessingType]
      val description = Some(nameGenerator.next[ProcessingType])
      val enabled = true

      val validation = ProcessingType.create(disabledStudy.id,
                                             processingTypeId,
                                             0L,
                                             name,
                                             description,
                                             enabled)
      validation mustFail "IdRequired"
    }

    it("not be created with an invalid version") {
      val disabledStudy = factory.defaultDisabledStudy
      val processingTypeId = processingTypeRepository.nextIdentity
      val name = nameGenerator.next[ProcessingType]
      val description = Some(nameGenerator.next[ProcessingType])
      val enabled = true

      val validation = ProcessingType.create(disabledStudy.id,
                                             processingTypeId,
                                             -2L,
                                             name,
                                             description,
                                             enabled)
      validation mustFail "InvalidVersion"
    }

    it("not be created with an null or empty name") {
      val disabledStudy = factory.defaultDisabledStudy
      val processingTypeId = processingTypeRepository.nextIdentity
      var name: String = null
      val description = Some(nameGenerator.next[ProcessingType])
      val enabled = true

      val validation = ProcessingType.create(disabledStudy.id,
                                             processingTypeId,
                                             0L,
                                             name,
                                             description,
                                             enabled)
      validation mustFail "NameRequired"

      name = ""
      val validation2 = ProcessingType.create(disabledStudy.id,
                                              processingTypeId,
                                              0L,
                                              name,
                                              description,
                                              enabled)
      validation2 mustFail "NameRequired"
    }

    it("not be created with an empty description option") {
      val disabledStudy = factory.defaultDisabledStudy
      val processingTypeId = processingTypeRepository.nextIdentity
      val name = nameGenerator.next[ProcessingType]
      var description: Option[String] = Some(null)
      val enabled = true

      val validation = ProcessingType.create(disabledStudy.id,
                                             processingTypeId,
                                             0L,
                                             name,
                                             description,
                                             enabled)
      validation mustFail "InvalidDescription"

      description = Some("")
      val validation2 = ProcessingType.create(disabledStudy.id,
                                              processingTypeId,
                                              0L,
                                              name,
                                              description,
                                              enabled)
      validation2 mustFail "InvalidDescription"
    }

    it("have more than one validation fail") {
      val disabledStudy = factory.defaultDisabledStudy
      val processingTypeId = processingTypeRepository.nextIdentity
      val name = nameGenerator.next[ProcessingType]
      val description: Option[String] = Some(null)
      val enabled = true

      val validation = ProcessingType.create(disabledStudy.id,
                                             processingTypeId,
                                             -2L,
                                             name,
                                             description,
                                             enabled)
      validation.mustFail("InvalidVersion", "InvalidDescription")
    }
  }

}
