package org.biobank.controllers

import org.biobank.services._
import scalaz.Scalaz._

object FilterAndSortQueryHelper {

  def apply(rawQueryString: String): ControllerValidation[FilterAndSortQuery] = {
    QueryStringParser(rawQueryString)
      .toSuccessNel(s"could not parse query string: $rawQueryString")
      .map(createFromExpressions)
  }

  def createFromExpressions(expressions: QueryStringExpressions): FilterAndSortQuery = {
    FilterAndSortQuery(filter = new FilterString(expressions.get("filter").getOrElse("")),
                       sort   = new SortString(expressions.get("sort").getOrElse("")))
  }
}
