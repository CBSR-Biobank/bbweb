package org.biobank.modules

import org.biobank.service.centres._
import org.biobank.service.study._
import org.biobank.service.users._

import com.google.inject.AbstractModule
import play.api.libs.concurrent.AkkaGuiceSupport

class AkkaModule extends AbstractModule with AkkaGuiceSupport {
  def configure() = {

    bindActor[CentresProcessor]("centresProcessor")
    bindActor[ParticipantsProcessor]("participantsProcessor")
    bindActor[UsersProcessor]("usersProcessor")

    bindActor[StudiesProcessor]("studiesProcessor")
    bindActor[CeventAnnotationTypeProcessor]("ceventAnnotationType")
    bindActor[CollectionEventTypeProcessor]("collectionEventType")
    bindActor[ParticipantAnnotationTypeProcessor]("participantAnnotationType")
    bindActor[ProcessingTypeProcessor]("processingType")
    bindActor[SpecimenGroupProcessor]("specimenGroup")
    bindActor[SpecimenLinkAnnotationTypeProcessor]("specimenLinkAnnotationType")
    bindActor[SpecimenLinkTypeProcessor]("specimenLinkType")
  }

}
