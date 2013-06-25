package service

import domain._
import domain.study._

trait IdentityService {

  def nextIdentity: String = java.util.UUID.randomUUID.toString.toUpperCase

}

object StudyIdentityService extends IdentityService

object SpecimenGroupIdentityService extends IdentityService

object CollectionEventTypeIdentityService extends IdentityService

object AnnotationTypeIdentityService extends IdentityService

object SpecimenGroupCollectionEventTypeIdentityService extends IdentityService

object CollectionEventAnnotationTypeIdentityService extends IdentityService

object CollectionEventTypeAnnotationTypeIdentityService extends IdentityService

object UserIdentityService extends IdentityService
