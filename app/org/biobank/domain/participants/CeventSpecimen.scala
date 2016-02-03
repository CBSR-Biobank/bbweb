package org.biobank.domain.participants

/**
 *  Used to link a [[CollectionEvent]] with one or more [[Specimens]].
 */
case class CeventSpecimen(ceventId: CollectionEventId, specimenId: SpecimenId)
