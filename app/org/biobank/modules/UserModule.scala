package org.biobank.modules

import org.biobank.domain._
import org.biobank.domain.centre._
import org.biobank.domain.study._
import org.biobank.domain.user._
import org.biobank.service._
import org.biobank.service.centre._
import org.biobank.service.study._
import org.biobank.service.users._

import scaldi.Module

class UserModule extends Module {

  bind [PasswordHasher] to new PasswordHasherImpl

  bind [CollectionEventAnnotationTypeRepository]  to new CollectionEventAnnotationTypeRepositoryImpl
  bind [CollectionEventTypeRepository]            to new CollectionEventTypeRepositoryImpl
  bind [ParticipantAnnotationTypeRepository]      to new ParticipantAnnotationTypeRepositoryImpl
  bind [ProcessingTypeRepository]                 to new ProcessingTypeRepositoryImpl
  bind [SpecimenGroupRepository]                  to new SpecimenGroupRepositoryImpl
  bind [SpecimenLinkAnnotationTypeRepository]     to new SpecimenLinkAnnotationTypeRepositoryImpl
  bind [SpecimenLinkTypeRepository]               to new SpecimenLinkTypeRepositoryImpl
  bind [StudyRepository]                          to new StudyRepositoryImpl

  bind [UserRepository] to new UserRepositoryImpl

  bind [CentreRepository] to new CentreRepositoryImpl
  bind [CentreLocationsRepository] to new CentreLocationsRepositoryImpl
  bind [CentreStudiesRepository] to new CentreStudiesRepositoryImpl
  bind [LocationRepository] to new LocationRepositoryImpl

  bind [StudiesService] to new StudiesServiceImpl

  bind [UsersService] to new UsersService

  bind [CentresService] to new CentresServiceImpl

  binding toProvider new CeventAnnotationTypeProcessor
  binding toProvider new CollectionEventTypeProcessor
  binding toProvider new ParticipantAnnotationTypeProcessor
  binding toProvider new ProcessingTypeProcessor
  binding toProvider new SpecimenGroupProcessor
  binding toProvider new SpecimenLinkAnnotationTypeProcessor
  binding toProvider new SpecimenLinkTypeProcessor
  binding toProvider new StudiesProcessor

  binding toProvider new UsersProcessor

  binding toProvider new CentresProcessor

}
