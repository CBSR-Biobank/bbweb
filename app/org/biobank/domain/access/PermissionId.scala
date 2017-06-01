package org.biobank.domain.access

import play.api.libs.json._
import org.biobank.infrastructure.EnumUtils._

@SuppressWarnings(Array("org.wartremover.warts.Enumeration"))
object PermissionId extends Enumeration {
  type PermissionId = Value

  val Snapshot: Value             = Value("Snapshot")

  val UserUpdate: Value           = Value("UserUpdate")
  val UserChangeState: Value      = Value("UserChangeState")
  val UserRead: Value             = Value("UserRead")

  // TODO: rename "create" to "add"
  val StudyCreate: Value           = Value("StudyCreate")
  val StudyRead: Value             = Value("StudyRead")
  val StudyUpdate: Value           = Value("StudyUpdate")
  val StudyChangeState: Value      = Value("StudyChangeState")

  val CentreCreate: Value          = Value("CentreCreate")
  val CentreRead: Value            = Value("CentreRead")
  val CentreUpdate: Value          = Value("CentreUpdate")
  val CentreDelete: Value          = Value("CentreDelete")
  val CentreChangeState: Value     = Value("CentreChangeState")

  val ParticipantRead: Value       = Value("ParticipantRead")
  val ParticipantCreate: Value     = Value("ParticipantCreate")
  val ParticipantUpdate: Value     = Value("ParticipantUpdate")
  val ParticipantDelete: Value      = Value("ParticipantDelete")

  val CollectionEventRead: Value   = Value("CollectionEventRead")
  val CollectionEventCreate: Value = Value("CollectionEventCreate")
  val CollectionEventUpdate: Value = Value("CollectionEventUpdate")
  val CollectionEventDelete: Value = Value("CollectionEventDelete")

  val SpecimenRead: Value          = Value("SpecimenRead")
  val SpecimenCreate: Value        = Value("SpecimenCreate")
  val SpecimenUpdate: Value        = Value("SpecimenUpdate")
  val SpecimenChangeState: Value   = Value("SpecimenChangeState")
  val SpecimenDelete: Value        = Value("SpecimenDelete")

  val ProcessingEventCreate: Value = Value("ProcessingEventCreate")
  val ProcessingEventRead: Value   = Value("ProcessingEventRead")
  val ProcessingEventUpdate: Value = Value("ProcessingEventUpdate")
  val ProcessingEventDelete: Value = Value("ProcessingEventDelete")

  val ShipmentRead: Value          = Value("ShipmentRead")
  val ShipmentCreate: Value        = Value("ShipmentCreate")
  val ShipmentUpdate: Value        = Value("ShipmentUpdate")
  val ShipmentChangeState: Value   = Value("ShipmentChangeState")
  val ShipmentDelete: Value        = Value("ShipmentDelete")

  val ContainerTypeCreate: Value   = Value("ContainerTypeCreate")
  val ContainerTypeRead: Value     = Value("ContainerTypeRead")
  val ContainerTypeUpdate: Value   = Value("ContainerTypeUpdate")
  val ContainerTypeDelete: Value   = Value("ContainerTypeDelete")

  val ContainerCreate: Value       = Value("ContainerCreate")
  val ContainerRead: Value         = Value("ContainerRead")
  val ContainerUpdate: Value       = Value("ContainerUpdate")
  val ContainerDelete: Value       = Value("ContainerDelete")

  implicit val permissionFormat: Format[PermissionId] =
    enumFormat(org.biobank.domain.access.PermissionId)
}
