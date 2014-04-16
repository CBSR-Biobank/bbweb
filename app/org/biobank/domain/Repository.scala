package org.biobank.domain

import scala.concurrent.stm.Ref

import scalaz._
import scalaz.Scalaz._

/**
  * A read-only wrapper around an STM Ref of a Map.
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

/** A read/write wrapper around an STM Ref of a map.
  *
  * Used by processor actors.
  */
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
