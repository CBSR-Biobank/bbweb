package org.biobank.domain

import scala.concurrent.stm.Ref

import scalaz._
import scalaz.Scalaz._

/**
  * A read-only repository.
  */
trait  ReadRepository[K, A] {

  def getByKey(key: K): DomainValidation[A]

  def getValues: Iterable[A]

  def getKeys: Iterable[K]

}

/** A read/write repository.
  */
trait ReadWriteRepository[K, A] extends ReadRepository[K, A] {

  def put(value: A): A

  def remove(value: A): A

}

/**
  * A read-only wrapper around an STM Ref of a Map.
  */
class ReadRepositoryRefImpl[K, A](keyGetter: (A) => K) extends ReadRepository[K, A] {

  protected val internalMap: Ref[Map[K, A]] = Ref(Map.empty[K, A])

  protected def getMap = internalMap.single.get

  def getByKey(key: K): DomainValidation[A] = {
    getMap.get(key) match {
      case Some(value) => value.success
      case None => DomainError(s"value with key $key not found").failNel
    }
  }

  def getValues: Iterable[A] = getMap.values

  def getKeys: Iterable[K] = getMap.keys

}

/** A read/write wrapper around an STM Ref of a map.
  *
  * Used by processor actors.
  */
private [domain] class ReadWriteRepositoryRefImpl[K, A](keyGetter: (A) => K)
    extends ReadRepositoryRefImpl[K, A](keyGetter)
    with ReadWriteRepository[K, A] {

  def put(value: A): A = {
    internalMap.single.transform(map => map + (keyGetter(value) -> value))
    value
  }

  def remove(value: A): A = {
    internalMap.single.transform(map => map - keyGetter(value))
    value
  }

}
