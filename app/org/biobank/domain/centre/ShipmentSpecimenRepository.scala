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

}

@Singleton
class ShipmentSpecimenRepositoryImpl
    extends ReadWriteRepositoryRefImpl[ShipmentSpecimenId, ShipmentSpecimen](v => v.id)
    with ShipmentSpecimenRepository {
  import org.biobank.CommonValidations._

  def nextIdentity: ShipmentSpecimenId = new ShipmentSpecimenId(nextIdentityAsString)

  def notFound(id: ShipmentSpecimenId) = IdNotFound(s"shipment specimen id: $id")

  override def getByKey(id: ShipmentSpecimenId): DomainValidation[ShipmentSpecimen] = {
    getMap.get(id).toSuccessNel(notFound(id).toString)
  }

  def allForShipment(id: ShipmentId): Set[ShipmentSpecimen] = {
    getValues.filter { ss => ss.shipmentId == id }.toSet
  }

  def allForSpecimen(id: SpecimenId): Set[ShipmentSpecimen] = {
    getValues.filter { ss => ss.specimenId == id }.toSet
  }
}
