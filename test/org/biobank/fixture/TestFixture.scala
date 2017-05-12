package org.biobank.fixture

import org.biobank.controllers.FixedEhCache
import org.biobank.domain.Factory
import org.scalatest._
import play.api.cache.CacheApi
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder

trait TestFixture extends FunSpec with BeforeAndAfterEach {

  val app = new GuiceApplicationBuilder()
    .overrides(bind[CacheApi].to[FixedEhCache])
    .build

  val factory = new Factory

}
