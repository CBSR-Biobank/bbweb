package org.biobank.services.studies

import org.biobank.fixture._
import org.biobank.domain.access._
import org.biobank.domain.annotations._
import org.biobank.domain.studies._
import org.biobank.domain.users._
import org.biobank.services.{FilterString, SortString}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.prop.TableDrivenPropertyChecks._

/**
 * Primarily these are tests that exercise the User Access aspect of CollectionEventTypeService.
 */
class CollectionEventTypeServiceSpec
    extends ProcessorTestFixture
    with StudiesServiceFixtures
    with ScalaFutures {

  import org.biobank.TestUtils._
  import org.biobank.infrastructure.commands.CollectionEventTypeCommands._

  class UsersCeventTypeFixture extends UsersWithStudyAccessFixture {
    val specimenDesc = factory.createCollectionSpecimenDefinition
    val annotationType = factory.createAnnotationType
    val ceventType = factory.createCollectionEventType
      .copy(studyId              = study.id,
            specimenDefinitions = Set(specimenDesc),
            annotationTypes      = Set(annotationType))
    collectionEventTypeRepository.put(ceventType)
  }

  protected val nameGenerator = new NameGenerator(this.getClass)

  protected val accessItemRepository = app.injector.instanceOf[AccessItemRepository]

  protected val membershipRepository = app.injector.instanceOf[MembershipRepository]

  protected val userRepository = app.injector.instanceOf[UserRepository]

  protected val studyRepository = app.injector.instanceOf[StudyRepository]

  protected val collectionEventTypeRepository = app.injector.instanceOf[CollectionEventTypeRepository]

  private val ceventTypeService = app.injector.instanceOf[CollectionEventTypeService]

  private def updateCommandsTable(sessionUserId:  UserId,
                                  study:          Study,
                                  ceventType:     CollectionEventType,
                                  specimenDesc:   CollectionSpecimenDefinition,
                                  annotationType: AnnotationType) = {
    Table("collection event type update commands",
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
          AddCollectionSpecimenDefinitionCmd(
            sessionUserId               = sessionUserId.id,
            studyId                     = study.id.id,
            id                          = ceventType.id.id,
            expectedVersion             = ceventType.version,
            name                        = specimenDesc.name,
            description                 = specimenDesc.description,
            units                       = specimenDesc.units,
            anatomicalSourceType        = specimenDesc.anatomicalSourceType,
            preservationType            = specimenDesc.preservationType,
            preservationTemperature = specimenDesc.preservationTemperature,
            specimenType                = specimenDesc.specimenType,
            maxCount                    = specimenDesc.maxCount,
            amount                      = specimenDesc.amount
          ),
          RemoveCollectionSpecimenDefinitionCmd(
            sessionUserId         = sessionUserId.id,
            studyId               = study.id.id,
            id                    = ceventType.id.id,
            expectedVersion       = ceventType.version,
            specimenDefinitionId = specimenDesc.id.id
          )
    )
  }

  override def beforeEach() {
    super.beforeEach()
    collectionEventTypeRepository.removeAll
  }

  describe("CollectionEventTypeService") {

    describe("when getting a collection event type") {

      it("users can access") {
        val f = new UsersCeventTypeFixture
        forAll (f.usersCanReadTable) { (user, label) =>
          info(label)
          ceventTypeService.eventTypeWithId(user.id, f.study.id, f.ceventType.id)
            .mustSucceed { result =>
              result.id must be (f.ceventType.id)
            }
        }
      }

      it("users cannot access") {
        val f = new UsersCeventTypeFixture
        info("no membership user")
        ceventTypeService.eventTypeWithId(f.noMembershipUser.id, f.study.id, f.ceventType.id)
          .mustFail("Unauthorized")

        info("no permission user")
        ceventTypeService.eventTypeWithId(f.nonStudyPermissionUser.id, f.study.id, f.ceventType.id)
          .mustFail("Unauthorized")

      }

    }

    describe("when querying if a collection event type is in use") {

      it("users can access") {
        val f = new UsersCeventTypeFixture
        forAll (f.usersCanReadTable) { (user, label) =>
          info(label)
          ceventTypeService.eventTypeInUse(user.id, f.ceventType.slug)
            .mustSucceed { result =>
              result must be (false)
            }
        }
      }

      it("users cannot access") {
        val f = new UsersCeventTypeFixture
        info("no membership user")
        ceventTypeService.eventTypeInUse(f.noMembershipUser.id, f.ceventType.slug)
          .mustFail("Unauthorized")

        info("no permission user")
        ceventTypeService.eventTypeInUse(f.nonStudyPermissionUser.id, f.ceventType.slug)
          .mustFail("Unauthorized")
      }

    }

    describe("when getting collection event types for a study") {

      it("users can access") {
        val f = new UsersCeventTypeFixture
        forAll (f.usersCanReadTable) { (user, label) =>
          info(label)
          ceventTypeService.list(user.id, f.study.id, new FilterString(""), new SortString(""))
            .mustSucceed { result =>
              result must have size 1
            }
        }
      }

      it("users cannot access") {
        val f = new UsersCeventTypeFixture
        info("no membership user")
        ceventTypeService.list(f.noMembershipUser.id, f.study.id, new FilterString(""), new SortString(""))
          .mustFail("Unauthorized")

        info("no permission user")
        ceventTypeService.list(f.nonStudyPermissionUser.id, f.study.id, new FilterString(""), new SortString(""))
          .mustFail("Unauthorized")
      }

    }

    describe("when adding a collection event type") {

      it("users can access") {
        val f = new UsersCeventTypeFixture
        forAll (f.usersCanAddOrUpdateTable) { (user, label) =>
          val cmd = AddCollectionEventTypeCmd(sessionUserId   = user.id.id,
                                              studyId         = f.study.id.id,
                                              name            = nameGenerator.next[String],
                                              description     = None,
                                              recurring       = true)
          collectionEventTypeRepository.removeAll
          ceventTypeService.processCommand(cmd).futureValue mustSucceed { reply =>
            reply.studyId must be (f.study.id)
          }
        }
      }

      it("users cannot access") {
        val f = new UsersCeventTypeFixture
        forAll (f.usersCannotAddOrUpdateTable) { (user, label) =>
          val cmd = AddCollectionEventTypeCmd(sessionUserId   = user.id.id,
                                              studyId         = f.study.id.id,
                                              name            = nameGenerator.next[String],
                                              description     = None,
                                              recurring       = true)
          collectionEventTypeRepository.removeAll
          ceventTypeService.processCommand(cmd).futureValue mustFail "Unauthorized"
        }
      }

    }

    describe("when updating a collection event type") {

      it("users with access") {
        val f = new UsersCeventTypeFixture
        forAll (f.usersCanAddOrUpdateTable) { (user, label) =>
          info(label)
          forAll(updateCommandsTable(user.id,
                                     f.study,
                                     f.ceventType,
                                     f.specimenDesc,
                                     f.annotationType)) { cmd =>
            val ceventType = cmd match {
                case _: CollectionEventTypeAddAnnotationTypeCmd =>
                  f.ceventType.copy(annotationTypes = Set.empty[AnnotationType])
                case _: AddCollectionSpecimenDefinitionCmd =>
                  f.ceventType.copy(specimenDefinitions = Set.empty[CollectionSpecimenDefinition])
                case _ =>
                  f.ceventType
              }

            collectionEventTypeRepository.put(ceventType) // restore it to it's previous state
            ceventTypeService.processCommand(cmd).futureValue mustSucceed { reply =>
              reply.studyId.id must be (cmd.studyId)
            }
          }
        }
      }

      it("users without access") {
        val f = new UsersCeventTypeFixture
        forAll (f.usersCannotAddOrUpdateTable) { (user, label) =>
          forAll(updateCommandsTable(user.id,
                                     f.study,
                                     f.ceventType,
                                     f.specimenDesc,
                                     f.annotationType)) { cmd =>
            ceventTypeService.processCommand(cmd).futureValue mustFail "Unauthorized"
          }
        }
      }

    }

    describe("when removing a collection event type") {

      it("users with access") {
        val f = new UsersCeventTypeFixture
        forAll (f.usersCanAddOrUpdateTable) { (user, label) =>
          info(label)
          val cmd = RemoveCollectionEventTypeCmd(
              sessionUserId    = user.id.id,
              studyId          = f.study.id.id,
              id               = f.ceventType.id.id,
              expectedVersion  = f.ceventType.version
            )

          collectionEventTypeRepository.put(f.ceventType) // restore it to it's previous state
          ceventTypeService.processRemoveCommand(cmd).futureValue mustSucceed { reply =>
            reply must be (true)
          }
        }
      }

      it("users without access") {
        val f = new UsersCeventTypeFixture
        forAll (f.usersCannotAddOrUpdateTable) { (user, label) =>
          info(label)
          val cmd = RemoveCollectionEventTypeCmd(
              sessionUserId    = user.id.id,
              studyId          = f.study.id.id,
              id               = f.ceventType.id.id,
              expectedVersion  = f.ceventType.version
            )

          ceventTypeService.processRemoveCommand(cmd).futureValue mustFail "Unauthorized"
        }
      }
    }

  }

}
