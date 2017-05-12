package org.biobank.service

import org.biobank.infrastructure.{AscendingOrder, DescendingOrder}
import org.slf4j.LoggerFactory
import org.scalatest.Inside._
import org.scalatest.OptionValues._
import org.scalatest.{FunSpec, MustMatchers }

class QuerySortParserSpec extends FunSpec with MustMatchers {
  import QuerySortParserGrammar._

  val log = LoggerFactory.getLogger(this.getClass)

  describe("QuerySortParserSpec") {

    it("must fail for an empty string") {
      val result = QuerySortParser(new SortString(""))
      inside (result) { case Some(expressions) =>
        expressions must have size (0)
      }
    }

    it("must parse a single sort field") {
      val result = QuerySortParser(new SortString("foo"))
      result.value must have length (1)
      inside (result.value(0)) { case SortExpression(name, sortOrder) =>
        name must be ("foo")
        sortOrder must be (AscendingOrder)
      }
    }

    it("must parse a single sort field with descending order") {
      val result = QuerySortParser(new SortString("-bar"))
      result.value must have length (1)
      inside (result.value(0)) { case SortExpression(name, sortOrder) =>
        name must be ("bar")
        sortOrder must be (DescendingOrder)
      }
    }

    it("must parse multiple fields") {
      val values = List("foo", "bar")
      val result = QuerySortParser(new SortString(s"${values(0)}|-${values(1)}"))
      result.value must have length (2)
      result.value.zipWithIndex.foreach { case (expression, index) =>
        inside (expression) { case SortExpression(name, sortOrder) =>
          name must be (values(index))
          sortOrder must be (if (index == 0 ) AscendingOrder else DescendingOrder)
        }
      }
    }

    it("must fail for invalid input") {
      val result = QuerySortParser(new SortString("foo,bar"))
      result mustBe None
    }
 }

}
