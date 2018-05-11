package org.biobank.domain.studies

import org.biobank.domain._
import scalaz.Scalaz._

trait HasSpecimenDefinitions[T <: SpecimenDefinition] {

  import org.biobank.CommonValidations._

  val specimenDefinitions: Set[T]

  def specimenDefinition(id: SpecimenDefinitionId): DomainValidation[T] = {
    specimenDefinitions.find(_.id == id).toSuccessNel(s"IdNotFound: specimen definition not found: $id")
  }

  protected def checkAddSpecimenDefinition(specimenDefinition: T): DomainValidation[Boolean] = {
    nameNotUsed(specimenDefinition).map { _ => true }
  }

  protected def checkRemoveSpecimenDefinition(specimenDefinitionId: SpecimenDefinitionId)
      : DomainValidation[T] = {
    specimenDefinitions
      .find { x => x.id == specimenDefinitionId }
      .toSuccessNel(s"specimen definition does not exist: $specimenDefinitionId")
  }

  protected def nameNotUsed(specimenDefinition: SpecimenDefinition): DomainValidation[Boolean] = {
    val nameLowerCase = specimenDefinition.name.toLowerCase
    specimenDefinitions
      .find { x => (x.name.toLowerCase == nameLowerCase) && (x.id != specimenDefinition.id)  }
      match {
        case Some(_) =>
          EntityCriteriaError(s"specimen definition name already used: ${specimenDefinition.name}").failureNel[Boolean]
        case None =>
          true.successNel[DomainError]
      }
  }

}
