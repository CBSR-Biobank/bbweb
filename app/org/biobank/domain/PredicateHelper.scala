package org.biobank.domain

/**
 * Help taken from here:
 *
 * http://danielwestheide.com/blog/2013/01/23/the-neophytes-guide-to-scala-part-10-staying-dry-with-higher-order-functions.html
 *
 */
trait PredicateHelper {

  def complement[A](predicate: A => Boolean): A => Boolean = (a: A) => !predicate(a)

  def any[A](predicates: (A => Boolean)*): A => Boolean =
    a => predicates.exists(pred => pred(a))

  def none[A](predicates: (A => Boolean)*): A => Boolean = complement(any(predicates: _*))

  @SuppressWarnings(Array("org.wartremover.warts.Any", "org.wartremover.warts.Nothing"))
  def every[A](predicates: (A => Boolean)*): A => Boolean = none(predicates.view.map(complement(_)): _*)

}
