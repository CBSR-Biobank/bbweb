package org.biobank.domain.containerType

import org.biobank.domain.containers._
import org.biobank.domain.centre.CentreId
import org.biobank.domain.DomainSpec
import org.biobank.fixture.NameGenerator

import org.slf4j.LoggerFactory
import com.github.nscala_time.time.Imports._
import org.scalatest.Tag
import scalaz.Scalaz._

class ContainerTypeSpec extends DomainSpec {
  import org.biobank.TestUtils._

  val log = LoggerFactory.getLogger(this.getClass)

  val nameGenerator = new NameGenerator(this.getClass)

  "A containerType" can {

    "be created" in {
      val containerType = factory.createEnabledContainerType
      val v = EnabledContainerType.create(id          = containerType.id,
                                          centreId    = containerType.centreId,
                                          schemaId    = containerType.schemaId,
                                          version     = -1,
                                          name        = containerType.name,
                                          description = containerType.description,
                                          shared      = containerType.shared)

      v mustSucceed { s =>
        s mustBe a[EnabledContainerType]

        s must have (
          'id          (containerType.id),
          'centreId    (containerType.centreId),
          'schemaId    (containerType.schemaId),
          'version     (0L),
          'name        (containerType.name),
          'description (containerType.description)
        )

        checkTimeStamps(s, DateTime.now, None)
      }
    }

    "have it's centreId updated" in {
      val containerType = factory.createEnabledContainerType
      val centreId = Some(CentreId(nameGenerator.next[ContainerType]))

      containerType.withCentreId(centreId) mustSucceed { updatedContainerType =>
        updatedContainerType must have (
          'id          (containerType.id),
          'centreId    (centreId),
          'schemaId    (containerType.schemaId),
          'version     (containerType.version + 1),
          'name        (containerType.name),
          'description (containerType.description)
        )

        checkTimeStamps(updatedContainerType, DateTime.now, None)
      }
    }

    "have it's schemaId updated" in {
      val containerType = factory.createEnabledContainerType
      val schemaId = ContainerSchemaId(nameGenerator.next[ContainerType])

      containerType.withSchemaId(schemaId) mustSucceed { updatedContainerType =>
        updatedContainerType must have (
          'id          (containerType.id),
          'centreId    (containerType.centreId),
          'schemaId    (schemaId),
          'version     (containerType.version + 1),
          'name        (containerType.name),
          'description (containerType.description)
        )

        checkTimeStamps(updatedContainerType, DateTime.now, None)
      }
    }

    "have it's name updated" in {
      val containerType = factory.createEnabledContainerType
      val name = nameGenerator.next[ContainerType]

      containerType.withName(name) mustSucceed { updatedContainerType =>
        updatedContainerType must have (
          'id          (containerType.id),
          'centreId    (containerType.centreId),
          'schemaId    (containerType.schemaId),
          'version     (containerType.version + 1),
          'name        (name),
          'description (containerType.description)
        )

        checkTimeStamps(updatedContainerType, DateTime.now, None)
      }
    }

    "have it's description updated" in {
      val containerType = factory.createEnabledContainerType
      val description = Some(nameGenerator.next[ContainerType])

      containerType.withDescription(description) mustSucceed { updatedContainerType =>
        updatedContainerType must have (
          'id          (containerType.id),
          'version     (containerType.version + 1),
          'name        (containerType.name),
          'description (description)
        )

        checkTimeStamps(updatedContainerType, DateTime.now, None)
      }
    }

    "enabled a disabled container type" in {
      val containerType = factory.createDisabledContainerType
      containerType.enable mustSucceed { enabledContainerType =>
        enabledContainerType mustBe a[EnabledContainerType]
        enabledContainerType.timeAdded mustBe (containerType.timeAdded)
      }
    }

    "disable an enabled container type" in {
      val containerType = factory.createEnabledContainerType
      containerType.disable mustSucceed { disabledContainerType =>
        disabledContainerType mustBe a[EnabledContainerType]
        disabledContainerType.timeAdded mustBe (containerType.timeAdded)
      }
    }

  }

  "A containerType" must {

    "not be created with an empty id" in {
      val v = EnabledContainerType.create(
        id          = ContainerTypeId(""),
        centreId    = Some(CentreId(nameGenerator.next[ContainerType])),
        schemaId    = ContainerSchemaId(nameGenerator.next[ContainerType]),
        version     = -1,
        name        = nameGenerator.next[ContainerType],
        description = Some(nameGenerator.next[ContainerType]),
        shared      = true)

      v mustFail "IdRequired"
    }

    "not be created with an invalid centre id" in {
      val v = EnabledContainerType.create(
        id          = ContainerTypeId(nameGenerator.next[ContainerType]),
        centreId    = Some(CentreId("")),
        schemaId    = ContainerSchemaId(nameGenerator.next[ContainerType]),
        version     = -1,
        name        = nameGenerator.next[ContainerType],
        description = Some(nameGenerator.next[ContainerType]),
        shared      = true)

      v mustFail "CentreIdRequired"
    }

    "not be created with an invalid schema id" in {
      val v = EnabledContainerType.create(
        id          = ContainerTypeId(nameGenerator.next[ContainerType]),
        centreId    = Some(CentreId(nameGenerator.next[ContainerType])),
        schemaId    = ContainerSchemaId(""),
        version     = -1,
        name        = nameGenerator.next[ContainerType],
        description = Some(nameGenerator.next[ContainerType]),
        shared      = true)

      v mustFail "ContainerSchemaIdInvalid"
    }

    "not be created with an invalid version" in {
      val v = EnabledContainerType.create(
        id          = ContainerTypeId(nameGenerator.next[ContainerType]),
        centreId    = Some(CentreId(nameGenerator.next[ContainerType])),
        schemaId    = ContainerSchemaId(nameGenerator.next[ContainerType]),
        version     = -2,
        name        = nameGenerator.next[ContainerType],
        description = Some(nameGenerator.next[ContainerType]),
        shared      = true)

      v mustFail "InvalidVersion"
    }

    "not be created with an null or empty name" in {
      var v = EnabledContainerType.create(
        id          = ContainerTypeId(nameGenerator.next[ContainerType]),
        centreId    = Some(CentreId(nameGenerator.next[ContainerType])),
        schemaId    = ContainerSchemaId(nameGenerator.next[ContainerType]),
        version     = -1,
        name        = null,
        description = Some(nameGenerator.next[ContainerType]),
        shared      = true)

      v mustFail "InvalidName"

      v = EnabledContainerType.create(
        id          = ContainerTypeId(nameGenerator.next[ContainerType]),
        centreId    = Some(CentreId(nameGenerator.next[ContainerType])),
        schemaId    = ContainerSchemaId(nameGenerator.next[ContainerType]),
        version     = -1,
        name        = "",
        description = Some(nameGenerator.next[ContainerType]),
        shared      = true)

      v mustFail "InvalidName"
    }

    "not be created with an empty description option" in {
      var v = EnabledContainerType.create(
        id          = ContainerTypeId(nameGenerator.next[ContainerType]),
        centreId    = Some(CentreId(nameGenerator.next[ContainerType])),
        schemaId    = ContainerSchemaId(nameGenerator.next[ContainerType]),
        version     = -1,
        name        = nameGenerator.next[ContainerType],
        description = Some(null),
        shared      = true)

      v mustFail "InvalidDescription"

      v = EnabledContainerType.create(
        id          = ContainerTypeId(nameGenerator.next[ContainerType]),
        centreId    = Some(CentreId(nameGenerator.next[ContainerType])),
        schemaId    = ContainerSchemaId(nameGenerator.next[ContainerType]),
        version     = -1,
        name        = nameGenerator.next[ContainerType],
        description = Some(""),
        shared      = true)

      v mustFail "InvalidDescription"
    }

    "have more than one validation fail" in {
      var v = EnabledContainerType.create(
        id          = ContainerTypeId(nameGenerator.next[ContainerType]),
        centreId    = Some(CentreId(nameGenerator.next[ContainerType])),
        schemaId    = ContainerSchemaId(nameGenerator.next[ContainerType]),
        version     = -2,
        name        = null,
        description = Some(nameGenerator.next[ContainerType]),
        shared      = true)
      v mustFail ("InvalidVersion",  "InvalidName")
    }

  }

}
