package org.biobank.controllers.study

import org.biobank.domain.study.{StudyId, CollectionEventTypeId, ProcessingTypeId, SpecimenLinkTypeId}
import play.api.mvc.PathBindable.Parsing
import play.api.routing.sird._

object StudiesRouting {

  implicit object bindableStudyId extends Parsing[StudyId](
    StudyId.apply,
    _.id,
    (key: String, e: Exception) => s"$key is not a valid study Id"
  )

  implicit object bindableCollectionEventTypeId extends Parsing[CollectionEventTypeId](
    CollectionEventTypeId.apply,
    _.id,
    (key: String, e: Exception) => s"$key is not a valid study Id"
  )

  implicit object bindableProcessingTypeId extends Parsing[ProcessingTypeId](
    ProcessingTypeId.apply,
    _.id,
    (key: String, e: Exception) => s"$key is not a valid study Id"
  )

  implicit object bindableSpecimenLinkTypeId extends Parsing[SpecimenLinkTypeId](
    SpecimenLinkTypeId.apply,
    _.id,
    (key: String, e: Exception) => s"$key is not a valid study Id"
  )

  val studyId: PathBindableExtractor[StudyId] =
    new PathBindableExtractor[StudyId]

  val ceventTypeId: PathBindableExtractor[CollectionEventTypeId] =
    new PathBindableExtractor[CollectionEventTypeId]

  val procTypeId: PathBindableExtractor[ProcessingTypeId] =
    new PathBindableExtractor[ProcessingTypeId]

  val slTypeId: PathBindableExtractor[SpecimenLinkTypeId] =
    new PathBindableExtractor[SpecimenLinkTypeId]

}
