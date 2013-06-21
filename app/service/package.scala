package service

import domain._
import domain.study._

trait IdentityService[T] {

  def nextStringIdentity: String = java.util.UUID.randomUUID.toString.toUpperCase

}

object SpecimenGroupIdentityService extends IdentityService[SpecimenGroupId] {

  def nextIdentity: SpecimenGroupId = new SpecimenGroupId(nextStringIdentity)

}

object CollectionEventTypeAnnotationTypeIdentityService extends IdentityService[String] {

  def nextIdentity: String = nextStringIdentity

}

object CollectionEventTypeIdentityService extends IdentityService[CollectionEventTypeId] {

  def nextIdentity: CollectionEventTypeId = new CollectionEventTypeId(nextStringIdentity)

}

object SpecimenGroupCollectionEventTypeIdentityService extends IdentityService[String] {

  def nextIdentity: String = nextStringIdentity

}

object AnnotationTypeIdentityService extends IdentityService[AnnotationTypeId] {

  def nextIdentity: AnnotationTypeId = new AnnotationTypeId(nextStringIdentity)

}

object UserIdentityService extends IdentityService[UserId] {

  def nextIdentity: UserId = new UserId(nextStringIdentity)

}
