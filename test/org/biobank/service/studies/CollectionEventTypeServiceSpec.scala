package org.biobank.service.studies

import org.biobank.fixture._
import org.biobank.domain._
import org.biobank.domain.access._
import org.biobank.domain.study._
import org.biobank.domain.user._
import org.biobank.service.users._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.prop.TableDrivenPropertyChecks._

/**
 * Primarily these are tests that exercise the User Access aspect of UsersService.
 */
class CollectionEventTypeServiceSpec
    extends ProcessorTestFixture
    with UserServiceFixtures
    with StudiesServiceFixtures
    with ScalaFutures {

  import org.biobank.TestUtils._
  import org.biobank.infrastructure.command.CollectionEventTypeCommands._

  class UsersCeventTypeFixture(adminUser:          ActiveUser,
                               nonAdminUser:       ActiveUser,
                               membership:         Membership,
                               study:              DisabledStudy,
                               val ceventType:     CollectionEventType,
                               val specimenDesc:   CollectionSpecimenDescription,
                               val annotationType: AnnotationType)
  extends UsersStudyFixture(adminUser, nonAdminUser, membership, study)

  protected val nameGenerator = new NameGenerator(this.getClass)

  protected val accessItemRepository = app.injector.instanceOf[AccessItemRepository]

  protected val membershipRepository = app.injector.instanceOf[MembershipRepository]

  protected val userRepository = app.injector.instanceOf[UserRepository]

  protected val studyRepository = app.injector.instanceOf[StudyRepository]

  protected val collectionEventTypeRepository = app.injector.instanceOf[CollectionEventTypeRepository]

  private val ceventTypeService = app.injector.instanceOf[CollectionEventTypeService]

  private def usersCeventFixture(): UsersCeventTypeFixture = {
    val f = usersStudyFixture
    val specimenDesc = factory.createCollectionSpecimenDescription
    val annotationType = factory.createAnnotationType
    val cet = factory.createCollectionEventType.copy(studyId              = f.study.id,
                                                     specimenDescriptions = Set(specimenDesc),
                                                     annotationTypes      = Set(annotationType))
    collectionEventTypeRepository.put(cet)
    new UsersCeventTypeFixture(adminUser      = f.adminUser,
                               nonAdminUser   = f.nonAdminUser,
                               membership     = f.membership,
                               study          = f.study,
                               ceventType     = cet,
                               specimenDesc   = specimenDesc,
                               annotationType = annotationType)
  }


  private def updateCommandsTable(sessionUserId:  UserId,
                                  study:          Study,
                                  ceventType:     CollectionEventType,
                                  specimenDesc:   CollectionSpecimenDescription,
                                  annotationType: AnnotationType) = {
    Table("collection event type update commands",
          AddCollectionEventTypeCmd(
            sessionUserId   = sessionUserId.id,
            studyId         = study.id.id,
            name            = nameGenerator.next[String],
            description     = None,
            recurring       = true
          ),
          UpdateCollectionEventTypeNameCmd(
            sessionUserId   = sessionUserId.id,
            studyId         = study.id.id,
            id              = ceventType.id.id,
            expectedVersion = ceventType.version,
            name            = nameGenerator.next[String]
          ),
          UpdateCollectionEventTypeDescriptionCmd(
            sessionUserId   = sessionUserId.id,
            studyId         = study.id.id,
            id              = ceventType.id.id,
            expectedVersion = ceventType.version,
            description     = Some(nameGenerator.next[String])
          ),
          UpdateCollectionEventTypeRecurringCmd(
            sessionUserId   = sessionUserId.id,
            studyId         = study.id.id,
            id              = ceventType.id.id,
            expectedVersion = ceventType.version,
            recurring       = !ceventType.recurring
          ),
          CollectionEventTypeAddAnnotationTypeCmd(
            sessionUserId   = sessionUserId.id,
            studyId         = study.id.id,
            id              = ceventType.id.id,
            expectedVersion = ceventType.version,
            name            = annotationType.name,
            description     = annotationType.description,
            valueType       = annotationType.valueType,
            maxValueCount   = annotationType.maxValueCount,
            options         = annotationType.options,
            required        = annotationType.required
          ),
          RemoveCollectionEventTypeAnnotationTypeCmd(
            sessionUserId    = sessionUserId.id,
            studyId          = study.id.id,
            id               = ceventType.id.id,
            expectedVersion  = ceventType.version,
            annotationTypeId = annotationType.id.id
          ),
          AddCollectionSpecimenDescriptionCmd(
            sessionUserId               = sessionUserId.id,
            studyId                     = study.id.id,
            id                          = ceventType.id.id,
            expectedVersion             = ceventType.version,
            name                        = specimenDesc.name,
            description                 = specimenDesc.description,
            units                       = specimenDesc.units,
            anatomicalSourceType        = specimenDesc.anatomicalSourceType,
            preservationType            = specimenDesc.preservationType,
            preservationTemperatureType = specimenDesc.preservationTemperatureType,
            specimenType                = specimenDesc.specimenType,
            maxCount                    = specimenDesc.maxCount,
            amount                      = specimenDesc.amount
          ),
          RemoveCollectionSpecimenDescriptionCmd(
            sessionUserId         = sessionUserId.id,
            studyId               = study.id.id,
            id                    = ceventType.id.id,
            expectedVersion       = ceventType.version,
            specimenDescriptionId = specimenDesc.id.id
          )
    )
  }

  override def beforeEach() {
    super.beforeEach()
    collectionEventTypeRepository.removeAll
  }

  describe("CollectionEventTypeService") {

    describe("a user with the Study Admin role is allowed to") {

      it("retrieve a collection event type ") {
        val f = usersCeventFixture
        ceventTypeService.collectionEventTypeWithId(f.adminUser.id, f.study.id, f.ceventType.id)
          .mustSucceed { result =>
            result.id must be (f.ceventType.id)
          }
      }

      it("query if a collection event type is in use") {
        val f = usersCeventFixture
        ceventTypeService.collectionEventTypeInUse(f.adminUser.id, f.ceventType.id)
          .mustSucceed { result =>
            result must be (false)
          }
      }

      it("retrieve all collection event types for a study") {
        val f = usersCeventFixture
        ceventTypeService.collectionEventTypesForStudy(f.adminUser.id, f.study.id)
          .mustSucceed { result =>
            result must have size 1
          }
      }

      it("update a collection event type") {
        val f = usersCeventFixture
        forAll(updateCommandsTable(f.adminUser.id,
                                   f.study,
                                   f.ceventType,
                                   f.specimenDesc,
                                   f.annotationType)) { cmd =>
          val ceventType = cmd match {
              case _: CollectionEventTypeAddAnnotationTypeCmd =>
                f.ceventType.copy(annotationTypes = Set.empty[AnnotationType])
              case _: AddCollectionSpecimenDescriptionCmd =>
                f.ceventType.copy(specimenDescriptions = Set.empty[CollectionSpecimenDescription])
              case _ =>
                f.ceventType
            }

          collectionEventTypeRepository.put(ceventType) // restore it to it's previous state
          ceventTypeService.processCommand(cmd).futureValue mustSucceed { reply =>
            reply.studyId.id must be (cmd.studyId)
          }
        }
      }

      it("remove a collection event type") {
        val f = usersCeventFixture
        val cmd = RemoveCollectionEventTypeCmd(
          sessionUserId    = f.adminUser.id.id,
          studyId          = f.study.id.id,
          id               = f.ceventType.id.id,
          expectedVersion  = f.ceventType.version
        )

        ceventTypeService.processRemoveCommand(cmd).futureValue mustSucceed { reply =>
          reply must be (true)
        }
      }

    }

    describe("a user without the Study Admin role is not allowed to") {

      it("retrieve a collection event type ") {
        val f = usersCeventFixture
        ceventTypeService.collectionEventTypeWithId(f.nonAdminUser.id, f.study.id, f.ceventType.id)
          .mustFail("Unauthorized")
      }

      it("query if a collection event type is in use") {
        val f = usersCeventFixture
        ceventTypeService.collectionEventTypeInUse(f.nonAdminUser.id, f.ceventType.id)
          .mustFail("Unauthorized")
      }

      it("retrieve all collection event types for a study") {
        val f = usersCeventFixture
        ceventTypeService.collectionEventTypesForStudy(f.nonAdminUser.id, f.study.id)
          .mustFail("Unauthorized")
      }

      it("update a collection event type") {
        val f = usersCeventFixture
        forAll(updateCommandsTable(f.nonAdminUser.id,
                                   f.study,
                                   f.ceventType,
                                   f.specimenDesc,
                                   f.annotationType)) { cmd =>
          ceventTypeService.processCommand(cmd).futureValue mustFail "Unauthorized"
        }
      }

      it("remove a collection event type") {
        val f = usersCeventFixture
        val cmd = RemoveCollectionEventTypeCmd(
          sessionUserId    = f.nonAdminUser.id.id,
          studyId          = f.study.id.id,
          id               = f.ceventType.id.id,
          expectedVersion  = f.ceventType.version
        )

        ceventTypeService.processRemoveCommand(cmd).futureValue mustFail "Unauthorized"
      }

    }

  }

}
