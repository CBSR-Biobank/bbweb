package org.biobank.services.participants

import org.biobank.domain._
import org.biobank.domain.access._
import org.biobank.domain.participants._
import org.biobank.services.studies.StudiesServiceFixtures
import org.scalatest.prop.TableDrivenPropertyChecks._

trait ParticipantsServiceFixtures extends StudiesServiceFixtures {

  class UsersWithParticipantAccessFixture extends UsersWithStudyAccessFixture {
    val collectorUser     = factory.createActiveUser
    val annotationType    = factory.createAnnotationType
    val annotation        = factory.createAnnotationWithValues(annotationType)
    val enabledStudy      = factory.createEnabledStudy.copy(annotationTypes = Set(annotationType))
    val specimenDesc      = factory.createCollectionSpecimenDefinition
    val participant       = factory.createParticipant.copy(studyId     = enabledStudy.id,
                                                        annotations = Set(annotation))

    override def usersCanAddOrUpdateTable() =
      Table(("users with update access", "label"),
            (allStudiesAdminUser, "all studies admin user"),
            (studyOnlyAdminUser,  "study only admin user"),
            (collectorUser,       "specimen collector user"))

    override def usersCannotAddOrUpdateTable() =
      Table(("users without update access", "label"),
            (noMembershipUser,       "non membership user"),
            (nonStudyPermissionUser, "non study permission user"))


    Set(enabledStudy, participant).foreach(addToRepository)
    addToRepository(studyOnlyMembership.copy(
                      userIds   = studyOnlyMembership.userIds + collectorUser.id,
                      studyData = studyOnlyMembership.studyData.copy(ids = Set(enabledStudy.id))))
    addUserToRole(collectorUser, RoleId.SpecimenCollector)
  }

  class UsersWithCeventAccessFixture extends UsersWithParticipantAccessFixture {
    val ceventAnnotationType = factory.createAnnotationType
    val ceventType = factory.createCollectionEventType.copy(studyId              = enabledStudy.id,
                                                            specimenDefinitions = Set(specimenDesc),
                                                            annotationTypes      = Set(ceventAnnotationType))
    val ceventAnnotation = factory.createAnnotationWithValues(annotationType)
    val cevent = factory.createCollectionEvent.copy(participantId = participant.id,
                                                    annotations   = Set(ceventAnnotation))

    Set(ceventType, cevent).foreach(addToRepository)
  }

  protected val participantRepository: ParticipantRepository

  protected val collectionEventRepository: CollectionEventRepository

  override protected def addToRepository[T <: ConcurrencySafeEntity[_]](entity: T): Unit = {
    entity match {
      case e: Participant     => participantRepository.put(e)
      case e: CollectionEvent => collectionEventRepository.put(e)
      case e                  => super.addToRepository(e)
    }
  }

}
