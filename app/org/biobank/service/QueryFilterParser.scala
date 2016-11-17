package org.biobank.service

import scala.util.parsing.combinator.RegexParsers
import scalaz.Scalaz._

/**
 * Parser for filter strings that use RSQL (https://github.com/jirutka/rsql-parser) grammar.
 *
 * Help taken from here:
 *
 * http://coryklein.com/scala/2015/09/17/using-scalas-regexparsers-to-create-a-grammar-for-interpreting-a-rest-data-query.html
 */
@SuppressWarnings(Array("org.wartremover.warts.Enumeration"))
object Comparator extends Enumeration {
  type Comparator = Value

  val LessThan             = Value(":lt:")
  val GreaterThan          = Value(":gt:")
  val LessThanOrEqualTo    = Value(":le:")
  val GreaterThanOrEqualTo = Value(":ge:")
  val In                   = Value(":in:")
  val NotIn                = Value(":out:")
  val NotEqualTo           = Value(":ne:")
  val Equal                = Value("::")
}

import Comparator._

/**
 * Grammar comes from here: https://github.com/jirutka/rsql-parser
 */
object QueryFilterParserGrammar {

  trait Argument
  trait Expression

  final case class Value(name: String) extends Argument {
    override def toString = s"Value: $name"
  }

  final case class Arguments(arguments: List[String]) extends Argument {
    override def toString = "Arguments: '" + arguments.mkString(", ") + "'"
  }

  final case class Selector(name: String) {
    override def toString = name
  }

  final case class Comparison(selector:   String,
                              comparator: Comparator,
                              arguments:  List[String]) extends Expression {
    override def toString =
      s"""Comparison: ($selector $comparator ${arguments.mkString(", ")})"""
  }

  final case class Constraint(expression: Expression) extends Expression {
    override def toString = s"Constraint: ($expression)"
  }

  final case class Group(orExpression: OrExpression) extends Expression {
    override def toString = orExpression.toString
  }

  final case class AndExpression(expressions: List[Expression]) extends Expression {
    override def toString = "And: (" + expressions.mkString("; ") + ")"
  }

  final case class OrExpression(expressions: List[Expression]) extends Expression {
    override def toString = "Or: (" + expressions.mkString(", ") + ")"
  }

}

object QueryFilterParser extends RegexParsers {
  import QueryFilterParserGrammar._

  def comparisonOp = (Comparator.LessThan.toString ^^^ Comparator.LessThan
                        | Comparator.GreaterThan.toString ^^^ Comparator.GreaterThan
                        | Comparator.LessThanOrEqualTo.toString ^^^ Comparator.LessThanOrEqualTo
                        | Comparator.GreaterThanOrEqualTo.toString ^^^ Comparator.GreaterThanOrEqualTo
                        | Comparator.In.toString ^^^ Comparator.In
                        | Comparator.NotIn.toString ^^^ Comparator.NotIn
                        | Comparator.NotEqualTo.toString ^^^ Comparator.NotEqualTo
                        | Comparator.Equal.toString ^^^ Comparator.Equal)

  def singleQuotedValue =
    """'[^\"\(\);,=!~<>]*'""".r ^^ { case v => Value(v.substring(1, v.size - 1))}

  def doubleQuotedValue =
    """\"[^'\(\);,=!~<>]*\"""".r ^^ { case v => Value(v.substring(1, v.size - 1))}

  def unquotedValue =
    """[^\(\);,=!~<>]+""".r ^^ { case v => Value(v) }

  def value: Parser[Value] =
    (unquotedValue | singleQuotedValue | doubleQuotedValue) ^^ { case v => v }

  def argumentList: Parser[Argument] =
    "(" ~> repsep(value, ",") <~ ")" ^^ { case l => Arguments(l.map {_.name}) }

  def arguments: Parser[Argument] =
    (argumentList | value) ^^ { case a => a }

  def selector: Parser[Selector] =
    """[_a-zA-Z]+[_a-zA-Z0-9.]*""".r ^^ { case n => Selector(n) }

  def comparison: Parser[Expression] =
    selector ~ comparisonOp ~ arguments ^^ {
      case s ~ c ~ a =>
        a match {
          case v: Value     => Comparison(s.name, c, List(v.name))
          case a: Arguments => Comparison(s.name, c, a.arguments)
        }
    }

  def group: Parser[Expression] =
    "(" ~> or <~ ")" ^^ { case o => o }

  def constraint: Parser[Constraint] =
    (group | comparison) ^^ { case c => Constraint(c) }

  def and: Parser[Expression] =
    rep1sep(constraint, ";") ^^ { case l =>
      if (l.size == 1) l(0).expression
      else AndExpression(l.map { _.expression })
    }

  def or: Parser[Expression] =
    rep1sep(and, ",") ^^ { case l =>
      if (l.size == 1) l(0)
      else OrExpression(l)
    }

  def apply(filter: FilterString) = parseAll(or, filter.expression) match {
      case Success(result, _) => Some(result)
      case NoSuccess(_, _) => None
    }

  def expressions(filter: FilterString):ServiceValidation[Option[Expression]] = {
    if (filter.expression.trim.isEmpty) {
        None.successNel[String]
    } else {
      val parseResult = QueryFilterParser(filter)
      if (parseResult.isEmpty) {
        s"could not parse filter expression: $filter".failureNel[Option[Expression]]
      } else {
        parseResult.successNel[String]
      }
    }
  }
}
