package service

import domain._
import domain.study._

object SpecimenGroupIdentityService {

  def nextIdentity: SpecimenGroupId =
    new SpecimenGroupId(java.util.UUID.randomUUID.toString.toUpperCase)

}

object CollectionEventTypeAnnotationTypeIdentityService {

  def nextIdentity: String =
    java.util.UUID.randomUUID.toString.toUpperCase

}

object CollectionEventTypeIdentityService {

  def nextIdentity: CollectionEventTypeId =
    new CollectionEventTypeId(java.util.UUID.randomUUID.toString.toUpperCase)

}

object SpecimenGroupCollectionEventTypeIdentityService {

  def nextIdentity: String =
    java.util.UUID.randomUUID.toString.toUpperCase

}

object AnnotationTypeIdentityService {

  def nextIdentity: AnnotationTypeId =
    new AnnotationTypeId(java.util.UUID.randomUUID.toString.toUpperCase)

}

object UserIdentityService {

  def nextIdentity: UserId =
    new UserId(java.util.UUID.randomUUID.toString.toUpperCase)

}
