package infrastructure

import scala.concurrent.stm.Ref
import scalaz._
import Scalaz._

/**
 * A Wrapper around an STM Ref of a Map.
 * To be used by the "Service" class and the "Processor" classes only
 */
class ReadRepository[K, A](keyGetter: (A) => K) {

  protected val internalMap: Ref[Map[K, A]] = Ref(Map.empty[K, A])

  def getMap = internalMap.single.get
  def getByKey(key: K): DomainValidation[A] = getMap.get(key) match {
    case Some(value) => value.success
    case None => DomainError("value does not exist for key: %s" format (key.toString)).fail
  }
  def getValues: Iterable[A] = getMap.values
  def getKeys: Iterable[K] = getMap.keys

}

class ReadWriteRepository[K, A](keyGetter: (A) => K) extends ReadRepository[K, A](keyGetter) {

  def updateMap(value: A) =
    internalMap.single.transform(map => map + (keyGetter(value) -> value))

  def remove(value: A) =
    internalMap.single.transform(map => map - keyGetter(value))

}