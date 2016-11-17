package org.biobank.controllers

import org.slf4j.LoggerFactory
import org.scalatest.{FreeSpec, MustMatchers }
import org.scalatest.Inside._
import org.scalatest.prop.TableDrivenPropertyChecks._

class QueryStringParserSpec extends FreeSpec with MustMatchers {

  val log = LoggerFactory.getLogger(this.getClass)

  "QueryStringParser" - {

    "must not fail for an empty string" in {
      val result = QueryStringParser("")
      inside (result) { case Some(expressions) =>
        expressions must have size (0)
      }
    }

    "must parse a single expression" in {
      val result = QueryStringParser(s"foo=bar")
      inside (result) { case Some(expressions) =>
        expressions must have size (1)
        expressions("foo") must be ("bar")
      }
    }

    "must parse a single empty expression" in {
      val testStrings = Table("expression",
                              s"""foo=''""",
                              s"""foo=""""")
      forAll(testStrings) { testString =>
        val result = QueryStringParser(testString)
        inside (result) { case Some(expressions) =>
          expressions must have size (1)
          expressions("foo") must be ("")
        }
      }
    }

    "must parse a multiple expressions" in {
      val result = QueryStringParser(s"foo1=bar1&foo2=bar2")
      inside (result) { case Some(expressions) =>
        expressions must have size (2)
        expressions("foo1") must be ("bar1")
        expressions("foo2") must be ("bar2")
      }
    }

    "must parse an RSQL expression, including expressions with single and double quotes" in {
      val testStrings = Table("expression",
                              "a=foo1::bar1,foo2::bar2",
                              "a='foo1::bar1,foo2::bar2'",
                              """a="foo1::bar1,foo2::bar2"""")
      forAll(testStrings) { testString =>
        val result = QueryStringParser(testString)
        inside (result) { case Some(expressions) =>
          expressions must have size (1)
          expressions("a") must be ("foo1::bar1,foo2::bar2")
        }
      }
    }

    "must parse multiple RSQL expressions" in {
      val result = QueryStringParser(s"a=foo1::bar1,foo2::bar2&b=foo3:in:(bar1,bar2)")
      log.info(s"----> $result")

      inside (result) { case Some(expressions) =>
        expressions must have size (2)
        expressions("a") must be ("foo1::bar1,foo2::bar2")
        expressions("b") must be ("foo3:in:(bar1,bar2)")
      }
    }

  }
}
