package org.biobank.domain.centre

import com.google.inject.ImplementedBy
import javax.inject.{Inject, Singleton}
import org.biobank.TestData
import org.biobank.domain._
import org.slf4j.{Logger, LoggerFactory}
import scalaz.Scalaz._
import scalaz.Validation.FlatMap._

@ImplementedBy(classOf[ShipmentRepositoryImpl])
trait ShipmentRepository extends ReadWriteRepository[ShipmentId, Shipment] {

  /**
   * Returns all shipments either being sent to or being received at the centre with centreId.
   */
  def withCentre(centreId: CentreId): Set[Shipment]

  def getCreated(id: ShipmentId): DomainValidation[CreatedShipment]

  def getUnpacked(id: ShipmentId): DomainValidation[UnpackedShipment]

}

@Singleton
class ShipmentRepositoryImpl @Inject() (val testData: TestData)
    extends ReadWriteRepositoryRefImpl[ShipmentId, Shipment](v => v.id)
    with ShipmentRepository {
  import org.biobank.CommonValidations._

  val log: Logger = LoggerFactory.getLogger(this.getClass)

  override def init(): Unit = {
    super.init()
    testData.testShipments.foreach(put)
  }

  def nextIdentity: ShipmentId = new ShipmentId(nextIdentityAsString)

  def notFound(id: ShipmentId): IdNotFound = IdNotFound(s"shipment id: $id")

  override def getByKey(id: ShipmentId): DomainValidation[Shipment] = {
    getMap.get(id).toSuccessNel(notFound(id).toString)
  }

  def withCentre(centreId: CentreId): Set[Shipment] = {
    getValues.filter { s => (s.fromCentreId == centreId) || (s.toCentreId == centreId) }.toSet
  }

  def getCreated(id: ShipmentId): DomainValidation[CreatedShipment] = {
    for {
      shipment <- getByKey(id)
      created  <- shipment.isCreated
    } yield created
  }

  def getUnpacked(id: ShipmentId): DomainValidation[UnpackedShipment] = {
    for {
      shipment <- getByKey(id)
      unpacked <- shipment.isUnpacked
    } yield unpacked
  }

}
