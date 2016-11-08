package org.biobank.service

import org.biobank.infrastructure.{AscendingOrder, DescendingOrder}
import org.slf4j.LoggerFactory
import org.scalatest.Inside._
import org.scalatest.OptionValues._
import org.scalatest.{FreeSpec, MustMatchers }

class QuerySortParserSpec extends FreeSpec with MustMatchers {
  import QuerySortParserGrammar._

  val log = LoggerFactory.getLogger(this.getClass)

  "QuerySortParserSpec" - {

    "must fail for an empty string" in {
      val result = QuerySortParser("")
      result mustBe None
    }

    "must parse a single sort field" in {
      val result = QuerySortParser("foo")
      result.value must have length (1)
      inside (result.value(0)) { case SortExpression(name, sortOrder) =>
        name must be ("foo")
        sortOrder must be (AscendingOrder)
      }
    }

    "must parse a single sort field with descending order" in {
      val result = QuerySortParser("-bar")
      result.value must have length (1)
      inside (result.value(0)) { case SortExpression(name, sortOrder) =>
        name must be ("bar")
        sortOrder must be (DescendingOrder)
      }
    }

    "must parse multiple fields" in {
      val values = List("foo", "bar")
      val result = QuerySortParser(s"${values(0)}|-${values(1)}")
      result.value must have length (2)
      result.value.zipWithIndex.foreach { case (expression, index) =>
        inside (expression) { case SortExpression(name, sortOrder) =>
          name must be (values(index))
          sortOrder must be (if (index == 0 ) AscendingOrder else DescendingOrder)
        }
      }
    }

    "must fail for invalid input" in {
      val result = QuerySortParser("foo,bar")
      result mustBe None
    }
 }

}
