package org.biobank.controllers

import java.net.URLDecoder
import java.nio.charset.{StandardCharsets => SC}
import org.biobank.infrastructure._
import org.biobank.services.{FilterString, SortString}
//import org.slf4j.{Logger, LoggerFactory}
import scalaz.Scalaz._
import scalaz.Validation.FlatMap._

final case class PagedQuery(filter: FilterString,
                            sort:   SortString,
                            page:   Int,
                            limit:  Int) {

  def validPage(totalItems: Int): ControllerValidation[Int] = {
    if (((totalItems > 0) && ((page - 1) * limit >= totalItems)) ||
          ((totalItems == 0) && (page > 1))) {
      ControllerError(s"page exceeds limit: $page").failureNel[Int]
    } else {
      // if totalItems is zero, but page is 1 then it is valid
      page.successNel
    }
  }
}

object PagedQuery {

  //val log: Logger = LoggerFactory.getLogger(this.getClass)

  def create(rawQueryString: String, maxPageSize: Int): ControllerValidation[PagedQuery] = {
    for {
      qsExpressions <- {
        val path = URLDecoder.decode(rawQueryString, SC.US_ASCII.name)
        QueryStringParser(path).toSuccessNel(s"could not parse query string: $rawQueryString")
      }
      filterAndSort <- FilterAndSortQuery.createFromExpressions(qsExpressions).successNel[String]
      page <- {
        Util.toInt(qsExpressions.get("page").getOrElse("1")).
          toSuccessNel(s"page is not a number: $rawQueryString")
      }
      validPage <- {
        if (page > 0) page.successNel[String]
        else ControllerError(s"page is invalid: $page").failureNel[Int]
      }
      limit <- {
        Util.toInt(qsExpressions.get("limit").getOrElse("5")).
          toSuccessNel(s"limit is not a number: $rawQueryString")
      }
      validLimit <- validLimit(limit, maxPageSize)
    } yield {
      PagedQuery(filter = filterAndSort.filter,
                 sort   = filterAndSort.sort,
                 page   = page,
                 limit  = limit)
    }
  }

  private def validLimit(limit: Int, maxPageSize: Int): ControllerValidation[Int] = {
    if (limit <= 0) {
      ControllerError(s"page size is invalid: $limit").failureNel[Int]
    } else if (limit > maxPageSize) {
      ControllerError(s"page size exceeds maximum: limit/$limit, max/$maxPageSize").failureNel[Int]
    } else {
      limit.successNel
    }
  }

}
