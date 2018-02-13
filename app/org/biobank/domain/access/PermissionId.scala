package org.biobank.domain.access

import play.api.libs.json._
import org.biobank.infrastructure.EnumUtils._

@SuppressWarnings(Array("org.wartremover.warts.Enumeration"))
object PermissionId extends Enumeration {
  type PermissionId = Value

  val Snapshot: Value             = Value("snapshot")

  val UserUpdate: Value           = Value("user-update")
  val UserChangeState: Value      = Value("user-changestate")
  val UserRead: Value             = Value("user-read")

  val RoleCreate: Value            = Value("role-create")
  val RoleRead: Value              = Value("role-read")
  val RoleUpdate: Value            = Value("role-update")
  val RoleDelete: Value            = Value("role-delete")

  val MembershipCreate: Value      = Value("membership-create")
  val MembershipRead: Value        = Value("membership-read")
  val MembershipUpdate: Value      = Value("membership-update")
  val MembershipDelete: Value      = Value("membership-delete")

  // todo: rename "create" to "add"
  val StudyCreate: Value           = Value("study-create")
  val StudyRead: Value             = Value("study-read")
  val StudyUpdate: Value           = Value("study-update")
  val StudyChangeState: Value      = Value("study-changestate")

  val CentreCreate: Value          = Value("centre-create")
  val CentreRead: Value            = Value("centre-read")
  val CentreUpdate: Value          = Value("centre-update")
  val CentreDelete: Value          = Value("centre-delete")
  val CentreChangeState: Value     = Value("centre-changestate")

  val ParticipantRead: Value       = Value("participant-read")
  val ParticipantCreate: Value     = Value("participant-create")
  val ParticipantUpdate: Value     = Value("participant-update")
  val ParticipantDelete: Value     = Value("participant-delete")

  val CollectionEventRead: Value   = Value("collectionevent-read")
  val CollectionEventCreate: Value = Value("collectionevent-create")
  val CollectionEventUpdate: Value = Value("collectionevent-update")
  val CollectionEventDelete: Value = Value("collectionevent-delete")

  val SpecimenRead: Value          = Value("specimen-read")
  val SpecimenCreate: Value        = Value("specimen-create")
  val SpecimenUpdate: Value        = Value("specimen-update")
  val SpecimenChangeState: Value   = Value("specimen-changestate")
  val SpecimenDelete: Value        = Value("specimen-delete")

  val ProcessingEventCreate: Value = Value("processingevent-create")
  val ProcessingEventRead: Value   = Value("processingevent-read")
  val ProcessingEventUpdate: Value = Value("processingevent-update")
  val ProcessingEventDelete: Value = Value("processingevent-delete")

  val ShipmentRead: Value          = Value("shipment-read")
  val ShipmentCreate: Value        = Value("shipment-create")
  val ShipmentUpdate: Value        = Value("shipment-update")
  val ShipmentChangeState: Value   = Value("shipment-changestate")
  val ShipmentDelete: Value        = Value("shipment-delete")

  val ContainerTypeCreate: Value   = Value("containertype-create")
  val ContainerTypeRead: Value     = Value("containertype-read")
  val ContainerTypeUpdate: Value   = Value("containertype-update")
  val ContainerTypeDelete: Value   = Value("containertype-delete")

  val ContainerCreate: Value       = Value("container-create")
  val ContainerRead: Value         = Value("container-read")
  val ContainerUpdate: Value       = Value("container-update")
  val ContainerDelete: Value       = Value("container-delete")

  implicit val permissionFormat: Format[PermissionId] =
    enumFormat(org.biobank.domain.access.PermissionId)
}
