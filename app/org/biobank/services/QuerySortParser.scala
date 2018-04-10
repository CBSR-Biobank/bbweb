package org.biobank.service

import org.biobank.infrastructure.{AscendingOrder, DescendingOrder, SortOrder}
import scala.util.parsing.combinator.RegexParsers

/**
 * Parser for sort strings as described in REST API Resources (http://www.restapitutorial.com/resources.html).
 *
 * Help taken from here:
 *
 * http://coryklein.com/scala/2015/09/17/using-scalas-regexparsers-to-create-a-grammar-for-interpreting-a-rest-data-query.html
 */

object QuerySortParserGrammar {

  final case class Selector(name: String) {
    override def toString: String = name
  }

  final case class SortExpression(name: String, order: SortOrder) {
    override def toString: String = s"$name, $order"
  }

}

object QuerySortParser extends RegexParsers {
  import QuerySortParserGrammar._

  def selector: Parser[Selector] =
    """[_a-zA-Z]+[_a-zA-Z0-9.]*""".r ^^ { case n => Selector(n) }

  def sortExpression: Parser[SortExpression] =
    ("-"?) ~ selector ^^ {
      case Some(o) ~ e => SortExpression(e.name, DescendingOrder)
      case None ~ e    => SortExpression(e.name, AscendingOrder)
    }

  def sortExpressions: Parser[List[SortExpression]] =
    rep1sep(sortExpression, "|") ^^ { case e => e }

  def apply(sort: org.biobank.service.SortString): Option[List[SortExpression]] = {
    if (sort.expression.trim.isEmpty) {
      Some(List[SortExpression]())
    } else {
      parseAll(sortExpressions, sort.expression) match {
        case Success(result, _) => Some(result)
        case NoSuccess(_, _) => None
      }
    }
  }
}
