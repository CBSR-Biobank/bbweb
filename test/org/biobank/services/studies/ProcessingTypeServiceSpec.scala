package org.biobank.services.studies

import org.biobank.fixtures._
import org.biobank.domain.access._
//import org.biobank.domain.annotations._
import org.biobank.domain.studies._
import org.biobank.domain.users._
import org.biobank.fixtures.ProcessingTypeFixtures
import org.biobank.services.{FilterString, PagedQuery, SortString}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.prop.TableDrivenPropertyChecks._

/**
 * Primarily these are tests that exercise the User Access aspect of ProcessingTypeService.
 */
class ProcessingTypeServiceSpec
    extends ProcessorTestFixture
    with ProcessingTypeFixtures
    with StudiesServiceFixtures
    with ScalaFutures {

  import org.biobank.TestUtils._
  //import org.biobank.infrastructure.commands.ProcessingTypeCommands._

  private class UsersProcessingTypeFixture extends UsersWithStudyAccessFixture {
    private val processingFixture = new ProcessedSpecimenDefinitionFixtures

    val inputProcessingType = processingFixture.inputProcessingType.copy(studyId = study.id)
    processingTypeRepository.put(inputProcessingType)
  }

  protected val nameGenerator = new NameGenerator(this.getClass)

  protected val accessItemRepository = app.injector.instanceOf[AccessItemRepository]

  protected val membershipRepository = app.injector.instanceOf[MembershipRepository]

  protected val userRepository = app.injector.instanceOf[UserRepository]

  protected val studyRepository = app.injector.instanceOf[StudyRepository]

  protected val collectionEventTypeRepository = app.injector.instanceOf[CollectionEventTypeRepository]

  protected val processingTypeRepository = app.injector.instanceOf[ProcessingTypeRepository]

  private val processingTypeService = app.injector.instanceOf[ProcessingTypeService]

  // private def updateCommandsTable(sessionUserId:  UserId,
  //                                 study:          Study,
  //                                 processingType:     ProcessingType,
  //                                 specimenDefinition:   CollectionSpecimenDefinition,
  //                                 annotationType: AnnotationType) = {
  //   Table("processing type update commands",
  //         UpdateProcessingTypeNameCmd(
  //           sessionUserId   = sessionUserId.id,
  //           studyId         = study.id.id,
  //           id              = processingType.id.id,
  //           expectedVersion = processingType.version,
  //           name            = nameGenerator.next[String]
  //         ),
  //         UpdateProcessingTypeDescriptionCmd(
  //           sessionUserId   = sessionUserId.id,
  //           studyId         = study.id.id,
  //           id              = processingType.id.id,
  //           expectedVersion = processingType.version,
  //           description     = Some(nameGenerator.next[String])
  //         ),
  //         UpdateProcessingTypeRecurringCmd(
  //           sessionUserId   = sessionUserId.id,
  //           studyId         = study.id.id,
  //           id              = processingType.id.id,
  //           expectedVersion = processingType.version,
  //           recurring       = !processingType.recurring
  //         ),
  //         ProcessingTypeAddAnnotationTypeCmd(
  //           sessionUserId   = sessionUserId.id,
  //           studyId         = study.id.id,
  //           id              = processingType.id.id,
  //           expectedVersion = processingType.version,
  //           name            = annotationType.name,
  //           description     = annotationType.description,
  //           valueType       = annotationType.valueType,
  //           maxValueCount   = annotationType.maxValueCount,
  //           options         = annotationType.options,
  //           required        = annotationType.required
  //         ),
  //         RemoveProcessingTypeAnnotationTypeCmd(
  //           sessionUserId    = sessionUserId.id,
  //           studyId          = study.id.id,
  //           id               = processingType.id.id,
  //           expectedVersion  = processingType.version,
  //           annotationTypeId = annotationType.id.id
  //         ),
  //         AddCollectionSpecimenDefinitionCmd(
  //           sessionUserId               = sessionUserId.id,
  //           studyId                     = study.id.id,
  //           id                          = processingType.id.id,
  //           expectedVersion             = processingType.version,
  //           name                        = specimenDefinition.name,
  //           description                 = specimenDefinition.description,
  //           units                       = specimenDefinition.units,
  //           anatomicalSourceType        = specimenDefinition.anatomicalSourceType,
  //           preservationType            = specimenDefinition.preservationType,
  //           preservationTemperature = specimenDefinition.preservationTemperature,
  //           specimenType                = specimenDefinition.specimenType,
  //           maxCount                    = specimenDefinition.maxCount,
  //           amount                      = specimenDefinition.amount
  //         ),
  //         RemoveCollectionSpecimenDefinitionCmd(
  //           sessionUserId         = sessionUserId.id,
  //           studyId               = study.id.id,
  //           id                    = processingType.id.id,
  //           expectedVersion       = processingType.version,
  //           specimenDefinitionId = specimenDefinition.id.id
  //         )
  //   )
  // }

  override def beforeEach() {
    super.beforeEach()
    processingTypeRepository.removeAll
  }

  describe("ProcessingTypeService") {

    describe("when getting a processing type") {

      it("users can access") {
        val f = new UsersProcessingTypeFixture
        forAll (f.usersCanReadTable) { (user, label) =>
          info(label)
          processingTypeService.processingTypeBySlug(user.id, f.study.slug, f.inputProcessingType.slug)
            .futureValue
            .mustSucceed { result =>
              result.id must be (f.inputProcessingType.id)
            }
        }
      }

      it("users cannot access") {
        val f = new UsersProcessingTypeFixture
        info("no membership user")
        processingTypeService.processingTypeBySlug(f.noMembershipUser.id,
                                                   f.study.slug,
                                                   f.inputProcessingType.slug)
          .futureValue
          .mustFail("Unauthorized")

        info("no permission user")
        processingTypeService.processingTypeBySlug(f.nonStudyPermissionUser.id,
                                                   f.study.slug,
                                                   f.inputProcessingType.slug)
          .futureValue
          .mustFail("Unauthorized")

      }

    }

    describe("when getting processing types for a study") {

      it("users can access") {
        val f = new UsersProcessingTypeFixture
        val query = PagedQuery(new FilterString(""), new SortString(""), 0 , 10)

        forAll (f.usersCanReadTable) { (user, label) =>
          info(label)
          processingTypeService.processingTypesForStudy(user.id, f.study.slug, query).futureValue
            .mustSucceed { result =>
              result.items must have size 1
            }
        }
      }

      it("users cannot access") {
        val f = new UsersProcessingTypeFixture
        val query = PagedQuery(new FilterString(""), new SortString(""), 0 , 1)

        info("no membership user")
        processingTypeService.processingTypesForStudy(f.noMembershipUser.id,
                                                      f.study.slug,
                                                      query)
          .futureValue.mustFail("Unauthorized")

        info("no permission user")
        processingTypeService.processingTypesForStudy(f.nonStudyPermissionUser.id,
                                                      f.study.slug,
                                                      query)
          .futureValue.mustFail("Unauthorized")
      }

    }

  //   describe("when adding a processing type") {

  //     it("users can access") {
  //       val f = new UsersProcessingTypeFixture
  //       forAll (f.usersCanAddOrUpdateTable) { (user, label) =>
  //         val cmd = AddProcessingTypeCmd(sessionUserId   = user.id.id,
  //                                             studyId         = f.study.id.id,
  //                                             name            = nameGenerator.next[String],
  //                                             description     = None,
  //                                             recurring       = true)
  //         processingTypeRepository.removeAll
  //         processingTypeService.processCommand(cmd).futureValue mustSucceed { reply =>
  //           reply.studyId must be (f.study.id)
  //         }
  //       }
  //     }

  //     it("users cannot access") {
  //       val f = new UsersProcessingTypeFixture
  //       forAll (f.usersCannotAddOrUpdateTable) { (user, label) =>
  //         val cmd = AddProcessingTypeCmd(sessionUserId   = user.id.id,
  //                                             studyId         = f.study.id.id,
  //                                             name            = nameGenerator.next[String],
  //                                             description     = None,
  //                                             recurring       = true)
  //         processingTypeRepository.removeAll
  //         processingTypeService.processCommand(cmd).futureValue mustFail "Unauthorized"
  //       }
  //     }

  //   }

  //   describe("when updating a processing type") {

  //     it("users with access") {
  //       val f = new UsersProcessingTypeFixture
  //       forAll (f.usersCanAddOrUpdateTable) { (user, label) =>
  //         info(label)
  //         forAll(updateCommandsTable(user.id,
  //                                    f.study,
  //                                    f.processingType,
  //                                    f.specimenDefinition,
  //                                    f.annotationType)) { cmd =>
  //           val processingType = cmd match {
  //               case _: ProcessingTypeAddAnnotationTypeCmd =>
  //                 f.processingType.copy(annotationTypes = Set.empty[AnnotationType])
  //               case _: AddCollectionSpecimenDefinitionCmd =>
  //                 f.processingType.copy(specimenDefinitions = Set.empty[CollectionSpecimenDefinition])
  //               case _ =>
  //                 f.processingType
  //             }

  //           processingTypeRepository.put(processingType) // restore it to it's previous state
  //           processingTypeService.processCommand(cmd).futureValue mustSucceed { reply =>
  //             reply.studyId.id must be (cmd.studyId)
  //           }
  //         }
  //       }
  //     }

  //     it("users without access") {
  //       val f = new UsersProcessingTypeFixture
  //       forAll (f.usersCannotAddOrUpdateTable) { (user, label) =>
  //         forAll(updateCommandsTable(user.id,
  //                                    f.study,
  //                                    f.processingType,
  //                                    f.specimenDefinition,
  //                                    f.annotationType)) { cmd =>
  //           processingTypeService.processCommand(cmd).futureValue mustFail "Unauthorized"
  //         }
  //       }
  //     }

  //   }

  //   describe("when removing a processing type") {

  //     it("users with access") {
  //       val f = new UsersProcessingTypeFixture
  //       forAll (f.usersCanAddOrUpdateTable) { (user, label) =>
  //         info(label)
  //         val cmd = RemoveProcessingTypeCmd(
  //             sessionUserId    = user.id.id,
  //             studyId          = f.study.id.id,
  //             id               = f.processingType.id.id,
  //             expectedVersion  = f.processingType.version
  //           )

  //         processingTypeRepository.put(f.processingType) // restore it to it's previous state
  //         processingTypeService.processRemoveCommand(cmd).futureValue mustSucceed { reply =>
  //           reply must be (true)
  //         }
  //       }
  //     }

  //     it("users without access") {
  //       val f = new UsersProcessingTypeFixture
  //       forAll (f.usersCannotAddOrUpdateTable) { (user, label) =>
  //         info(label)
  //         val cmd = RemoveProcessingTypeCmd(
  //             sessionUserId    = user.id.id,
  //             studyId          = f.study.id.id,
  //             id               = f.processingType.id.id,
  //             expectedVersion  = f.processingType.version
  //           )

  //         processingTypeService.processRemoveCommand(cmd).futureValue mustFail "Unauthorized"
  //       }
  //     }
  //   }

  }

}
