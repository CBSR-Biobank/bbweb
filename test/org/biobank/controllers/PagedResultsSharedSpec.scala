package org.biobank.controllers

import org.biobank.fixture._
import play.api.test.Helpers._

/**
 * Common code for REST APIs that uses paged results.
 */
trait PagedResultsSharedSpec { this: ControllerFixture =>

  def pagedQueryShouldFailSharedBehaviour(uri: String) = {

    it("fail with a negative page number") {
      val resp = makeAuthRequest(GET, uri + "?page=-1&limit=1")
      resp.value must beBadRequestWithMessage("page is invalid")
    }

    it("fail with a invalid page number") {
      val resp = makeAuthRequest(GET, uri + "?page=100000000")
      resp.value must beBadRequestWithMessage("page exceeds limit")
    }

    it("fail with a negative page size") {
      val resp = makeAuthRequest(GET, uri + "?limit=-1")
      resp.value must beBadRequestWithMessage("page size is invalid")
    }

    it("fail with an invalid page size") {
      val resp = makeAuthRequest(GET, uri + "?limit=1000000")
      resp.value must beBadRequestWithMessage("page size exceeds maximum")
    }

    it("fail with an invalid sort field") {
      val resp = makeAuthRequest(GET, uri + "?sort=xyz")
      resp.value must beBadRequestWithMessage("invalid sort field")
    }
  }

}
