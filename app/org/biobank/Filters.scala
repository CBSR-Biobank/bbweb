package org.biobank

import javax.inject.Inject

import play.api.http.HttpFilters
import play.filters.gzip.GzipFilter

class Filters @Inject() (gzipFilter: GzipFilter) extends HttpFilters {
  @SuppressWarnings(Array("org.wartremover.warts.PublicInference"))
  def filters = Seq(gzipFilter)
}
