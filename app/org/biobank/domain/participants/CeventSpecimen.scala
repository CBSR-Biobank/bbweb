package org.biobank.domain.participants

/**
 *  Used to link a [[domain.participants.CollectionEvent CollectionEvent]] with one or more
 *  [[domain.participants.Specimen Specimens]].
 */
final case class CeventSpecimen(ceventId: CollectionEventId, specimenId: SpecimenId)
