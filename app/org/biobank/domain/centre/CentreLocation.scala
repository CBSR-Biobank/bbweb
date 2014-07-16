package org.biobank.domain.centre

import org.biobank.domain.IdentifiedValueObject

/** Used to link a center with a location.
  *
  * A centre can have multiple locations. But a location can be linked to a single centre.
  */
case class CentreLocation(centreId: CentreId, locationId: LocationId)
