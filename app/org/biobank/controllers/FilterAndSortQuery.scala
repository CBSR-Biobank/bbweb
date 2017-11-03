package org.biobank.controllers

import org.biobank.service.{FilterString, SortString}
import scalaz.Scalaz._

final case class FilterAndSortQuery(filter: FilterString, sort: SortString)

object FilterAndSortQuery {

  def create(rawQueryString: String): ControllerValidation[FilterAndSortQuery] = {
    QueryStringParser(rawQueryString)
      .toSuccessNel(s"could not parse query string: $rawQueryString")
      .map(createFromExpressions)
  }

  def createFromExpressions(expressions: QueryStringExpressions): FilterAndSortQuery = {
    FilterAndSortQuery(filter = new FilterString(expressions.get("filter").getOrElse("")),
                       sort   = new SortString(expressions.get("sort").getOrElse("")))
  }
}
