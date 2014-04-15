package org.biobank.domain.study

import org.biobank.domain.AnnotationTypeId
import org.biobank.fixture.NameGenerator
import org.biobank.domain.AnnotationValueType

import org.scalatest.WordSpecLike
import org.scalatest.Matchers
import scalaz._
import scalaz.Scalaz._


class CollectionEventAnnotationTypeSpec extends WordSpecLike with Matchers {

  val nameGenerator = new NameGenerator(this.getClass.getName)

  "A collection event annotation type" can {

    "be created" in {
    }

  }

  "A collection event annotation type" can {

    "not be created with an empty study id" in {
      fail
    }

    "not be created with an empty id" in {
      fail
    }

    "not be created with an invalid version" in {
      fail
    }

    "not be created with an null or empty name" in {
      fail
    }

    "not be created with an empty description option" in {
      fail
    }

    "not be created with an negative max value count" in {
      fail
    }


  }
}
