package service

import scala.concurrent.stm.Ref

/**
 * A Wrapper around an STM Ref of a Map.
 * To be used by the "Service" class and the "Processor" classes only
 */
class Repository[K, A](keyGetter: (A) => K) {

  private val internalMap: Ref[Map[K, A]] = Ref(Map.empty[K, A])

  def getMap = internalMap.single.get
  def getByKey(key: K): Option[A] = getMap.get(key)
  def getValues: Iterable[A] = getMap.values
  def getKeys: Iterable[K] = getMap.keys

  protected[service] def updateMap(value: A) =
    internalMap.single.transform(map => map + (keyGetter(value) -> value))

}