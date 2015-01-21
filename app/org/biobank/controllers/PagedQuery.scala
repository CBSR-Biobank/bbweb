package org.biobank.controllers

import org.biobank.infrastructure._
import org.biobank.domain.{ DomainError, DomainValidation }

import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._

import scalaz._
import scalaz.Scalaz._

case class PagedQuery(sortField: String, page: Int, pageSize: Int, order: String) {

  def getSortField(validFields: Seq[String]): DomainValidation[String] = {
    if (validFields.contains(sortField)) {
      sortField.successNel
    } else {
      DomainError(s"invalid sort field: $sortField").failureNel
    }
  }

  def getPageSize(maxPageSize: Int): DomainValidation[Int] = {
    if (pageSize <= 0) {
      DomainError(s"page size is invalid: $pageSize").failureNel
    } else if (pageSize > maxPageSize) {
      DomainError(s"page size exceeds maximum: pageSize/$pageSize, max/$maxPageSize").failureNel
    } else {
      pageSize.successNel
    }
  }

  def getPage(maxPageSize: Int, totalItems: Int): DomainValidation[Int] = {
    getPageSize(maxPageSize).fold(
      err => err.failure[Int],
      pageSize => {
        if (page < 1) {
          DomainError(s"page is invalid: $page").failureNel
        } else if (((totalItems > 0) && ((page - 1) * pageSize >= totalItems)) ||
          ((totalItems == 0) && (page > 1))) {
          DomainError(s"page exceeds limit: $page").failureNel
        } else {
          // if totalItems is zero, but page is 1 then it is valid
          page.successNel
        }
      }
    )
  }

  def getSortOrder(): DomainValidation[SortOrder] = {
    SortOrder.fromString(order)
  }

}

/**
  * Defines a Page of elements.
  *
  * @param items items in the page
  * @param page page number. Starts at page 1.
  * @param offset page offset. Starts at 0.
  * @param total total elements
  * @param pageSize max elements in a page
  *
  * Borrowed from:
  *
  * https://github.com/pvillega/Play-Modules/blob/master/app/models/Pagination.scala
  */
case class PagedResults[+T](items: Seq[T], page: Int, pageSize: Int, offset: Long, total: Long) {
  lazy val prev = Option(page - 1).filter(_ > 0)
  lazy val next = Option(page + 1).filter(_ => (offset + items.size) < total)
  lazy val maxPages = (total.toDouble/pageSize).ceil.toInt
}


object PagedResults {

  def create[T](items: Seq[T], page: Int, pageSize: Int): DomainValidation[PagedResults[T]]= {
    if (items.isEmpty) {
      PagedResults.createEmpty(page, pageSize).successNel
    } else {
      val offset = pageSize * (page - 1)
      if ((offset > 0) && (offset >= items.size)) {
        DomainError(s"invalid page requested: ${page}").failureNel
      } else {
        PagedResults(
          items    = items.drop(offset).take(pageSize),
          page     = page,
          pageSize = pageSize,
          offset   = offset,
          total    = items.size
        ).successNel
      }
    }
  }

  def createEmpty[T](page: Int, pageSize: Int) = PagedResults(
    items    = Seq.empty[T],
    page     = page,
    pageSize = pageSize,
    offset   = 0,
    total    = 0
  )

  implicit def pagedResultsWrites[T](implicit fmt: Writes[T]) : Writes[PagedResults[T]] =
    new Writes[PagedResults[T]] {
      def writes(pr: PagedResults[T]) = Json.obj(
        "items"           -> pr.items,
        "page"            -> pr.page,
        "offset"          -> pr.offset,
        "total"           -> pr.total,
        "pageSize"        -> pr.pageSize,
        "prev"            -> pr.prev,
        "next"            -> pr.next,
        "maxPages"        -> pr.maxPages
      )
    }

}
