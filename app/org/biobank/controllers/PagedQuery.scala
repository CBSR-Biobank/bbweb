package org.biobank.controllers

import org.biobank.domain._
import org.biobank.infrastructure._
import org.biobank.domain.{ DomainError, DomainValidation }

import scalaz.Scalaz._

case class PagedQuery[T <: ConcurrencySafeEntity[_]](sortFields: Map[String, (T, T) => Boolean],
                                                     page:       Int,
                                                     pageSize:   Int,
                                                     order:      String) {

  def getSortFunc(sortBy: String, default: (T, T) => Boolean): (T, T) => Boolean = {
    sortFields.getOrElse(sortBy, default)
  }

  def getSortFunc(sortBy: String): DomainValidation[(T, T) => Boolean] = {
    sortFields.get(sortBy).toSuccessNel(DomainError(s"invalid sort field: $sortBy"))
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
