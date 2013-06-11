package fixture

import scala.concurrent.stm.Ref
import scala.collection.mutable.ConcurrentMap
import scala.reflect.ClassTag
import java.util.concurrent.atomic.AtomicInteger

import scala.reflect._

trait NameGeneratorBase {
  def root: String
}

class NameGenerator(aRoot: String) extends {
  val root = NameGenerator.formatRoot(aRoot)
} with NameGeneratorBase {
  private val Delimiter = "_"

  val suffixesRef = Ref(Map.empty[ClassTag[_], Int])

  def next[T: ClassTag] = {
    val tag = classTag[T]
    suffixesRef.single.get.keys.find(_.equals(tag)) match {
      case Some(k) => {
        val count = suffixesRef.single.get(k)
        suffixesRef.single.transform(suff => suff + (k -> (count + 1)))
      }
      case None => suffixesRef.single.transform(suff => suff + (tag -> 1))
    }

    root + Delimiter + suffixesRef.single.get(tag)
  }

  override def toString() = root

}

object NameGenerator {
  private val TruncateLength = 25
  private val TruncateDelimiter = "..."
  private val TruncateDelimiterLength = TruncateDelimiter.length

  def formatRoot(root: String) = {
    if (root.isEmpty) throw new Error("root is empty")
    else if (root.length < TruncateLength - TruncateDelimiterLength) root
    else root.slice(0, TruncateLength - TruncateDelimiterLength) + TruncateDelimiter
  }
}