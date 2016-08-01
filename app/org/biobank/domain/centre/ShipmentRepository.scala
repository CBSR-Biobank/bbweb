package org.biobank.domain.centre

import com.google.inject.ImplementedBy
import javax.inject.Singleton
import org.biobank.domain._
import scalaz.Scalaz._

import org.slf4j.LoggerFactory

@ImplementedBy(classOf[ShipmentRepositoryImpl])
trait ShipmentRepository extends ReadWriteRepository[ShipmentId, Shipment] {

  /**
   * Returns all shipments either being sent to or being received at the centre with centreId.
   */
  def withCentre(centreId: CentreId): Set[Shipment]

}

@Singleton
class ShipmentRepositoryImpl
    extends ReadWriteRepositoryRefImpl[ShipmentId, Shipment](v => v.id)
    with ShipmentRepository {
  import org.biobank.CommonValidations._

  val log = LoggerFactory.getLogger(this.getClass)

  def nextIdentity: ShipmentId = new ShipmentId(nextIdentityAsString)

  def notFound(id: ShipmentId) = IdNotFound(s"shipment id: $id")

  override def getByKey(id: ShipmentId): DomainValidation[Shipment] = {
    getMap.get(id).toSuccessNel(notFound(id).toString)
  }

  def withCentre(centreId: CentreId): Set[Shipment] = {
    getValues.filter { s => (s.fromCentreId == centreId) || (s.toCentreId == centreId) }.toSet
  }
}
