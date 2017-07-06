package org.biobank.controllers

import akka.Done
import javax.inject.{ Inject, Singleton }
import play.api.cache._
import net.sf.ehcache.{ CacheManager, Element }
import scala.concurrent.duration.Duration
import scala.concurrent.{ ExecutionContext, Future }
import scala.reflect.ClassTag

/**
  * Custom in-memory cache working around the shortcomings of the play bundled one.
  *
  * See more:
  * https://groups.google.com/d/msg/play-framework/PBIfeiwl5rU/-IWifSWhBlAJ
  *
 */
@SuppressWarnings(Array("org.wartremover.warts.ImplicitParameter"))
@Singleton
class CacheForTesting @Inject() (implicit context: ExecutionContext)
    extends AsyncCacheApi {

  @SuppressWarnings(Array("org.wartremover.warts.NonUnitStatements"))
  lazy val cache = {
    val manager = CacheManager.getInstance()
    manager.addCacheIfAbsent("play")
    manager.getCache("play")
  }

  override def set(key: String, value: Any, expiration: Duration): Future[Done] = {
    val element = new Element(key, value)
    if (expiration.length == 0) element.setEternal(true)
    element.setTimeToLive(expiration.toSeconds.toInt)
    Future.successful {
      cache.put(element)
      Done
    }
  }

  @SuppressWarnings(Array("org.wartremover.warts.AsInstanceOf"))
  override def get[T](key: String)(implicit ct: ClassTag[T]): Future[Option[T]] =  {
    Future.successful { Option(cache.get(key)).map(_.getObjectValue.asInstanceOf[T]) }
  }

  override def getOrElseUpdate[A: ClassTag](key: String, expiration: Duration)
                              (orElse: => Future[A]): Future[A] = {
    get[A](key).flatMap {
      case Some(value) => Future.successful(value)
      case None => orElse.flatMap(value => set(key, value, expiration).map(_ => value))
    }
  }

  @SuppressWarnings(Array("org.wartremover.warts.NonUnitStatements"))
  override def remove(key: String): Future[Done] =
    Future.successful {
      cache.remove(key)
      Done
    }

  def removeAll(): Future[Done] = Future {
    cache.removeAll()
    Done
  }

}
