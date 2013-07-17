package domain

import scala.concurrent.stm.Ref
import scala.reflect.ClassTag
import org.slf4j.LoggerFactory

import scalaz._
import Scalaz._

/**
 * A Wrapper around an STM Ref of a Map.
 * To be used by the "Service" class and the "Processor" classes only
 */
private[domain] class ReadRepository[K, A](keyGetter: (A) => K) {

  protected val internalMap: Ref[Map[K, A]] = Ref(Map.empty[K, A])

  protected def getMap = internalMap.single.get
  protected def getByKey(key: K): DomainValidation[A] = getMap.get(key) match {
    case Some(value) => value.success
    case None => DomainError("value does not exist for key: %s" format (key.toString)).fail
  }
  protected def getValues: Iterable[A] = getMap.values
  protected def getKeys: Iterable[K] = getMap.keys

}

class ReadWriteRepository[K, A](keyGetter: (A) => K) extends ReadRepository[K, A](keyGetter) {

  protected def updateMap(value: A) = {
    internalMap.single.transform(map => map + (keyGetter(value) -> value))
    value
  }

  protected def removeFromMap(value: A) = {
    internalMap.single.transform(map => map - keyGetter(value))
    value
  }

}
