package org.biobank.domain.access

import play.api.libs.json._
import org.biobank.infrastructure.EnumUtils._

@SuppressWarnings(Array("org.wartremover.warts.Enumeration"))
object PermissionId extends Enumeration {
  type PermissionId = Value

  val Snapshot: Value             = Value("Snapshot")

  val UserUpdate: Value           = Value("User Update")
  val UserChangeState: Value      = Value("User ChangeState")
  val UserRead: Value             = Value("User Read")

  val RoleCreate: Value            = Value("Role Create")
  val RoleRead: Value              = Value("Role Read")
  val RoleUpdate: Value            = Value("Role Update")
  val RoleDelete: Value            = Value("Role Delete")

  val MembershipCreate: Value      = Value("Membership Create")
  val MembershipRead: Value        = Value("Membership Read")
  val MembershipUpdate: Value      = Value("Membership Update")
  val MembershipDelete: Value      = Value("Membership Delete")

  // TODO: rename "create" to "add"
  val StudyCreate: Value           = Value("Study Create")
  val StudyRead: Value             = Value("Study Read")
  val StudyUpdate: Value           = Value("Study Update")
  val StudyChangeState: Value      = Value("Study ChangeState")

  val CentreCreate: Value          = Value("Centre Create")
  val CentreRead: Value            = Value("Centre Read")
  val CentreUpdate: Value          = Value("Centre Update")
  val CentreDelete: Value          = Value("Centre Delete")
  val CentreChangeState: Value     = Value("Centre ChangeState")

  val ParticipantRead: Value       = Value("Participant Read")
  val ParticipantCreate: Value     = Value("Participant Create")
  val ParticipantUpdate: Value     = Value("Participant Update")
  val ParticipantDelete: Value     = Value("Participant Delete")

  val CollectionEventRead: Value   = Value("CollectionEvent Read")
  val CollectionEventCreate: Value = Value("CollectionEvent Create")
  val CollectionEventUpdate: Value = Value("CollectionEvent Update")
  val CollectionEventDelete: Value = Value("CollectionEvent Delete")

  val SpecimenRead: Value          = Value("Specimen Read")
  val SpecimenCreate: Value        = Value("Specimen Create")
  val SpecimenUpdate: Value        = Value("Specimen Update")
  val SpecimenChangeState: Value   = Value("Specimen ChangeState")
  val SpecimenDelete: Value        = Value("Specimen Delete")

  val ProcessingEventCreate: Value = Value("ProcessingEvent Create")
  val ProcessingEventRead: Value   = Value("ProcessingEvent Read")
  val ProcessingEventUpdate: Value = Value("ProcessingEvent Update")
  val ProcessingEventDelete: Value = Value("ProcessingEvent Delete")

  val ShipmentRead: Value          = Value("Shipment Read")
  val ShipmentCreate: Value        = Value("Shipment Create")
  val ShipmentUpdate: Value        = Value("Shipment Update")
  val ShipmentChangeState: Value   = Value("Shipment ChangeState")
  val ShipmentDelete: Value        = Value("Shipment Delete")

  val ContainerTypeCreate: Value   = Value("ContainerType Create")
  val ContainerTypeRead: Value     = Value("ContainerType Read")
  val ContainerTypeUpdate: Value   = Value("ContainerType Update")
  val ContainerTypeDelete: Value   = Value("ContainerType Delete")

  val ContainerCreate: Value       = Value("Container Create")
  val ContainerRead: Value         = Value("Container Read")
  val ContainerUpdate: Value       = Value("Container Update")
  val ContainerDelete: Value       = Value("Container Delete")

  implicit val permissionFormat: Format[PermissionId] =
    enumFormat(org.biobank.domain.access.PermissionId)
}
