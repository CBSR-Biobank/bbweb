package org.biobank.controllers

import play.api.cache._
import net.sf.ehcache._
import scala.concurrent.duration.Duration
import scala.reflect.ClassTag

/**
  * Custom in-memory cache working around the shortcomings of the play bundled one.
  *
  * See more:
  * https://groups.google.com/d/msg/play-framework/PBIfeiwl5rU/-IWifSWhBlAJ
  *
 */
class FixedEhCache extends CacheApi {

  lazy val cache = {
    val manager = CacheManager.getInstance()
    manager.addCacheIfAbsent("play")
    manager.getCache("play")
  }

  override def set(key: String, value: Any, expiration: Duration) {
    val element = new Element(key, value)
    if (expiration.length == 0) element.setEternal(true)
    element.setTimeToLive(expiration.toSeconds.toInt)
    cache.put(element)
  }

  override def get[T](key: String)(implicit arg: ClassTag[T]): Option[T] =  {
    Option(cache.get(key)).map(_.getObjectValue.asInstanceOf[T])
  }

  override def getOrElse[A](key: String, expiration: Duration)(orElse: => A)
                        (implicit arg: ClassTag[A]): A = orElse

  override def remove(key: String) {
    cache.remove(key)
    ()
  }

}
