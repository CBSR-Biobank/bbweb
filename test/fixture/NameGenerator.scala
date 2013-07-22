package fixture

import scala.concurrent.stm.Ref
import scala.collection.mutable.ConcurrentMap
import scala.reflect.ClassTag
import java.util.concurrent.atomic.AtomicInteger
import org.specs2.data.UniqueNames

import scala.reflect._

class NameGenerator(rootName: String) {
  val namer = UniqueNames(rootName)

  def next[T: ClassTag]: String =
    namer.uniqueName("%s-%s" format (rootName, classTag[T].getClass.getSimpleName))

}