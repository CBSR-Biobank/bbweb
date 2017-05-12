package org.biobank.controllers

import org.slf4j.LoggerFactory
import org.scalatest.{FunSpec, MustMatchers }
import org.scalatest.Inside._
import org.scalatest.prop.TableDrivenPropertyChecks._

class QueryStringParserSpec extends FunSpec with MustMatchers {

  val log = LoggerFactory.getLogger(this.getClass)

  describe("QueryStringParser") {

    it("must not fail for an empty string") {
      val result = QueryStringParser("")
      inside (result) { case Some(expressions) =>
        expressions must have size (0)
      }
    }

    it("must parse a single expression") {
      val result = QueryStringParser(s"foo=bar")
      inside (result) { case Some(expressions) =>
        expressions must have size (1)
        expressions("foo") must be ("bar")
      }
    }

    it("must parse a single empty expression") {
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

    it("must parse a multiple expressions") {
      val result = QueryStringParser(s"foo1=bar1&foo2=bar2")
      inside (result) { case Some(expressions) =>
        expressions must have size (2)
        expressions("foo1") must be ("bar1")
        expressions("foo2") must be ("bar2")
      }
    }

    it("must parse an RSQL expression, including expressions with single and double quotes") {
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

    it("must parse multiple RSQL expressions") {
      val result = QueryStringParser(s"a=foo1::bar1,foo2::bar2&b=foo3:in:(bar1,bar2)")
      inside (result) { case Some(expressions) =>
        expressions must have size (2)
        expressions("a") must be ("foo1::bar1,foo2::bar2")
        expressions("b") must be ("foo3:in:(bar1,bar2)")
      }
    }

  }
}
