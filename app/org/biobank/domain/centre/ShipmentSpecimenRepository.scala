package org.biobank.domain.centre

import com.google.inject.ImplementedBy
import javax.inject.Singleton
import org.biobank.domain._
import org.biobank.domain.participants.SpecimenId
import scalaz.Scalaz._
//import scalaz._

@ImplementedBy(classOf[ShipmentSpecimenRepositoryImpl])
trait ShipmentSpecimenRepository extends ReadWriteRepository[ShipmentSpecimenId, ShipmentSpecimen] {

  def allForShipment(id: ShipmentId): Set[ShipmentSpecimen]

  def allForSpecimen(id: SpecimenId): Set[ShipmentSpecimen]

  def specimenCanBeAddedToShipment(specimenId: SpecimenId, shipmentId: ShipmentId)
      : DomainValidation[Boolean]
}

@Singleton
class ShipmentSpecimenRepositoryImpl
    extends ReadWriteRepositoryRefImpl[ShipmentSpecimenId, ShipmentSpecimen](v => v.id)
    with ShipmentSpecimenRepository {
  import org.biobank.CommonValidations._

  def nextIdentity: ShipmentSpecimenId = new ShipmentSpecimenId(nextIdentityAsString)

  def notFound(id: ShipmentSpecimenId) = IdNotFound(s"shipment specimen id: $id")

  def shipmentAndSpecimenNotFound(shipmentId: ShipmentId, specimenId: SpecimenId) =
    IdNotFound(s"shipment id: $shipmentId, specimen id: $specimenId")

  override def getByKey(id: ShipmentSpecimenId): DomainValidation[ShipmentSpecimen] = {
    getMap.get(id).toSuccessNel(notFound(id).toString)
  }

  def allForShipment(id: ShipmentId): Set[ShipmentSpecimen] = {
    getValues.filter { ss => ss.shipmentId == id }.toSet
  }

  def allForSpecimen(id: SpecimenId): Set[ShipmentSpecimen] = {
    getValues.filter { ss => ss.specimenId == id }.toSet
  }

  def specimenCanBeAddedToShipment(specimenId: SpecimenId, shipmentId: ShipmentId)
      : DomainValidation[Boolean] = {
    val exists = getValues.exists(ss => (ss.shipmentId == shipmentId) && (ss.specimenId == specimenId))
    if (exists) EntityCriteriaError(s"specimen already present in shipment").failureNel[Boolean]
    else true.successNel[String]
  }
}
