package org.biobank.fixture

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
class NameGenerator(klass: Class[_]) {

  private val delimiter: String = "_"

  private val rootSimpleName = klass.getSimpleName + delimiter + IdGenerator.next

  private val names = new HashMap[String, Int].withDefaultValue(0)

  // if a reference is found more than once we need to append a new id
  private val references = new HashMap[Int, Int].withDefaultValue(0)

  def name() = rootSimpleName

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
      s"$name$delimiter${references(code)}"
    } else {
      name
    }
    references(code) += 1
    result
  }

  def next[T: ClassTag]: String = {
    val className = implicitly[ClassTag[T]].runtimeClass.getSimpleName
    uniqueName(s"$rootSimpleName-$className")
  }

  def nextEmail[T: ClassTag]: String = {
    val className = implicitly[ClassTag[T]].runtimeClass.getSimpleName
    uniqueName(s"$rootSimpleName-$className") + "@test.com"
  }

  def nextUrl[T: ClassTag]: String = {
    val className = implicitly[ClassTag[T]].runtimeClass.getSimpleName
    "http://" + uniqueName(s"$rootSimpleName$className").replace("_", "").toLowerCase + ".com/"
  }

}
