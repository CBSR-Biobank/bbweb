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
 * Primarily these are tests that exercise the User Access aspect of StudiesService.
 */
class StudiesServiceSpec
    extends ProcessorTestFixture
    with UserServiceFixtures
    with StudiesServiceFixtures
    with ScalaFutures {

  import org.biobank.TestUtils._
  import org.biobank.infrastructure.command.StudyCommands._

  case class UserMembership(user: User, membership: Membership)

  class StudyOfAllStatesFixure extends UsersWithStudyAccessFixture {
    val disabledStudy = study
    val enabledStudy = factory.createEnabledStudy
    val retiredStudy = factory.createRetiredStudy
    val cet = factory.createCollectionEventType
      .copy(studyId              = disabledStudy.id,
            specimenDescriptions = Set(factory.createCollectionSpecimenDescription))

    Set(disabledStudy, enabledStudy, retiredStudy).foreach(addToRepository)
    collectionEventTypeRepository.put(cet)
    addToRepository(studyOnlyMembership.copy(
        studyInfo = studyOnlyMembership.studyInfo.copy(
            studyIds = Set(disabledStudy.id, enabledStudy.id, retiredStudy.id))))

  }

  protected val nameGenerator = new NameGenerator(this.getClass)

  protected val accessItemRepository = app.injector.instanceOf[AccessItemRepository]

  protected val membershipRepository = app.injector.instanceOf[MembershipRepository]

  protected val userRepository = app.injector.instanceOf[UserRepository]

  protected val studyRepository = app.injector.instanceOf[StudyRepository]

  protected val collectionEventTypeRepository = app.injector.instanceOf[CollectionEventTypeRepository]

  private val studiesService = app.injector.instanceOf[StudiesService]

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

    describe("users with access to a study are allowed to") {

      it("retrieve study counts") {
        val f = new UsersWithStudyAccessFixture
        forAll (f.usersCanReadTable) { (user, label) =>
          info(label)
          studiesService.getStudyCount(user.id) mustSucceed { count =>
            count must be (1)
          }
        }

        info("no membership user")
        studiesService.getStudyCount(f.noMembershipUser.id) mustSucceed { count =>
          count must be (0)
        }
      }

      it("retrieve study counts by status") {
        val f = new UsersWithStudyAccessFixture
        forAll (f.usersCanReadTable) { (user, label) =>
          info(label)
          studiesService.getCountsByStatus(user.id) mustSucceed { counts =>
            counts.total must be (1)
          }
        }

        info("no membership user")
        studiesService.getCountsByStatus(f.noMembershipUser.id) mustSucceed { count =>
          count.total must be (0)
        }
      }

      it("retrieve studies") {
        val f = new UsersWithStudyAccessFixture
        forAll (f.usersCanReadTable) { (user, label) =>
          info(label)
          studiesService.getStudies(user.id, new FilterString(""), new SortString(""))
            .mustSucceed { studies =>
              studies must have length (1)
            }
        }

        info("no membership user")
        studiesService.getStudies(f.noMembershipUser.id, new FilterString(""), new SortString(""))
          .mustSucceed { studies =>
            studies must have length (0)
          }
      }

      it("retrieve study names") {
        val f = new UsersWithStudyAccessFixture
        forAll (f.usersCanReadTable) { (user, label) =>
          info(label)
          studiesService.getStudyNames(user.id, new FilterString(""), new SortString(""))
            .mustSucceed { studies =>
              studies must have length (1)
            }
        }

        info("no membership user")
        studiesService.getStudyNames(f.noMembershipUser.id, new FilterString(""), new SortString(""))
          .mustSucceed { studies =>
            studies must have length (0)
          }
      }

      it("retrieve a study") {
        val f = new UsersWithStudyAccessFixture
        forAll (f.usersCanReadTable) { (user, label) =>
          info(label)
          studiesService.getStudy(user.id, f.study.id) mustSucceed { result =>
            result.id must be (f.study.id)
          }
        }

        info("no membership user")
        studiesService.getStudy(f.noMembershipUser.id, f.study.id) mustFail "Unauthorized"
      }

      it("retrieve centres for a study") {
        val f = new UsersWithStudyAccessFixture
        forAll (f.usersCanReadTable) { (user, label) =>
          info(label)
          studiesService.getCentresForStudy(user.id, f.study.id) mustSucceed { result =>
            result must have size (0)
          }
        }

        info("no membership user")
        studiesService.getCentresForStudy(f.noMembershipUser.id, f.study.id) mustFail "Unauthorized"
      }

      it("111 update a study") {
        val f = new UsersWithStudyAccessFixture
        val annotationType = factory.createAnnotationType

        forAll (f.usersCanUpdateTable) { (user, label) =>
          info(label)

          forAll(updateCommandsTable(user.id, f.study, annotationType)) { cmd =>
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

        forAll (f.usersCannotUpdateTable) { (user, label) =>
          info(s"$label cannot update")
          studyRepository.put(f.study) // restore the study to it's previous state
          forAll(updateCommandsTable(user.id, f.study, annotationType)) { cmd =>
            studiesService.processCommand(cmd).futureValue mustFail "Unauthorized"
          }
        }
      }

      it("change a study's state") {
        val f = new StudyOfAllStatesFixure
        forAll (f.usersCanUpdateTable) { (user, label) =>
          info(label)
          forAll(stateChangeCommandsTable(user.id,
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

    }

    describe("users without access to a study are not allowed to") {

      it("retrieve study counts") {
        val f = new UserWithNoStudyAccessFixture
        studiesService.getStudyCount(f.nonStudyPermissionUser.id) mustFail "Unauthorized"
      }

      it("retrieve study counts by status") {
        val f = new UserWithNoStudyAccessFixture
        studiesService.getCountsByStatus(f.nonStudyPermissionUser.id) mustFail "Unauthorized"
      }

      it("retrieve studies") {
        val f = new UserWithNoStudyAccessFixture
        studiesService.getStudies(f.nonStudyPermissionUser.id, new FilterString(""), new SortString(""))
          .mustFail("Unauthorized")
      }

      it("retrieve study names") {
        val f = new UserWithNoStudyAccessFixture
        studiesService.getStudyNames(f.nonStudyPermissionUser.id, new FilterString(""), new SortString(""))
          .mustFail("Unauthorized")
      }

      it("retrieve a study") {
        val f = new UserWithNoStudyAccessFixture
        studiesService.getStudy(f.nonStudyPermissionUser.id, f.study.id) mustFail "Unauthorized"
      }

      it("retrieve centres for a study") {
        val f = new UserWithNoStudyAccessFixture
        studiesService.getCentresForStudy(f.nonStudyPermissionUser.id, f.study.id) mustFail "Unauthorized"
      }

      it("update a study") {
        val f = new UserWithNoStudyAccessFixture
        val annotationType = factory.createAnnotationType

        forAll(updateCommandsTable(f.nonStudyPermissionUser.id, f.study, annotationType)) { cmd =>
          studyRepository.put(f.study) // restore the study to it's previous state
          studiesService.processCommand(cmd).futureValue mustFail "Unauthorized"
        }
      }

      it("111 change a study's state") {
        val f1 = new UserWithNoStudyAccessFixture
        val f2 = new StudyOfAllStatesFixure
        forAll(stateChangeCommandsTable(f1.nonStudyPermissionUser.id,
                                        f2.disabledStudy,
                                        f2.enabledStudy,
                                        f2.retiredStudy)) { cmd =>
          Set(f2.disabledStudy, f2.enabledStudy, f2.retiredStudy).foreach(addToRepository)
          studiesService.processCommand(cmd).futureValue mustFail "Unauthorized"
        }

        forAll (f2.usersCannotUpdateTable) { (user, label) =>
          info(label)
          studyRepository.put(f2.study) // restore the study to it's previous state
          forAll(stateChangeCommandsTable(user.id,
                                          f2.disabledStudy,
                                          f2.enabledStudy,
                                          f2.retiredStudy)) { cmd =>
            studiesService.processCommand(cmd).futureValue mustFail "Unauthorized"
          }
        }
      }

    }

    describe("studies membership") {

      it("user has access to all studies corresponding his membership") {
        val secondStudy = factory.createDisabledStudy  // should show up in results
        addToRepository(secondStudy)

        val f = new UsersWithStudyAccessFixture
        studiesService.getStudies(f.allStudiesAdminUser.id, new FilterString(""), new SortString(""))
          .mustSucceed { reply =>
            reply must have size (2)
            val studyIds = reply.map(c => c.id)
            studyIds must contain (f.study.id)
            studyIds must contain (secondStudy.id)
          }
      }

      it("user has access only to studies corresponding his membership") {
        val secondStudy = factory.createDisabledStudy  // should not show up in results
        addToRepository(secondStudy)

        val f = new UsersWithStudyAccessFixture
        studiesService.getStudies(f.studyOnlyAdminUser.id, new FilterString(""), new SortString(""))
          .mustSucceed { reply =>
            reply must have size (1)
            reply.map(c => c.id) must contain (f.study.id)
          }
      }

      it("user does not have access to study if not in membership") {
        val f = new UsersWithStudyAccessFixture

        // remove all studies from membership
        val noStudiesMembership = f.studyOnlyMembership.copy(
            studyInfo = f.studyOnlyMembership.studyInfo.copy(studyIds = Set.empty[StudyId]))
        addToRepository(noStudiesMembership)

        // should not show up in results
        val study = factory.createDisabledStudy
        addToRepository(study)

        studiesService.getStudies(f.studyUser.id, new FilterString(""), new SortString(""))
          .mustSucceed { reply =>
            reply must have size (0)
          }
      }

    }

  }

}
