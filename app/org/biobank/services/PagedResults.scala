package org.biobank.services

import play.api.libs.json._
import org.slf4j.{Logger, LoggerFactory}
import scalaz.Scalaz._

/**
  * Defines a Page of items.
  *
  * @param items items in the page
  * @param page page number. Starts at page 1.
  * @param offset page offset. Starts at 0.
  * @param total total elements
  * @param limit max elements in a page
  *
  * Borrowed from:
  *
  * https://github.com/pvillega/Play-Modules/blob/master/app/models/Pagination.scala
  */
final case class PagedResults[+T](items: Seq[T], page: Int, limit: Int, offset: Long, total: Long) {
  lazy val prev: Option[Int] = Option(page - 1).filter(_ > 0)
  lazy val next: Option[Int] = Option(page + 1).filter(_ => (offset + items.size) < total)
  lazy val maxPages: Int = (total.toDouble/limit).ceil.toInt
}

object PagedResults {

  val log: Logger = LoggerFactory.getLogger(this.getClass)

  def create[T](items: Seq[T], page: Int, limit: Int): ServiceValidation[PagedResults[T]]= {
    if (items.isEmpty) {
      PagedResults.createEmpty[T](page, limit).successNel[String]
    } else {
      val offset = limit * (page - 1)
      if ((offset > 0) && (offset >= items.size)) {
        ServiceError(s"invalid page requested: ${page}").failureNel[PagedResults[T]]
      } else {
        log.debug(s"PagedResults.create: page:$page, limit: $limit, offset: $offset")
        PagedResults(items    = items.drop(offset).take(limit),
                     page     = page,
                     limit = limit,
                     offset   = offset.toLong,
                     total    = items.size.toLong).successNel[String]
      }
    }
  }

  def createEmpty[T](page: Int, limit: Int): PagedResults[T] = PagedResults(
    items    = Seq.empty[T],
    page     = page,
    limit = limit,
    offset   = 0,
    total    = 0
  )

  implicit def pagedResultsWrites[T](implicit fmt: Writes[T]) : Writes[PagedResults[T]] =
    new Writes[PagedResults[T]] {
      def writes(pr: PagedResults[T]): JsValue = Json.obj(
        "items"    -> pr.items,
        "page"     -> pr.page,
        "offset"   -> pr.offset,
        "total"    -> pr.total,
        "limit"    -> pr.limit,
        "prev"     -> pr.prev,
        "next"     -> pr.next,
        "maxPages" -> pr.maxPages
      )
    }

}
