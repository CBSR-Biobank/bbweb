package org.biobank.service

import org.slf4j.LoggerFactory
import org.scalatest.{FunSpec, MustMatchers }
import org.scalatest.OptionValues._
import org.scalatest.Inside._
import org.scalatest.matchers.{Matcher, MatchResult}
import org.scalatest.prop.TableDrivenPropertyChecks._
import Comparator._
import QueryFilterParserGrammar._

trait CustomMatchers {

  class ComparisonMatcher(expectedSelector:   String,
                          expectedComparator: Comparator,
                          expectedArguments:  List[String])
      extends Matcher[Expression] {

    val argsToString = "(" + expectedArguments.mkString(", ") + ")"

    def apply(left: Expression) =
      MatchResult(
        left match {
          case c: Comparison =>
            ((c.selector == expectedSelector)
               && (c.comparator == expectedComparator)
               && c.arguments.diff(expectedArguments).isEmpty)
          case _ => false
        },
        s"Expression $left is not a ArgumentComparison($expectedSelector, $expectedComparator, $argsToString)",
        s"Expression $left is a ArgumentComparison($expectedSelector, $expectedComparator, $argsToString))"
      )
  }

  class GroupMatcher extends Matcher[Expression] {

    def apply(left: Expression) = {
      MatchResult(
        left match {
          case Group(_) => true
          case _ => false
        },
        s"""Expression $left is not a Group"""",
        s"""Expression $left is a Group""""
      )
    }
  }

  def beComparison(expectedSelector:  String,
                   expectedComparator: Comparator,
                   expectedArgument:  String) =
    new ComparisonMatcher(expectedSelector, expectedComparator, List(expectedArgument))

  def beComparison(expectedSelector:  String,
                   expectedComparator: Comparator,
                   expectedArguments:  List[String]) =
    new ComparisonMatcher(expectedSelector, expectedComparator, expectedArguments)

  def beGroup = new GroupMatcher
}

object CustomMatchers extends CustomMatchers

class QueryFilterParserSpec extends FunSpec with MustMatchers with CustomMatchers {

  val log = LoggerFactory.getLogger(this.getClass)

  val comparisons = Table("comparator",
                          LessThan,
                          GreaterThan,
                          LessThanOrEqualTo,
                          GreaterThanOrEqualTo,
                          In,
                          NotIn,
                          NotEqualTo,
                          Equal)

  describe("QueryFilterParser") {

    it("must fail for an empty string") {
      val result = QueryFilterParser(new FilterString(""))
      result mustBe None
    }

    it("must parse a single comparison") {
      forAll(comparisons) { comparator =>
        val selector = "foo"
        val value = "bar"
        val result = QueryFilterParser(new FilterString(s"$selector$comparator$value"))
        result.value must beComparison(selector, comparator, value)
      }
    }

    it("must parse IN and OUT comparisons with arguments") {
      val comparisons = Table("comparator", In, NotIn)

      forAll(comparisons) { comparator =>
        val selector = "foo"
        val args = List("bar1", "bar2", "bar3")
        val stringToParse = s"""$selector$comparator(${args.mkString(",")})"""
        val result = QueryFilterParser(new FilterString(stringToParse))
        result.value must beComparison(selector, comparator, args)
      }
    }

    it("must parse IN and OUT comparisons with arguments within an OR expression") {
      val comparisons = Table("comparator", In, NotIn)

      forAll(comparisons) { comparator =>
        val selector = "foo"
        val args = List("bar1", "bar2", "bar3")
        val stringToParse = s"""foo::bar,$selector$comparator(${args.mkString(",")})"""
        val result = QueryFilterParser(new FilterString(stringToParse))
        inside(result.value) { case OrExpression(expressions) =>
          expressions must have length (2)
          expressions(1) must beComparison(selector, comparator, args)
        }
      }
    }

    it("must parse IN and OUT comparisons with arguments within an AND expression") {
      val comparisons = Table("comparator", In, NotIn)

      forAll(comparisons) { comparator =>
        val selector = "foo"
        val args = List("bar1", "bar2", "bar3")
        val stringToParse = s"""foo::bar;$selector$comparator(${args.mkString(",")})"""
        val result = QueryFilterParser(new FilterString(stringToParse))
        inside(result.value) { case AndExpression(expressions) =>
          expressions must have length (2)
          expressions(1) must beComparison(selector, comparator, args)
        }
      }
    }

    it("must parse an AND expression") {
      val selectors = List("foo1", "foo2")
      val values = List("bar1", "bar2")

      forAll(comparisons) { (comparator) =>
        val stringToParse = s"${selectors(0)}$comparator${values(0)};${selectors(1)}$comparator${values(1)}"
        val result = QueryFilterParser(new FilterString(stringToParse))
        inside (result.value) { case AndExpression(expressions) =>
          expressions must have length (2)
          expressions.zipWithIndex.foreach { case (comparison, index) =>
            comparison must beComparison(selectors(index), comparator, values(index))
          }
        }
      }
    }

    it("must parse an OR expression") {
      val selectors = List("foo1", "foo2")
      val values = List("bar1", "bar2")

      forAll(comparisons) { (comparator) =>
        val stringToParse = s"${selectors(0)}$comparator${values(0)},${selectors(1)}$comparator${values(1)}"
        val result = QueryFilterParser(new FilterString(stringToParse))
        inside (result.value) { case OrExpression(expressions) =>
          expressions must have length (2)
          expressions.zipWithIndex.foreach { case (comparison, index) =>
            comparison must beComparison(selectors(index), comparator, values(index))
          }
        }
      }
    }

    it("must parse an AND expression within an OR expresssion") {
      val stringToParse = s"(foo1::bar1;foo2::bar2),(foo3::bar3;foo4::bar4)"
      val result = QueryFilterParser(new FilterString(stringToParse))
      //log.info(s"-------> $result")
      inside (result.value) { case OrExpression(expressions) =>
        expressions must have length (2)
        expressions.zipWithIndex.foreach { case (andExpression, orIndex) =>
          inside (andExpression) { case AndExpression(expressions) =>
            expressions must have length (2)
            expressions.zipWithIndex.foreach { case (comparison, andIndex) =>
              val id = 2 * orIndex + andIndex + 1
              comparison must beComparison(s"foo$id", Comparator.Equal, s"bar$id")
            }
          }
        }
      }
    }

    it("must parse an OR expression within an AND expresssion") {
      val stringToParse = s"(foo1::bar1,foo2::bar2);(foo3::bar3,foo4::bar4)"
      val result = QueryFilterParser(new FilterString(stringToParse))
      inside (result.value) { case AndExpression(expressions) =>
        expressions must have length (2)
        expressions.zipWithIndex.foreach { case (orExpression, andIndex) =>
          inside (orExpression) { case OrExpression(expressions) =>
            expressions must have length (2)
            expressions.zipWithIndex.foreach { case (comparison, orIndex) =>
              val id = 2 * andIndex + orIndex + 1
              comparison must beComparison(s"foo$id", Comparator.Equal, s"bar$id")
            }
          }
        }
      }
    }

  }
}
