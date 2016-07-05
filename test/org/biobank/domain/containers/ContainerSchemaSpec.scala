package org.biobank.domain.containerType

import org.biobank.domain.containers._
import org.biobank.domain.DomainSpec
import org.biobank.fixture.NameGenerator

import org.slf4j.LoggerFactory
import com.github.nscala_time.time.Imports._
import scalaz.Scalaz._

class ContainerSchemaSpec extends DomainSpec {
  import org.biobank.TestUtils._

  val log = LoggerFactory.getLogger(this.getClass)

  val nameGenerator = new NameGenerator(this.getClass)

  "A container schema" can {

    "be created" in {
      val containerType = factory.createContainerSchema
      val v = ContainerSchema.create(id          = containerType.id,
                                     version     = 0L,
                                     name        = containerType.name,
                                     description = containerType.description,
                                     shared      = containerType.shared)

      v mustSucceed { s =>
        s mustBe a[ContainerSchema]

        s must have (
          'id          (containerType.id),
          'version     (0L),
          'name        (containerType.name),
          'description (containerType.description)
        )

        checkTimeStamps(s, DateTime.now, None)
      }
    }

    "have it's name updated" in {

      val containerType = factory.createContainerSchema
      val name = nameGenerator.next[ContainerType]

      containerType.withName(name) mustSucceed { updatedContainerType =>
        updatedContainerType must have (
          'id          (containerType.id),
          'version     (containerType.version + 1),
          'name        (name),
          'description (containerType.description)
        )

        checkTimeStamps(updatedContainerType, DateTime.now, None)
      }
    }

    "have it's description updated" in {
      val containerType = factory.createContainerSchema
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

  }

  "A container schema" must {

    "not be created with an empty id" in {
      val v = ContainerSchema.create(
        id          = ContainerSchemaId(""),
        version     = 0L,
        name        = nameGenerator.next[ContainerType],
        description = Some(nameGenerator.next[ContainerType]),
        shared      = true)

      v mustFail "IdRequired"
    }

    "not be created with an invalid version" in {
      val v = ContainerSchema.create(
        id          = ContainerSchemaId(nameGenerator.next[ContainerType]),
        version     = -2,
        name        = nameGenerator.next[ContainerType],
        description = Some(nameGenerator.next[ContainerType]),
        shared      = true)

      v mustFail "InvalidVersion"
    }

    "not be created with an null or empty name" in {
      var v = ContainerSchema.create(
        id          = ContainerSchemaId(nameGenerator.next[ContainerType]),
        version     = 0L,
        name        = null,
        description = Some(nameGenerator.next[ContainerType]),
        shared      = true)

      v mustFail "InvalidName"

      v = ContainerSchema.create(
        id          = ContainerSchemaId(nameGenerator.next[ContainerType]),
        version     = 0L,
        name        = "",
        description = Some(nameGenerator.next[ContainerType]),
        shared      = true)

      v mustFail "InvalidName"
    }

    "not be created with an empty description option" in {
      var v = ContainerSchema.create(
        id          = ContainerSchemaId(nameGenerator.next[ContainerType]),
        version     = 0L,
        name        = nameGenerator.next[ContainerType],
        description = Some(null),
        shared      = true)

      v mustFail "InvalidDescription"

      v = ContainerSchema.create(
        id          = ContainerSchemaId(nameGenerator.next[ContainerType]),
        version     = 0L,
        name        = nameGenerator.next[ContainerType],
        description = Some(""),
        shared      = true)

      v mustFail "InvalidDescription"
    }

    "have more than one validation fail" in {
      val v = ContainerSchema.create(
        id          = ContainerSchemaId(nameGenerator.next[ContainerType]),
        version     = -2,
        name        = null,
        description = Some(nameGenerator.next[ContainerType]),
        shared      = true)
      v mustFail ("InvalidVersion",  "InvalidName")
    }

  }

}
