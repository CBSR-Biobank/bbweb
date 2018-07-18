package org.biobank.utils

import javax.inject.Inject

import play.api.http.HttpFilters
import play.filters.csrf.CSRFFilter
import play.filters.gzip.GzipFilter
import play.filters.headers.SecurityHeadersFilter

class Filters @Inject() (csrfFilter: CSRFFilter,
                         securityHeadersFilter: SecurityHeadersFilter,
                         gzipFilter: GzipFilter) extends HttpFilters {
  def filters = Seq(csrfFilter, securityHeadersFilter, gzipFilter)
}
