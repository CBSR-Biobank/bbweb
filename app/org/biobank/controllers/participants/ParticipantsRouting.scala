package org.biobank.controllers.participants

import org.biobank.domain.participants.{ParticipantId, CollectionEventId, SpecimenId}
import play.api.mvc.PathBindable.Parsing
import play.api.routing.sird._

object ParticipantsRouting {

  implicit object bindableParticipantId extends Parsing[ParticipantId](
    ParticipantId.apply,
    _.id,
    (key: String, e: Exception) => s"$key is not a valid participant Id"
  )

  implicit object bindableCollectionEventId extends Parsing[CollectionEventId](
    CollectionEventId.apply,
    _.id,
    (key: String, e: Exception) => s"$key is not a valid collection event Id"
  )

  implicit object bindableSpecimenId extends Parsing[SpecimenId](
    SpecimenId.apply,
    _.id,
    (key: String, e: Exception) => s"$key is not a valid specimen Id"
  )

  val participantId     = new PathBindableExtractor[ParticipantId]
  val collectionEventId = new PathBindableExtractor[CollectionEventId]
  val specimenId        = new PathBindableExtractor[SpecimenId]

}
