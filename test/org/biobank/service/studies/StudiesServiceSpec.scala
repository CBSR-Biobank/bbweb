package org.biobank.service.studies

import org.biobank.fixture._
import org.biobank.domain._
import org.biobank.domain.access._
import org.biobank.domain.ConcurrencySafeEntity
import org.biobank.domain.study._
import org.biobank.domain.user._
import org.biobank.service.{FilterString, SortString}
import org.biobank.service.users._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.prop.TableDrivenPropertyChecks._

/**
 * Primarily these are tests that exercise the User Access aspect of UsersService.
 */
class StudiesServiceSpec
    extends ProcessorTestFixture
    with UserServiceFixtures
    with StudiesServiceFixtures
    with ScalaFutures {

  import org.biobank.TestUtils._
  import org.biobank.infrastructure.command.StudyCommands._

  class StudyOfAllStatesFixure(val adminUser:     ActiveUser,
                               val nonAdminUser:  ActiveUser,
                               val membership:    Membership,
                               val disabledStudy: DisabledStudy,
                               val enabledStudy:  EnabledStudy,
                               val retiredStudy:  RetiredStudy)

  protected val nameGenerator = new NameGenerator(this.getClass)

  protected val accessItemRepository = app.injector.instanceOf[AccessItemRepository]

  protected val membershipRepository = app.injector.instanceOf[MembershipRepository]

  protected val userRepository = app.injector.instanceOf[UserRepository]

  protected val studyRepository = app.injector.instanceOf[StudyRepository]

  protected val collectionEventTypeRepository = app.injector.instanceOf[CollectionEventTypeRepository]

  private val studiesService = app.injector.instanceOf[StudiesService]

  private def studiesOfAllStatesFixure() = {
    val adminUser = factory.createActiveUser
    val disabledStudy = factory.createDisabledStudy
    val enabledStudy = factory.createEnabledStudy
    val retiredStudy = factory.createRetiredStudy
    val membershipStudyInfo = MembershipStudyInfo(false, Set(disabledStudy.id,
                                                             enabledStudy.id,
                                                             retiredStudy.id))
    val membership = factory.createMembership.copy(userIds = Set(adminUser.id),
                                                   studyInfo = membershipStudyInfo)
    val f = new StudyOfAllStatesFixure(adminUser,
                                       factory.createActiveUser,
                                       membership,
                                       disabledStudy,
                                       enabledStudy,
                                       retiredStudy)
    Set(f.adminUser,
        f.nonAdminUser,
        f.membership,
        f.disabledStudy,
        f.enabledStudy,
        f.retiredStudy
    ).foreach(addToRepository)

    val cet = factory.createCollectionEventType
      .copy(studyId              = f.disabledStudy.id,
            specimenDescriptions = Set(factory.createCollectionSpecimenDescription))
    collectionEventTypeRepository.put(cet)

    addUserToStudyAdminRole(f.adminUser.id)
    f
  }

  override protected def addToRepository[T <: ConcurrencySafeEntity[_]](entity: T): Unit = {
    entity match {
      case u: User       => userRepository.put(u)
      case i: AccessItem => accessItemRepository.put(i)
      case s: Study      => studyRepository.put(s)
      case m: Membership => membershipRepository.put(m)
      case _             => fail("invalid entity")
    }
  }

  private def updateCommandsTable(sessionUserId: UserId, study: Study, annotationType: AnnotationType) = {
    Table("study update commands",
          UpdateStudyNameCmd(
            sessionUserId   = Some(sessionUserId.id),
            id              = study.id.id,
            expectedVersion = study.version,
            name            = nameGenerator.next[String]
          ),
          UpdateStudyDescriptionCmd(
            sessionUserId   = Some(sessionUserId.id),
            id              = study.id.id,
            expectedVersion = study.version,
            description     = Some(nameGenerator.next[String])
          ),
          StudyAddParticipantAnnotationTypeCmd(
            sessionUserId   = Some(sessionUserId.id),
            id              = study.id.id,
            expectedVersion = study.version,
            name            = annotationType.name,
            description     = annotationType.description,
            valueType       = annotationType.valueType,
            maxValueCount   = annotationType.maxValueCount,
            options         = annotationType.options,
            required        = annotationType.required
          ),
          StudyUpdateParticipantAnnotationTypeCmd(
            sessionUserId    = Some(sessionUserId.id),
            id               = study.id.id,
            expectedVersion  = study.version,
            annotationTypeId = annotationType.id.id,
            name             = annotationType.name,
            description      = annotationType.description,
            valueType        = annotationType.valueType,
            maxValueCount    = annotationType.maxValueCount,
            options          = annotationType.options,
            required         = annotationType.required
          ),
          UpdateStudyRemoveAnnotationTypeCmd(
            sessionUserId    = Some(sessionUserId.id),
            id               = study.id.id,
            expectedVersion  = study.version,
            annotationTypeId = annotationType.id.id
          )
    )
  }

  private def stateChangeCommandsTable(sessionUserId:  UserId,
                                       disabledStudy:  DisabledStudy,
                                       enabledStudy:   EnabledStudy,
                                       retiredStudy:   RetiredStudy) =
    Table("user sate change commands",
          EnableStudyCmd(
            sessionUserId   = Some(sessionUserId.id),
            id              = disabledStudy.id.id,
            expectedVersion = disabledStudy.version
          ),
          DisableStudyCmd(
            sessionUserId   = Some(sessionUserId.id),
            id              = enabledStudy.id.id,
            expectedVersion = enabledStudy.version
          ),
          RetireStudyCmd(
            sessionUserId   = Some(sessionUserId.id),
            id              = disabledStudy.id.id,
            expectedVersion = disabledStudy.version
          ),
          UnretireStudyCmd(
            sessionUserId   = Some(sessionUserId.id),
            id              = retiredStudy.id.id,
            expectedVersion = retiredStudy.version
          )
    )

  override def beforeEach() {
    super.beforeEach()
    removeUsersFromRepository
    restoreRoles
    studyRepository.removeAll
  }

  describe("StudiesService") {

    describe("a user with the Study Admin role is allowed to") {

      it("retrieve study counts") {
        val f = usersStudyFixture
        studiesService.getStudyCount(f.adminUser.id) mustSucceed { count =>
          count must be (1)
        }
      }

      it("retrieve study counts by status") {
        val f = usersStudyFixture
        studiesService.getCountsByStatus(f.adminUser.id) mustSucceed { counts =>
          counts.total must be (1)
        }
      }

      it("retrieve studies") {
        val f = usersStudyFixture
        studiesService.getStudies(f.adminUser.id, new FilterString(""), new SortString(""))
          .mustSucceed { studies =>
            studies must have length (1)
          }
      }

      it("retrieve study names") {
        val f = usersStudyFixture
        studiesService.getStudyNames(f.adminUser.id, new FilterString(""), new SortString(""))
          .mustSucceed { studies =>
            studies must have length (1)
          }
      }

      it("retrieve a study") {
        val f = usersStudyFixture
        studiesService.getStudy(f.adminUser.id, f.study.id) mustSucceed { result =>
          result.id must be (f.study.id)
        }
      }

      it("retrieve centres for a study") {
        val f = usersStudyFixture
        studiesService.getCentresForStudy(f.adminUser.id, f.study.id) mustSucceed { result =>
          result must have size (0)
        }
      }

      it("update a study") {
        val f = usersStudyFixture
        val annotationType = factory.createAnnotationType

        forAll(updateCommandsTable(f.adminUser.id, f.study, annotationType)) { cmd =>
          val study = cmd match {
              case _: StudyUpdateParticipantAnnotationTypeCmd | _: UpdateStudyRemoveAnnotationTypeCmd =>
                f.study.copy(annotationTypes = Set(annotationType))
              case _ =>
                f.study
            }

          studyRepository.put(study) // restore the study to it's previous state
          studiesService.processCommand(cmd).futureValue mustSucceed { s =>
            s.id must be (study.id)
          }
        }
      }

      it("change a study's state") {
        val f = studiesOfAllStatesFixure
        forAll(stateChangeCommandsTable(f.adminUser.id,
                                        f.disabledStudy,
                                        f.enabledStudy,
                                        f.retiredStudy)) { cmd =>
          Set(f.disabledStudy, f.enabledStudy, f.retiredStudy).foreach(addToRepository)
          studiesService.processCommand(cmd).futureValue mustSucceed { s =>
            s.id.id must be (cmd.id)
          }
        }
      }

    }

    describe("a user without the Study Admin role is not allowed to") {

      it("retrieve study counts") {
        val f = usersStudyFixture
        studiesService.getStudyCount(f.nonAdminUser.id) mustFail "Unauthorized"
      }

      it("retrieve study counts by status") {
        val f = usersStudyFixture
        studiesService.getCountsByStatus(f.nonAdminUser.id) mustFail "Unauthorized"
      }

      it("retrieve studies") {
        val f = usersStudyFixture
        studiesService.getStudies(f.nonAdminUser.id, new FilterString(""), new SortString(""))
          .mustFail("Unauthorized")
      }

      it("retrieve study names") {
        val f = usersStudyFixture
        studiesService.getStudyNames(f.nonAdminUser.id, new FilterString(""), new SortString(""))
          .mustFail("Unauthorized")
      }

      it("retrieve a study") {
        val f = usersStudyFixture
        val study = factory.createEnabledStudy
        studyRepository.put(study)
        studiesService.getStudy(f.nonAdminUser.id, study.id) mustFail "Unauthorized"
      }

      it("retrieve centres for a study") {
        val f = usersStudyFixture
        val study = factory.createEnabledStudy
        studyRepository.put(study)
        studiesService.getCentresForStudy(f.nonAdminUser.id, study.id) mustFail "Unauthorized"
      }

      it("update a study") {
        val f = usersStudyFixture
        val annotationType = factory.createAnnotationType

        forAll(updateCommandsTable(f.nonAdminUser.id, f.study, annotationType)) { cmd =>
          studyRepository.put(f.study) // restore the study to it's previous state
          studiesService.processCommand(cmd).futureValue mustFail "Unauthorized"
        }
      }

      it("change a study's state") {
        val f = studiesOfAllStatesFixure
        forAll(stateChangeCommandsTable(f.nonAdminUser.id,
                                        f.disabledStudy,
                                        f.enabledStudy,
                                        f.retiredStudy)) { cmd =>
          Set(f.disabledStudy, f.enabledStudy, f.retiredStudy).foreach(addToRepository)
          studiesService.processCommand(cmd).futureValue mustFail "Unauthorized"
        }
      }

    }

  }

}
