package org.biobank.controllers

import play.api.cache._
import play.api._
import net.sf.ehcache._

/**
  * Custom in-memory cache plugin working around the shortcomings of the play bundled one.
  *
  * See more:
  * https://groups.google.com/d/msg/play-framework/PBIfeiwl5rU/-IWifSWhBlAJ
  *
  */
class FixedEhCachePlugin(app: play.api.Application) extends CachePlugin {

  lazy val cache = {
    val manager = CacheManager.getInstance()
    manager.addCacheIfAbsent("play")
    manager.getCache("play")
  }

  override def onStart() {
    cache
    ()
  }

  override def onStop() {
    cache.flush()
  }

  lazy val api = new CacheAPI {

    def set(key: String, value: Any, expiration: Int) {
      val element = new Element(key, value)
      if (expiration == 0) element.setEternal(true)
      element.setTimeToLive(expiration)
      cache.put(element)
    }

    def get(key: String): Option[Any] = {
      Option(cache.get(key)).map(_.getObjectValue)
    }

    def remove(key: String) {
      cache.remove(key)
      ()
    }
  }

}
