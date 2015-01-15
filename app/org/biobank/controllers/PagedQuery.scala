package org.biobank.controllers

import org.biobank.infrastructure._
import org.biobank.domain.{ DomainError, DomainValidation }

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

  def getPageSize(): DomainValidation[Int] = {
    if (pageSize < 0) {
      DomainError(s"page size is invalid: $pageSize").failureNel
    } else {
      pageSize.successNel
    }
  }

  def getPage(totalItems: Int): DomainValidation[Int] = {
    getPageSize.fold(
      err => err.failure[Int],
      pageSize => {
        if (page < 1) {
          DomainError(s"page is invalid: $page").failureNel
        } else if ((page - 1) * pageSize > totalItems) {
          DomainError(s"page exeeds limits: $page").failureNel
        } else {
          page.successNel[DomainError]
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
  * @param page page number
  * @param offset page offset
  * @param total total elements
  * @param pageSize max elements in a page
  *
  * Borrowed from:
  *
  * https://github.com/pvillega/Play-Modules/blob/master/app/models/Pagination.scala
  */
case class PagedReults[+T](items: Seq[T], page: Int, offset: Long, total: Long, pageSize: Int) {
  lazy val prev = Option(page - 1).filter(_ >= 0)
  lazy val next = Option(page + 1).filter(_ => (offset + items.size) < total)
  lazy val maxPages = (total.toDouble/pageSize).ceil.toInt
  lazy val paginationStart = (page - 2).max(1)
  lazy val paginationEnd = (page + 3).min(maxPages)
}

