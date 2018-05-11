package org.biobank.controllers.centres

import org.biobank.domain.centres._
import org.biobank.fixtures.Url

private[centres] trait ShipmentsControllerSpecUtils {

  protected def uri(paths: String*): Url = {
    val baseUri = "/api/shipments"
    val path = if (paths.isEmpty) baseUri
               else baseUri + "/" + paths.mkString("/")
    return new Url(path)
  }

  protected def uri(shipment: Shipment): Url = uri(shipment.id.id)

  protected def uri(shipment: Shipment, path: String): Url = uri(path, shipment.id.id)

}
