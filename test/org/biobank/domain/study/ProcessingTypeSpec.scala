package org.biobank.domain.study

import org.biobank.infrastructure._
import org.biobank.fixture.NameGenerator

import org.scalatest.WordSpecLike
import org.scalatest.Matchers
import org.slf4j.LoggerFactory
import scalaz._
import scalaz.Scalaz._

class ProcessingTypeSpec extends WordSpecLike with Matchers {

  val log = LoggerFactory.getLogger(this.getClass)

  val nameGenerator = new NameGenerator(this.getClass)

  "A processing type" can {

    "be created" in {
      fail
    }

  }

}
