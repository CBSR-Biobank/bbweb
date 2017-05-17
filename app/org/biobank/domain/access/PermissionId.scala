package org.biobank.domain.access

import play.api.libs.json._
import org.biobank.infrastructure.EnumUtils._

@SuppressWarnings(Array("org.wartremover.warts.Enumeration"))
object PermissionId extends Enumeration {
  type PermissionId = Value

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

  val PatientCreate: Value         = Value("PatientCreate")
  val PatientRead: Value           = Value("PatientRead")
  val PatientUpdate: Value         = Value("PatientUpdate")
  val PatientDelete: Value         = Value("PatientDelete")
  val PatientMerge: Value          = Value("PatientMerge")

  val CollectionEventCreate: Value = Value("CollectionEventCreate")
  val CollectionEventRead: Value   = Value("CollectionEventRead")
  val CollectionEventUpdate: Value = Value("CollectionEventUpdate")
  val CollectionEventDelete: Value = Value("CollectionEventDelete")

  val SpecimenCreate: Value        = Value("SpecimenCreate")
  val SpecimenRead: Value          = Value("SpecimenRead")
  val SpecimenUpdate: Value        = Value("SpecimenUpdate")
  val SpecimenDelete: Value        = Value("SpecimenDelete")

  val ProcessingEventCreate: Value = Value("ProcessingEventCreate")
  val ProcessingEventRead: Value   = Value("ProcessingEventRead")
  val ProcessingEventUpdate: Value = Value("ProcessingEventUpdate")
  val ProcessingEventDelete: Value = Value("ProcessingEventDelete")

  val ShipmentCreate: Value        = Value("ShipmentCreate")
  val ShipmentRead: Value          = Value("ShipmentRead")
  val ShipmentChangeState: Value   = Value("ShipmentChangeState")
  val ShipmentUpdate: Value        = Value("ShipmentUpdate")
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
