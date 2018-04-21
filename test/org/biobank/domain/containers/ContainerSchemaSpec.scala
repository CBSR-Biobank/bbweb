package org.biobank.domain.containerType

import java.time.OffsetDateTime
import org.biobank.domain.containers._
import org.biobank.domain.DomainSpec
import org.biobank.fixture.NameGenerator
import org.slf4j.LoggerFactory
import scalaz.Scalaz._

class ContainerSchemaSpec extends DomainSpec {
  import org.biobank.TestUtils._
  import org.biobank.matchers.EntityMatchers._

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

        s must beEntityWithTimeStamps(OffsetDateTime.now, None, 5L)

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

        updatedContainerType must beEntityWithTimeStamps(OffsetDateTime.now, None, 5L)
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

        updatedContainerType must beEntityWithTimeStamps(OffsetDateTime.now, None, 5L)
      }
    }

  }

  describe("A container schema") {

    def createFrom(schema: ContainerSchema) = {
      ContainerSchema.create(id          = schema.id,
                             version     = schema.version,
                             name        = schema.name,
                             description = schema.description,
                             shared      = schema.shared)
    }

    it("not be created with an empty id") {
      val schema = factory.createContainerSchema.copy(id = ContainerSchemaId(""))
      createFrom(schema) mustFail "IdRequired"
    }

    it("not be created with an invalid version") {
      val schema = factory.createContainerSchema.copy(version = -2)
      createFrom(schema) mustFail "InvalidVersion"
    }

    it("not be created with an null or empty name") {
      var schema = factory.createContainerSchema.copy(name = null)
      createFrom(schema) mustFail "InvalidName"

      schema = factory.createContainerSchema.copy(name = "")
      createFrom(schema) mustFail "InvalidName"
    }

    it("not be created with an empty description option") {
      var schema = factory.createContainerSchema.copy(description = Some(null))
      createFrom(schema) mustFail "InvalidDescription"

      schema = factory.createContainerSchema.copy(description = Some(""))
      createFrom(schema) mustFail "InvalidDescription"
    }

    it("have more than one validation fail") {
      val schema = factory.createContainerSchema.copy(version = -2, name = "")
      createFrom(schema) mustFail ("InvalidVersion",  "InvalidName")
    }

  }

}
