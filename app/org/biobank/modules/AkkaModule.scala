package org.biobank.modules

import org.biobank.service.centres._
import org.biobank.service.study._
import org.biobank.service.participants._
import org.biobank.service.users._

import com.google.inject.AbstractModule
import play.api.libs.concurrent.AkkaGuiceSupport

class AkkaModule extends AbstractModule with AkkaGuiceSupport {
  def configure() = {

    bindActor[CentresProcessor]("centresProcessor")
    bindActor[ShipmentsProcessor]("shipmentsProcessor")
    bindActor[UsersProcessor]("usersProcessor")

    bindActor[ParticipantsProcessor]("participantsProcessor")
    bindActor[CollectionEventsProcessor]("collectionEventsProcessor")
    bindActor[SpecimensProcessor]("specimensProcessor")

    bindActor[StudiesProcessor]("studiesProcessor")
    bindActor[CollectionEventTypeProcessor]("collectionEventType")
    bindActor[ProcessingTypeProcessor]("processingType")
    bindActor[SpecimenLinkTypeProcessor]("specimenLinkType")
  }

}
