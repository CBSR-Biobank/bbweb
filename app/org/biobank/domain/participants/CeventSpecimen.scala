package org.biobank.domain.participants

/**
 *  Used to link a [[CollectionEvent]] with one or more [[Specimens]].
 */
final case class CeventSpecimen(ceventId: CollectionEventId, specimenId: SpecimenId)
