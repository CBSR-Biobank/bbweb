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

  bind [PasswordHasher] toNonLazy new PasswordHasherImpl
  bind [AuthToken] toNonLazy new AuthTokenImpl

  bind [CollectionEventAnnotationTypeRepository]  toNonLazy new CollectionEventAnnotationTypeRepositoryImpl
  bind [CollectionEventTypeRepository]            toNonLazy new CollectionEventTypeRepositoryImpl
  bind [ParticipantAnnotationTypeRepository]      toNonLazy new ParticipantAnnotationTypeRepositoryImpl
  bind [ProcessingTypeRepository]                 toNonLazy new ProcessingTypeRepositoryImpl
  bind [SpecimenGroupRepository]                  toNonLazy new SpecimenGroupRepositoryImpl
  bind [SpecimenLinkAnnotationTypeRepository]     toNonLazy new SpecimenLinkAnnotationTypeRepositoryImpl
  bind [SpecimenLinkTypeRepository]               toNonLazy new SpecimenLinkTypeRepositoryImpl
  bind [StudyRepository]                          toNonLazy new StudyRepositoryImpl
  bind [ParticipantRepository]                    toNonLazy new ParticipantRepositoryImpl

  bind [UserRepository]            toNonLazy new UserRepositoryImpl
  bind [CentreRepository]          toNonLazy new CentreRepositoryImpl
  bind [CentreLocationsRepository] toNonLazy new CentreLocationsRepositoryImpl
  bind [CentreStudiesRepository]   toNonLazy new CentreStudiesRepositoryImpl
  bind [LocationRepository]        toNonLazy new LocationRepositoryImpl

  bind [StudiesService]      toNonLazy new StudiesServiceImpl
  bind [ParticipantsService] toNonLazy new ParticipantsServiceImpl
  bind [UsersService]        toNonLazy new UsersService
  bind [CentresService]      toNonLazy new CentresServiceImpl

  binding toProvider new CeventAnnotationTypeProcessor
  binding toProvider new CollectionEventTypeProcessor
  binding toProvider new ParticipantAnnotationTypeProcessor
  binding toProvider new ProcessingTypeProcessor
  binding toProvider new SpecimenGroupProcessor
  binding toProvider new SpecimenLinkAnnotationTypeProcessor
  binding toProvider new SpecimenLinkTypeProcessor
  binding toProvider new StudiesProcessor

  binding toProvider new ParticipantsProcessor

  binding toProvider new UsersProcessor

  binding toProvider new CentresProcessor

}
