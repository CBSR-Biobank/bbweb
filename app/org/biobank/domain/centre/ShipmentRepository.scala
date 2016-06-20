package org.biobank.domain.centre

import com.google.inject.ImplementedBy
import javax.inject.Singleton
import org.biobank.domain._
import scalaz.Scalaz._

@ImplementedBy(classOf[ShipmentRepositoryImpl])
trait ShipmentRepository extends ReadWriteRepository[ShipmentId, Shipment] {
}

@Singleton
class ShipmentRepositoryImpl
    extends ReadWriteRepositoryRefImpl[ShipmentId, Shipment](v => v.id)
    with ShipmentRepository {
  import org.biobank.CommonValidations._

  def nextIdentity: ShipmentId = new ShipmentId(nextIdentityAsString)

  def notFound(id: ShipmentId) = IdNotFound(s"shipment id: $id")

  override def getByKey(id: ShipmentId): DomainValidation[Shipment] = {
    getMap.get(id).toSuccessNel(notFound(id).toString)
  }

}
