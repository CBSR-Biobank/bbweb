package infrastructure

import scala.concurrent.stm.Ref

class ValueObjectList[A] {

  protected val internalList: Ref[List[A]] = Ref(List.empty[A])

  def getList = internalList.single.get

  def add(value: A) =
    internalList.single.transform(list => value :: list)

  def remove(value: A) =
    internalList.single.transform(list => list.filterNot(v => v.equals(value)))

}
