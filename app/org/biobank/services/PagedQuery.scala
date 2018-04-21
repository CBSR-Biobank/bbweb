package org.biobank.services

import scalaz.Scalaz._

final case class PagedQuery(filter: FilterString,
                            sort:   SortString,
                            page:   Int,
                            limit:  Int) {

  def validPage(totalItems: Int): ServiceValidation[Int] = {
    if (((totalItems > 0) && ((page - 1) * limit >= totalItems)) ||
          ((totalItems == 0) && (page > 1))) {
      ServiceError(s"page exceeds limit: $page").failureNel[Int]
    } else {
      // if totalItems is zero, but page is 1 then it is valid
      page.successNel
    }
  }
}
