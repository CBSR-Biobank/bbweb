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

  describe("A container schema") {

    it("be created") {
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

    it("have it's name updated") {

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

    it("have it's description updated") {
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

  describe("A container schema") {

    it("not be created with an empty id") {
      val v = ContainerSchema.create(
        id          = ContainerSchemaId(""),
        version     = 0L,
        name        = nameGenerator.next[ContainerType],
        description = Some(nameGenerator.next[ContainerType]),
        shared      = true)

      v mustFail "IdRequired"
    }

    it("not be created with an invalid version") {
      val v = ContainerSchema.create(
        id          = ContainerSchemaId(nameGenerator.next[ContainerType]),
        version     = -2,
        name        = nameGenerator.next[ContainerType],
        description = Some(nameGenerator.next[ContainerType]),
        shared      = true)

      v mustFail "InvalidVersion"
    }

    it("not be created with an null or empty name") {
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

    it("not be created with an empty description option") {
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

    it("have more than one validation fail") {
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
