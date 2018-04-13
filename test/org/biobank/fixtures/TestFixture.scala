package org.biobank.fixture

import org.biobank.controllers.CacheForTesting
import org.biobank.domain.Factory
import org.scalatest._
import play.api.cache.{AsyncCacheApi, DefaultSyncCacheApi, SyncCacheApi}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder

trait TestFixture extends FunSpec with BeforeAndAfterEach {

  val app = new GuiceApplicationBuilder()
    .overrides(bind[SyncCacheApi].to[DefaultSyncCacheApi])
    .overrides(bind[AsyncCacheApi].to[CacheForTesting])
    .build

  val factory = new Factory

}
