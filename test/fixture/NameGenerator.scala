package fixture

import scala.reflect.ClassTag
import scala.reflect._
import scala.collection.mutable.HashMap

/**
 * Stateful class to create unique names.
 *
 * When the same name is asked for the second time, a unique int is attached to it
 *
 * Code borrowed from the class UniqueNames in the Specs2 project.
 */
class NameGenerator(rootName: String) {

  val delimiter: String = "_"

  // if a name is found more than once we need to append a new id
  var names = new HashMap[String, Int].withDefaultValue(0)

  // if a reference is found more than once we need to append a new id
  var references = new HashMap[Int, Int].withDefaultValue(0)

  /**
   * @return a unique name based on the name unicity
   */
  def uniqueName(name: String) = {
    val result = if (names.contains(name)) {
      name + delimiter + names(name)
    } else name
    names(name) += 1
    result
  }

  /**
   * @return a unique name based on the reference hashcode
   */
  def uniqueName(reference: Any, name: String) = {
    val code = reference.hashCode
    val result = if (references.contains(code)) {
      name + delimiter + references(code)
    } else name
    references(code) += 1
    result
  }

  def next[T: ClassTag]: String =
    uniqueName(s"$rootName-${classTag[T].getClass.getSimpleName}")

}