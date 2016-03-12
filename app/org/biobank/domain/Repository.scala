package org.biobank.domain

import scala.concurrent.stm.Ref
import scalaz.Scalaz._

/**
  * A read-only repository.
  */
trait  ReadRepository[K, A] {

  def isEmpty: Boolean

  def getByKey(key: K): DomainValidation[A]

  def getValues: Iterable[A]

  def getKeys: Iterable[K]

}

/** A read/write repository.
  */
trait ReadWriteRepository[K, A] extends ReadRepository[K, A] {

  protected def nextIdentityAsString: String

  def nextIdentity: K

  def put(value: A): A

  def remove(value: A): A

  def removeAll()

}

/**
  * A read-only wrapper around an STM Ref of a Map.
  */
abstract class ReadRepositoryRefImpl[K, A](keyGetter: (A) => K) extends ReadRepository[K, A] {
  import org.biobank.CommonValidations._

  protected val internalMap: Ref[Map[K, A]] = Ref(Map.empty[K, A])

  protected def getMap = internalMap.single.get

  def isEmpty: Boolean = getMap.isEmpty

  def getByKey(key: K): DomainValidation[A] = {
    getMap.get(key).toSuccessNel(IdNotFound(s"$key").toString)
  }

  def getValues: Iterable[A] = getMap.values

  def getKeys: Iterable[K] = getMap.keys

}

/** A read/write wrapper around an STM Ref of a map.
  *
  * Used by processor actors.
  */
private [domain] abstract class ReadWriteRepositoryRefImpl[K, A](keyGetter: (A) => K)
    extends ReadRepositoryRefImpl[K, A](keyGetter)
    with ReadWriteRepository[K, A] {

  protected def nextIdentityAsString: String = java.util.UUID.randomUUID.toString.replaceAll("-","").toUpperCase

  def put(value: A): A = {
    internalMap.single.transform(map => map + (keyGetter(value) -> value))
    value
  }

  def remove(value: A): A = {
    internalMap.single.transform(map => map - keyGetter(value))
    value
  }

  def removeAll() = {
    internalMap.single.transform(map => map.empty)
  }

}
