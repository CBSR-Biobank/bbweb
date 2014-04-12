package domain

import scala.concurrent.stm.Ref

import scalaz._
import scalaz.Scalaz._

/**
 * A Wrapper around an STM Ref of a Map.
 * To be used by the "Service" class
 */
private[domain] class ReadRepository[K, A](keyGetter: (A) => K) {

  protected val internalMap: Ref[Map[K, A]] = Ref(Map.empty[K, A])

  protected def getMap = internalMap.single.get
  protected def getByKey(key: K): DomainValidation[A] = {
    getMap.get(key) match {
      case Some(value) => value.success
      case None => DomainError(s"value with key $key not found").failNel
    }
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
