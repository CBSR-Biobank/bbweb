package org.biobank.domain

import com.github.ghik.silencer.silent
import org.biobank.CommonValidations.EntityCriteriaNotFound
import scala.concurrent.stm.Ref
import scalaz.Scalaz._

/**
  * A read-only repository.
  */
trait ReadRepository[K, A] {

  def isEmpty: Boolean

  def getByKey(key: K): DomainValidation[A]

  def getValues: Iterable[A]

  def getKeys: Iterable[K]

}

/** A read/write repository.
  */
trait ReadWriteRepository[K, A] extends ReadRepository[K, A] {

  def nextIdentity(): K

  protected def nextIdentityAsString: String

  def init(): Unit

  def put(value: A): Unit

  def remove(value: A): Unit

  def removeAll(): Unit

}

trait ReadWriteRepositoryWithSlug[K, A] extends ReadWriteRepository[K, A] {

  /** if slug is already used, then create a new one with a count appended to it. */
  def uniqueSlug(slug: Slug): Slug

  def getBySlug(slug: Slug): DomainValidation[A]

  /** if slug is already used, then create a new one with a count appended to it. */
  def uniqueSlugFromStr(name: String): Slug

  protected def slugNotFound(slug: Slug): EntityCriteriaNotFound
}

/**
  * A read-only wrapper around an STM Ref of a Map.
  */
@silent abstract class ReadRepositoryRefImpl[K, A](keyGetter: (A) => K) extends ReadRepository[K, A] {
  import org.biobank.CommonValidations._

  protected val internalMap: Ref[Map[K, A]] = Ref(Map.empty[K, A])

  protected def getMap = internalMap.single.get

  def isEmpty: Boolean = getMap.isEmpty

  protected def notFound(id: K): IdNotFound

  def getByKey(key: K): DomainValidation[A] = {
    internalMap.single.get.get(key).toSuccessNel(notFound(key).toString)
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

  def init(): Unit = {
    removeAll
  }

  protected def nextIdentityAsString: String =
    // ensure all IDs can be used in URLs
    Slug.slugify(
      play.api.libs.Codecs.sha1(
        ReadWriteRepositoryRefImpl.md.digest(
          java.util.UUID.randomUUID.toString.getBytes)))

  def put(value: A): Unit = {
    internalMap.single.transform(map => map + (keyGetter(value) -> value))
  }

  def remove(value: A): Unit = {
    internalMap.single.transform(map => map - keyGetter(value))
  }

  def removeAll(): Unit = {
    internalMap.single.transform(map => map.empty)
  }

}

private [domain] abstract
class ReadWriteRepositoryRefImplWithSlug
  [K, A <: ConcurrencySafeEntity[K] with HasSlug](keyGetter: (A) => K)
    extends ReadWriteRepositoryRefImpl[K, A](keyGetter) {

  protected def slugNotFound(slug: Slug): EntityCriteriaNotFound

  def uniqueSlug(origSlug: Slug): Slug = {
    val slugRegex = s"^${origSlug}(-[0-9]+)?$$".r
    val count = internalMap.single.get.values
      .filter { v =>
        slugRegex.findFirstIn(v.slug.id) != None
      }.size
    if (count <= 0) origSlug
    else Slug(s"${origSlug.id}-$count")
  }

  def uniqueSlugFromStr(strSlug: String): Slug = {
    uniqueSlug(Slug(strSlug))
  }

  def getBySlug(slug: Slug): DomainValidation[A] = {
    internalMap.single.get.find(_._2.slug == slug).map(_._2).toSuccessNel(slugNotFound(slug).toString)
  }

}

private object ReadWriteRepositoryRefImpl {

  val md = java.security.MessageDigest.getInstance("SHA-1")

}
