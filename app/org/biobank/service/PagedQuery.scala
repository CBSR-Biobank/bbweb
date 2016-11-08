package org.biobank.service

import org.biobank.infrastructure._
import scalaz.Scalaz._

final case class PagedQuery(page: Int, limit: Int, order: String) {

  def getPageSize(maxPageSize: Int): ServiceValidation[Int] = {
    if (limit <= 0) {
      ServiceError(s"page size is invalid: $limit").failureNel[Int]
    } else if (limit > maxPageSize) {
      ServiceError(s"page size exceeds maximum: limit/$limit, max/$maxPageSize").failureNel[Int]
    } else {
      limit.successNel
    }
  }

  def getPage(maxPageSize: Int, totalItems: Int): ServiceValidation[Int] = {
    getPageSize(maxPageSize).fold(
      err => err.failure[Int],
      limit => {
        if (page < 1) {
          ServiceError(s"page is invalid: $page").failureNel[Int]
        } else if (((totalItems > 0) && ((page - 1) * limit >= totalItems)) ||
          ((totalItems == 0) && (page > 1))) {
          ServiceError(s"page exceeds limit: $page").failureNel[Int]
        } else {
          // if totalItems is zero, but page is 1 then it is valid
          page.successNel
        }
      }
    )
  }

  def getSortOrder(): ServiceValidation[SortOrder] = {
    SortOrder.fromString(order)
  }

}
