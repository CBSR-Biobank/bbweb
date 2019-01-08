package org.biobank.controllers.studies

import java.time.OffsetDateTime
import org.biobank.controllers.PagedResultsSharedSpec
import org.biobank.domain.Slug
import org.biobank.domain.annotations._
import org.biobank.domain.containers.{ContainerType, ContainerTypeId}
import org.biobank.domain.studies._
import org.biobank.dto.{EntityInfoDto, ProcessedSpecimenDefinitionNames}
import org.biobank.fixtures._
import org.biobank.matchers.PagedResultsMatchers
import org.scalatest.matchers.{MatchResult, Matcher}
import org.scalatest.prop.TableDrivenPropertyChecks._
import play.api.mvc._
import play.api.libs.json._
import play.api.test.Helpers._
import scala.concurrent.Future
import shapeless._

class ProcessingTypesControllerSpec
    extends ControllerFixture
    with AnnotationTypeJson
    with PagedResultsSharedSpec
    with PagedResultsMatchers
    with ProcessingTypeFixtures {

  import org.biobank.TestUtils._
  import org.biobank.matchers.EntityMatchers._
  import org.biobank.matchers.JsonMatchers._

  describe("Processing Type REST API") {

    describe("GET /api/studies/proctypes/:studySlug/:procTypeSlug") {

      it("get a single processing type") {
        val f = collectionSpecimenDefinitionFixtures
        addToRepository(f.processingType)

        val reply = makeAuthRequest(GET, uri(f.study.slug.id, f.processingType.slug.id)).value
        reply must beOkResponseWithJsonReply

        val replyPt = (contentAsJson(reply) \ "data").validate[ProcessingType]
        replyPt must be (jsSuccess)
        replyPt.get must matchProcessingType(f.processingType)
      }

      it("fail for an invalid study slug") {
        val f = collectionSpecimenDefinitionFixtures
        studyRepository.remove(f.study)

        val reply = makeAuthRequest(GET, uri(f.study.slug.id, f.processingType.slug.id))
        reply.value must beNotFoundWithMessage("EntityCriteriaNotFound.*study slug")
      }

      it("fail for an invalid processing type slug") {
        val f = collectionSpecimenDefinitionFixtures
        val reply = makeAuthRequest(GET, uri(f.study.slug.id, f.processingType.slug.id))
        reply.value must beNotFoundWithMessage("EntityCriteriaNotFound.*processing type slug")
      }

    }

    describe("GET /api/studies/proctypes/id/:studyId/:procTypeId") {

      it("get a single processing type by ID") {
        val f = collectionSpecimenDefinitionFixtures
        addToRepository(f.processingType)

        val reply = makeAuthRequest(GET, uri("id", f.study.id.id, f.processingType.id.id)).value
        reply must beOkResponseWithJsonReply

        val replyPt = (contentAsJson(reply) \ "data").validate[ProcessingType]
        replyPt must be (jsSuccess)
        replyPt.get must matchProcessingType(f.processingType)
      }

      it("fail for an invalid study ID") {
        val f = collectionSpecimenDefinitionFixtures
        studyRepository.remove(f.study)

        val reply = makeAuthRequest(GET, uri("id", f.study.id.id, f.processingType.id.id))
        reply.value must beNotFoundWithMessage("IdNotFound.*study id")
      }

      it("fail for an invalid processing type ID") {
        val f = collectionSpecimenDefinitionFixtures
        val reply = makeAuthRequest(GET, uri("id", f.study.id.id, f.processingType.id.id))
        reply.value must beNotFoundWithMessage("IdNotFound.*processing type")
      }

    }

    describe("GET /api/studies/proctypes") {

      it("list none") {
        val study = factory.createDisabledStudy
        studyRepository.put(study)
        val url = new Url(uri(study.slug.id))
        url must beEmptyResults
      }

      describe("list a single processing type") {
        listSingleProcessingType() { () =>
          val study = factory.createDisabledStudy
          studyRepository.put(study)

          val procType = factory.createProcessingType
          processingTypeRepository.put(procType)

          (new Url(uri(study.slug.id)), procType)
        }
      }

      describe("list multiple processing types") {
        listMultipleProcessingTypes() { () =>
          val study = factory.createDisabledStudy
          studyRepository.put(study)

          val procTypes = List(factory.createProcessingType, factory.createProcessingType).sortBy(_.name)
          procTypes.foreach(processingTypeRepository.put)
          (new Url(uri(study.slug.id)), procTypes)
        }
      }

      it("fail for an invalid study ID") {
        val study = factory.createDisabledStudy
        val reply = makeAuthRequest(GET, uri(study.slug.id)).value
        reply must beNotFoundWithMessage("EntityCriteriaNotFound: study slug")
      }

      it("fail for an invalid study ID when using an processing type id") {
        val study = factory.createDisabledStudy
        val procType = factory.createProcessingType
        val reply = makeAuthRequest(GET, uri(study.slug.id, procType.slug.id)).value
        reply must beNotFoundWithMessage("EntityCriteriaNotFound: study slug")
      }

      it("fail for an invalid processing type id") {
        val study = factory.createDisabledStudy
        studyRepository.put(study)
        val procType = factory.createProcessingType
        val reply = makeAuthRequest(GET, uri(study.slug.id, procType.slug.id)).value
        reply must beNotFoundWithMessage("EntityCriteriaNotFound: processing type slug")
      }

      describe("fail when using an invalid query parameters") {
        pagedQueryShouldFailSharedBehaviour { () =>
          val study = factory.createDisabledStudy
          studyRepository.put(study)
          factory.createProcessingType
          new Url(uri(study.slug.id))
        }
      }

    }

    describe("GET /api/studies/proctypes/spcdefs/:studyId") {

      it("can retrieve specimen definitions for a study") {
        val f = new CollectionSpecimenDefinitionFixtures
        Set(f.study, f.collectionEventType, f.processingType).foreach(addToRepository)
        val definition = f.processingType.output.specimenDefinition
        val expectedReply = List(
            ProcessedSpecimenDefinitionNames(
              f.processingType.id.id,
              f.processingType.slug,
              f.processingType.name,
              EntityInfoDto(definition.id.id, definition.slug, definition.name)))

        val reply = makeAuthRequest(GET, uri("spcdefs", f.study.id.id)).value
        reply must beOkResponseWithJsonReply

        val replyDtos = (contentAsJson(reply) \ "data").validate[List[ProcessedSpecimenDefinitionNames]]
        replyDtos must be (jsSuccess)
        replyDtos.get must equal (expectedReply)
      }

      it("fail for an invalid study ID") {
        val study = factory.createDisabledStudy
        val reply = makeAuthRequest(GET, uri("spcdefs", study.id.id)).value
        reply must beNotFoundWithMessage("IdNotFound.*study id")
      }

    }

    describe("GET /api/studies/proctypes/inuse/:id") {

      it("must return false if not in use") {
        val f = collectionSpecimenDefinitionFixtures
        addToRepository(f.processingType)

        val reply = makeAuthRequest(GET, uri("inuse", f.processingType.slug.id)).value
        reply must beOkResponseWithJsonReply

        val replyPt = (contentAsJson(reply) \ "data").validate[Boolean]
        replyPt must be (jsSuccess)
        replyPt.get must be (false)
      }

    }

    describe("POST /api/studies/proctypes/:studyId") {

      it("add a processing type for a collected input specimen") {
        val f = new CollectionSpecimenDefinitionFixtures
        Set(f.study, f.collectionEventType).foreach(addToRepository)

        val reply = makeAuthRequest(POST,
                                    uri(f.study.id.id),
                                    procTypeToAddJson(f.processingType)).value
        reply must beOkResponseWithJsonReply

        val ptId = (contentAsJson(reply) \ "data" \ "id").validate[ProcessingTypeId]
        ptId must be (jsSuccess)

        val sdId =
          (contentAsJson(reply) \ "data" \ "output" \ "specimenDefinition" \ "id")
            .validate[SpecimenDefinitionId]
        sdId must be (jsSuccess)

        sdId.get.id.length must be > 0

        val updateLens = lens[ProcessingType].id ~
          lens[ProcessingType].output.specimenDefinition.id

        val updatedPt = updateLens.set(f.processingType)(Tuple2(ptId.get, sdId.get))
        reply must matchUpdatedProcessingType(updatedPt)
      }

      it("add a processing type for a processed input specimen") {
        val f = new ProcessedSpecimenDefinitionFixtures
        Set(f.study, f.inputProcessingType).foreach(addToRepository)

        val reply = makeAuthRequest(POST,
                                    uri(f.study.id.id),
                                    procTypeToAddJson(f.outputProcessingType)).value
        reply must beOkResponseWithJsonReply

        val ptId = (contentAsJson(reply) \ "data" \ "id").validate[ProcessingTypeId]
        ptId must be (jsSuccess)

        val osdId =
          (contentAsJson(reply) \ "data" \ "output" \ "specimenDefinition" \ "id")
            .validate[SpecimenDefinitionId]
        osdId must be (jsSuccess)

        val updateLens = lens[ProcessingType].id ~
        lens[ProcessingType].output.specimenDefinition.id

        val updatedPt = updateLens.set(f.outputProcessingType)(Tuple2(ptId.get, osdId.get))
        reply must matchUpdatedProcessingType(updatedPt)

        processingTypeRepository.getByKey(f.inputProcessingType.id) mustSucceed { pt =>
          pt.inUse must be(true)
        }
      }

      describe("not add a processing type to an enabled study") {
        addOnNonDisabledStudySharedBehaviour { () =>
          val f = collectionSpecimenDefinitionFixtures
          val enabledStudy = f.study.enable.toOption.value
          studyRepository.put(enabledStudy)
          (new Url(uri(enabledStudy.id.id)), f.processingType)
        }
      }

      describe("not add a processing type to a retired study") {
        addOnNonDisabledStudySharedBehaviour { () =>
          val f = collectionSpecimenDefinitionFixtures
          val enabledStudy = f.study.retire.toOption.value
          studyRepository.put(enabledStudy)
          (new Url(uri(enabledStudy.id.id)), f.processingType)
        }
      }

      it("allow adding a processing type with same name on two different studies") {
        val f1 = collectionSpecimenDefinitionFixtures
        val f2 = collectionSpecimenDefinitionFixtures
        val commonName = nameGenerator.next[ProcessingType]

        val pt1 = f1.processingType.copy(name = commonName)
        val pt2 = f2.processingType.copy(name = commonName)

        var reply = makeAuthRequest(POST, uri(f1.study.id.id), procTypeToAddJson(pt1)).value
        reply must beOkResponseWithJsonReply

        reply = makeAuthRequest(POST, uri(f2.study.id.id), procTypeToAddJson(pt2)).value
        reply must beOkResponseWithJsonReply
      }

      it("cannot add a processing type with the same name as an exising one") {
        val f = collectionSpecimenDefinitionFixtures
        processingTypeRepository.put(f.processingType)

        val reply = makeAuthRequest(POST, uri(f.study.id.id), procTypeToAddJson(f.processingType)).value
        reply must beForbiddenRequestWithMessage(
          "EntityCriteriaError: processing type with name already exists")
      }

      it("fail for an invalid study ID") {
        val f = new CollectionSpecimenDefinitionFixtures
        Set(f.collectionEventType).foreach(addToRepository)
        val reply = makeAuthRequest(POST,
                                    uri(f.study.id.id),
                                    procTypeToAddJson(f.processingType)).value
        reply must beNotFoundWithMessage("IdNotFound: study id")
      }
    }

    describe("POST /studies/proctypes/:studyId/:procTypeId") {

      describe("update a processing type's name") {

        it("update a processing type") {
          val f = collectionSpecimenDefinitionFixtures
          val newName = nameGenerator.next[ProcessingType]
          processingTypeRepository.put(f.processingType)

          val reply = makeUpdateRequest(f.processingType, "name", JsString(newName)).value
          reply must beOkResponseWithJsonReply

          val updatedPt = f.processingType.copy(version      = f.processingType.version + 1,
                                                slug         = Slug(newName),
                                                name         = newName,
                                                timeModified = Some(OffsetDateTime.now))
          reply must matchUpdatedProcessingType(updatedPt)
        }

        it("allow a updating processing types on two different studies to same name") {
          val f1 = collectionSpecimenDefinitionFixtures
          val f2 = collectionSpecimenDefinitionFixtures
          val commonName = nameGenerator.next[ProcessingType]

          val processingTypes = List(f1.processingType, f2.processingType)
          processingTypes.foreach(processingTypeRepository.put)

          processingTypes.zipWithIndex.foreach { case (pt, index) =>
            val reply = makeUpdateRequest(pt, "name", JsString(commonName)).value
            reply must beOkResponseWithJsonReply

            // second pt gets modified slug
            val newSlug = if (index <= 0) Slug(commonName)
                          else Slug(s"${commonName}-1")

            val updatedPt = pt.copy(version      = pt.version + 1,
                                    slug         = newSlug,
                                    name         = commonName,
                                    timeModified = Some(OffsetDateTime.now))
            reply must matchUpdatedProcessingType(updatedPt)
          }
        }

        it("cannot change the name to be the same name as an exising processing type") {
          val f = collectionSpecimenDefinitionFixtures
          val secondProcessingType = factory.createProcessingType
          val dupName = nameGenerator.next[ProcessingType]

          Set(f.processingType.copy(name = dupName), secondProcessingType)
            .foreach(processingTypeRepository.put)

          val reply = makeUpdateRequest(secondProcessingType, "name", JsString(dupName)).value
          reply must beForbiddenRequestWithMessage(
            "EntityCriteriaError: processing type with name already exists")
        }

        it("must not update with an invalid value") {
          val f = collectionSpecimenDefinitionFixtures
          processingTypeRepository.put(f.processingType)
          val reply = makeUpdateRequest(f.processingType, "name", JsNumber(1)).value
          reply must beBadRequestWithMessage("expected.jsstring")
        }

        describe("fail when updating name and an invalid version is used") {
          updateWithInvalidVersionSharedBehaviour { processingType =>
            processingTypeRepository.put(processingType)

            (new Url(uri("update", processingType.studyId.id, processingType.id.id)),
             Json.obj("property" -> "name",
                      "newValue" -> JsString(nameGenerator.next[String])))
          }
        }

        describe("not update when study is not disabled") {
          updateSharedBehaviour { () =>
            ("name", JsString(nameGenerator.next[String]))
          }
        }
      }

      describe("when updating a processing type's description") {

        it("must update with valid descriptions") {
          val f = collectionSpecimenDefinitionFixtures
          processingTypeRepository.put(f.processingType)

          val descriptionValues = List(Some(nameGenerator.next[ProcessingType]), None)

          descriptionValues.zipWithIndex.foreach { case (newValue, index) =>
            val jsValue = newValue match {
                case Some(v) => JsString(v)
                case None    => JsString("")
              }
            val processingType = f.processingType.copy(version = f.processingType.version + index)
            val reply = makeUpdateRequest(processingType, "description", jsValue).value
            reply must beOkResponseWithJsonReply

            val updatedPt = f.processingType.copy(version      = processingType.version + 1,
                                                  description  = newValue,
                                                  timeModified = Some(OffsetDateTime.now))
            reply must matchUpdatedProcessingType(updatedPt)
          }
        }

        it("must not update with an invalid value") {
          val f = collectionSpecimenDefinitionFixtures
          processingTypeRepository.put(f.processingType)
          val reply = makeUpdateRequest(f.processingType, "description", JsNumber(1)).value
          reply must beBadRequestWithMessage("expected.jsstring")
        }

        describe("fail when updating description and an invalid version is used") {
          updateWithInvalidVersionSharedBehaviour { processingType =>
            processingTypeRepository.put(processingType)

            (new Url(uri("update", processingType.studyId.id, processingType.id.id)),
             Json.obj("property" -> "description",
                      "newValue" -> JsString(nameGenerator.next[String])))
          }
        }

        describe("not update when study is not disabled") {
          updateSharedBehaviour { () =>
            ("description", JsString(nameGenerator.next[String]))
          }
        }

      }

      describe("when updating a processing type's enabled state") {

        it("must update with valid values") {
          val f = collectionSpecimenDefinitionFixtures
          processingTypeRepository.put(f.processingType)

          val validValues = List(true, false)

          validValues.zipWithIndex.foreach { case (newValue, index) =>
            val processingType = f.processingType.copy(version = f.processingType.version + index)
            val reply = makeUpdateRequest(processingType, "enabled", JsBoolean(newValue)).value
            reply must beOkResponseWithJsonReply

            val updatedPt = f.processingType.copy(version      = processingType.version + 1,
                                                  enabled      = newValue,
                                                  timeModified = Some(OffsetDateTime.now))
            reply must matchUpdatedProcessingType(updatedPt)
          }
        }

        it("must not update with an invalid value") {
          val f = collectionSpecimenDefinitionFixtures
          processingTypeRepository.put(f.processingType)
          val reply = makeUpdateRequest(f.processingType, "enabled", JsNumber(1)).value
          reply must beBadRequestWithMessage("expected.jsboolean")
        }

        describe("fail when updating 'enabled' and an invalid version is used") {
          updateWithInvalidVersionSharedBehaviour { processingType =>
            processingTypeRepository.put(processingType)

            (new Url(uri("update", processingType.studyId.id, processingType.id.id)),
             Json.obj("property" -> "enabled",
                      "newValue" -> JsBoolean(true)))
          }
        }

        describe("not update when study is not disabled") {
          updateSharedBehaviour { () =>
            ("enabled", JsBoolean(false))
          }
        }

      }

      describe("when updating a processing type's input and output expected change") {
        updateExpectedChangeSharedBehaviour
      }

      describe("when updating a processing type's input and output count") {
        updateCountSharedBehaviour
      }

      describe("when updating a processing type's input and output container type") {
        updateContainerTypeSharedBehaviour
      }

      describe("when updating the input specimen definition") {

        describe("for a processing type from a collected specimen") {

          def commonSetup = {
            val f = collectionSpecimenDefinitionFixtures
            val specimenDefinition = factory.createCollectionSpecimenDefinition
            val eventType =
              factory.createCollectionEventType.copy(specimenDefinitions = Set(specimenDefinition))
            val newValue = f.processingType.input
              .copy(entityId = eventType.id.id, specimenDefinitionId = specimenDefinition.id)

            (f, eventType, newValue)
          }

          it("must update specimen definition with valid values") {
            val (f, eventType, newValue) = commonSetup
            Set(f.processingType, eventType).foreach(addToRepository)

            val reply = makeUpdateRequest(f.processingType,
                                          "inputSpecimenProcessing",
                                          Json.toJson(newValue)).value
            reply must beOkResponseWithJsonReply

            val updateLens = lens[ProcessingType].version ~
              lens[ProcessingType].timeModified ~
              lens[ProcessingType].input

            val updatedProcessingType =
              updateLens.set(f.processingType)(Tuple3(f.processingType.version + 1,
                                                      Some(OffsetDateTime.now),
                                                      newValue))
            reply must matchUpdatedProcessingType(updatedProcessingType)
          }

          it("must fail if collection event type is invalid") {
            val (f, eventType, newValue) = commonSetup
            Set(f.processingType).foreach(addToRepository)

            val reply = makeUpdateRequest(f.processingType,
                                          "inputSpecimenProcessing",
                                          Json.toJson(newValue)).value
            reply must beNotFoundWithMessage("IdNotFound: collection event type")
          }

          it("must fail if collection specimen definition is invalid") {
            val (f, eventType, validValue) = commonSetup
            Set(eventType, f.processingType).foreach(addToRepository)

            val newValue = validValue
              .copy(entityId = eventType.id.id,
                    specimenDefinitionId = SpecimenDefinitionId(nameGenerator.next[String]))

            val reply = makeUpdateRequest(f.processingType,
                                          "inputSpecimenProcessing",
                                          Json.toJson(newValue)).value
            reply must beNotFoundWithMessage("IdNotFound: specimen definition")
          }

        }

        describe("for a processing type from a processed specimen") {

          def commonSetup = {
            val f = processedSpecimenDefinitionFixtures
            val specimenDefinition = factory.createProcessedSpecimenDefinition
            val output = OutputSpecimenProcessing(expectedChange     = 1.0,
                                                  count              = 1,
                                                  containerTypeId    = None,
                                                  specimenDefinition = specimenDefinition)
            val newInputProcessingType = factory.createProcessingType
              .withOutputSpecimenProcessing(output)
              .toOption.value

            val newValue = newInputProcessingType.input
              .copy(definitionType       = ProcessingType.processedDefinition,
                    entityId             = newInputProcessingType.id.id,
                    specimenDefinitionId = specimenDefinition.id)

            (f, newInputProcessingType, newValue)
          }

          it("must update specimen definition with valid values") {
            val (f, newInputProcessingType, newValue) = commonSetup
            Set(f.outputProcessingType, newInputProcessingType).foreach(addToRepository)

            val reply = makeUpdateRequest(f.outputProcessingType,
                                          "inputSpecimenProcessing",
                                          Json.toJson(newValue)).value
            reply must beOkResponseWithJsonReply

            val updateLens = lens[ProcessingType].version ~
            lens[ProcessingType].timeModified ~
            lens[ProcessingType].input

            val updatedProcessingType =
              updateLens.set(f.outputProcessingType)(Tuple3(f.outputProcessingType.version + 1,
                                                            Some(OffsetDateTime.now),
                                                            newValue))
            reply must matchUpdatedProcessingType(updatedProcessingType)
          }

          it("must fail if input processing type is invalid") {
            val (f, newInputProcessingType, newValue) = commonSetup
            Set(f.outputProcessingType).foreach(addToRepository)

            val reply = makeUpdateRequest(f.outputProcessingType,
                                          "inputSpecimenProcessing",
                                          Json.toJson(newValue)).value
            reply must beNotFoundWithMessage("IdNotFound: processing type id")
          }

          it("must fail if input specimen definition is invalid") {
            val (f, newInputProcessingType, validValue) = commonSetup
            Set(f.outputProcessingType, newInputProcessingType).foreach(addToRepository)

            val newValue = validValue
              .copy(specimenDefinitionId = SpecimenDefinitionId(nameGenerator.next[String]))

            val reply = makeUpdateRequest(f.outputProcessingType,
                                          "inputSpecimenProcessing",
                                          Json.toJson(newValue)).value
            reply must beNotFoundWithMessage("IdNotFound: specimen definition")
          }
        }

        describe("fail when updating input specimen info and an invalid version is used") {
          updateWithInvalidVersionSharedBehaviour { processingType =>
            processingTypeRepository.put(processingType)
            val newValue = processingType.input

            (new Url(uri("update", processingType.studyId.id, processingType.id.id)),
             Json.obj("property" -> "inputSpecimenProcessing",
                      "newValue" -> Json.toJson(newValue)))
          }
        }

        describe("fail when an invalid version is used") {
          updateWithInvalidVersionSharedBehaviour { processingType =>
            processingTypeRepository.put(processingType)

            val json = Json.toJson(processingType.input)

            (new Url(uri("update", processingType.studyId.id, processingType.id.id)),
             Json.obj("property" -> "inputSpecimenProcessing", "newValue" -> json))
          }
        }

        describe("not update when study is not disabled") {
          updateSharedBehaviour { () =>
            val f = collectionSpecimenDefinitionFixtures
            ("inputSpecimenProcessing", Json.toJson(f.processingType.input))
          }
        }

      }

      describe("when updating the output specimen definition") {

        def commonSetup = {
          val f = processedSpecimenDefinitionFixtures
          val specimenDefinition = factory.createProcessedSpecimenDefinition
          val output = f.outputProcessingType.output
            .copy(specimenDefinition = specimenDefinition)
          (f, output)
        }

        it("must update the output specimen definition with valid values") {
          val (f, output) = commonSetup
          Set(f.outputProcessingType).foreach(addToRepository)
          val reply = makeUpdateRequest(f.outputProcessingType,
                                        "outputSpecimenProcessing",
                                        Json.toJson(output)).value
          reply must beOkResponseWithJsonReply

          val sdId =
            (contentAsJson(reply) \ "data" \ "output" \ "specimenDefinition" \ "id")
              .validate[SpecimenDefinitionId]
          sdId must be (jsSuccess)

          val updatedSpecimenDefinition = output.specimenDefinition.copy(id = sdId.get)

          val updateLens = lens[ProcessingType].version ~
            lens[ProcessingType].timeModified ~
            lens[ProcessingType].output.specimenDefinition

          val updatedProcessingType =
            updateLens.set(f.outputProcessingType)(Tuple3(f.outputProcessingType.version + 1,
                                                          Some(OffsetDateTime.now),
                                                          updatedSpecimenDefinition))
          reply must matchUpdatedProcessingType(updatedProcessingType)
        }

        it("must fail if processing type is invalid") {
          val (f, specimenDefinition) = commonSetup
          val reply = makeUpdateRequest(f.outputProcessingType,
                                        "outputSpecimenProcessing",
                                        Json.toJson(specimenDefinition)).value
          reply must beNotFoundWithMessage("IdNotFound: processing type id")
        }

        it("must fail if output specimen definition is invalid") {
          val (f, output) = commonSetup
          val updateLens = lens[OutputSpecimenProcessing].specimenDefinition.name
          val invalidOutput = updateLens.set(output)("")
          Set(f.outputProcessingType).foreach(addToRepository)
          val reply = makeUpdateRequest(f.outputProcessingType,
                                        "outputSpecimenProcessing",
                                        Json.toJson(invalidOutput)).value
          reply must beBadRequestWithMessage("NameRequired")
        }

      }

      it("must not update with an invalid value") {
        val f = collectionSpecimenDefinitionFixtures
        processingTypeRepository.put(f.processingType)
        val reply = makeUpdateRequest(f.processingType, "inputSpecimenProcessing", JsNumber(-1)).value
        reply must beBadRequestWithMessage("expected.jsobject")
      }

      describe("fail when updating output specimen info and an invalid version is used") {
        updateWithInvalidVersionSharedBehaviour { processingType =>
          processingTypeRepository.put(processingType)
          (new Url(uri("update", processingType.studyId.id, processingType.id.id)),
           Json.obj("property" -> "outputSpecimenProcessing",
                    "newValue" -> Json.toJson(processingType.output)))
        }
      }

      describe("not update when study is not disabled") {
        updateSharedBehaviour { () =>
          val f = collectionSpecimenDefinitionFixtures
          ("outputSpecimenProcessing", Json.toJson(f.processingType.output))
        }
      }

    }

    it("when updating an invalid field on a processing type") {
      val f = new CollectionSpecimenDefinitionFixtures
      Set(f.study, f.collectionEventType, f.processingType).foreach(addToRepository)
      val reply = makeUpdateRequest(f.processingType,
                                    nameGenerator.next[String],
                                    JsString(nameGenerator.next[String])).value
      reply must beBadRequestWithMessage("processing type does not support updates to property")
    }

    describe("POST /api/studies/proctypes/annottypes/:id") {

      def commonSetup = {
        val f = collectionSpecimenDefinitionFixtures
        val annotType = factory.createAnnotationType

        val reqJson = Json.obj("id"              -> f.processingType.id.id,
                               "studyId"         -> f.processingType.studyId.id,
                               "expectedVersion" -> Some(f.processingType.version)) ++
        annotationTypeToJsonNoId(annotType)
        val url = uri("annottype", f.processingType.id.id)

        (f.processingType, annotType, url, reqJson)
      }

      it("add an annotation type") {
        val (processingType, annotType, url, reqJson) = commonSetup
        Set(processingType).foreach(addToRepository)
        val reply = makeAuthRequest(POST, url, reqJson).value
        reply must beOkResponseWithJsonReply

        val newAnnotationTypeId =
          (contentAsJson(reply) \ "data" \ "annotationTypes" \ 0 \ "id").validate[AnnotationTypeId]
        newAnnotationTypeId must be (jsSuccess)

        val updatedAnnotationType = annotType.copy(id = newAnnotationTypeId.get)
        val updatedPt = processingType.copy(version         = processingType.version + 1,
                                            annotationTypes = Set(updatedAnnotationType),
                                            timeModified    = Some(OffsetDateTime.now))
        reply must matchUpdatedProcessingType(updatedPt)
      }

      it("fail when adding an annotation type and processing type ID does not exist") {
        val (processingType, annotType, url, reqJson) = commonSetup
        val reply = makeAuthRequest(POST, url, reqJson).value
        reply must beNotFoundWithMessage("IdNotFound: processing type id")
      }

      describe("fail when adding an annotation type using an invalid version") {
        updateWithInvalidVersionSharedBehaviour { processingType =>
          processingTypeRepository.put(processingType)

          (new Url(uri("annottype", processingType.id.id)),
           annotationTypeToJsonNoId(factory.createAnnotationType))
        }
      }

      describe("not add an annotation type on a non disabled study") {
        updateWithNonDisabledStudySharedBehaviour { processingType =>
          (new Url(uri("annottype", processingType.id.id)),
           annotationTypeToJsonNoId(factory.createAnnotationType))
        }
      }

    }

    describe("POST /api/studies/proctypes/annottype/:cetId/:annotationTypeId") {

      def commonSetup = {
        val f = collectionSpecimenDefinitionFixtures
        val annotationType = factory.createAnnotationType
        val updatedAnnotationType =
          annotationType.copy(description = Some(nameGenerator.next[ProcessingType]))
        val processingType = f.processingType.copy(annotationTypes = Set(annotationType))

        val reqJson = Json.obj("id"              -> processingType.id.id,
                               "studyId"         -> processingType.studyId.id,
                               "expectedVersion" -> Some(processingType.version)) ++
          annotationTypeToJson(updatedAnnotationType)
        val url = uri("annottype", processingType.id.id, annotationType.id.id)

        (processingType, annotationType, updatedAnnotationType, url, reqJson)
      }

      it("update an annotation type") {
        val (processingType, annotationType, updatedAnnotationType, url, reqJson) = commonSetup
        processingTypeRepository.put(processingType)
        val reply = makeAuthRequest(POST, url, reqJson).value
        reply must beOkResponseWithJsonReply
        val updatedPt = processingType.copy(version         = processingType.version + 1,
                                            annotationTypes = Set(updatedAnnotationType),
                                            timeModified    = Some(OffsetDateTime.now))
        reply must matchUpdatedProcessingType(updatedPt)
      }

      it("fail when updating an annotation type and processing type ID does not exist") {
        val (processingType, annotationType, updatedAnnotationType, url, reqJson) = commonSetup
        val reply = makeAuthRequest(POST, url, reqJson).value
        reply must beNotFoundWithMessage("IdNotFound: processing type id")
      }

      describe("fail when updating an annotation type using invalid version") {
        updateWithInvalidVersionSharedBehaviour { processingType =>
          val annotationType = factory.createAnnotationType
          processingTypeRepository.put(processingType)

          (new Url(uri("annottype", processingType.id.id, annotationType.id.id)),
           annotationTypeToJsonNoId(annotationType))
        }
      }

      describe("not update an annotation type when study is not disabled") {
        updateWithNonDisabledStudySharedBehaviour { processingType =>
          val annotationType = factory.createAnnotationType
          (new Url(uri("annottype", processingType.id.id, annotationType.id.id)),
           annotationTypeToJsonNoId(annotationType))
        }
      }

    }

    describe("DELETE /api/studies/proctypes/:studyId/:id/:ver") {

      it("remove a processing type") {
        val f = new ProcessedSpecimenDefinitionFixtures
        Set(f.study, f.inputProcessingType, f.outputProcessingType).foreach(addToRepository)

        val url = uri(f.study.id.id, f.outputProcessingType.id.id, f.outputProcessingType.version.toString)
        val reply = makeAuthRequest(DELETE, url).value
        reply must beOkResponseWithJsonReply

        val result = (contentAsJson(reply) \ "data").validate[Boolean]
        result must be (jsSuccess)
        result.get must be (true)

        processingTypeRepository.getByKey(f.inputProcessingType.id) mustSucceed { pt =>
          pt.inUse must be(false)
        }
      }

      it("not remove a processing type that is an input to another processing type") {
        val f = processedSpecimenDefinitionFixtures
        addToRepository(f.outputProcessingType)

        val url = uri(f.study.id.id, f.inputProcessingType.id.id, f.inputProcessingType.version.toString)
        val reply = makeAuthRequest(DELETE, url).value
        reply must beBadRequestWithMessage("EntityInUse: processing type in use")
      }

      it("reply is NOT_FOUND when using an invalid study ID") {
        val study = factory.createDisabledStudy

        val procType = factory.createProcessingType
        processingTypeRepository.put(procType)

        val url = uri(study.id.id, procType.id.id, procType.version.toString)
        val reply = makeAuthRequest(DELETE, url).value
        reply must beNotFoundWithMessage("IdNotFound: study id")
      }

      it("reply is NOT_FOUND when using an invalid processing type ID") {
        val study = factory.createDisabledStudy
        studyRepository.put(study)

        val procType = factory.createProcessingType

        val url = uri(study.id.id, procType.id.id, procType.version.toString)
        val reply = makeAuthRequest(DELETE, url).value
        reply must beNotFoundWithMessage("IdNotFound: processing type id")
      }

      it("not remove a processing type on a study that is not disabled") {
        val f = collectionSpecimenDefinitionFixtures
        processingTypeRepository.put(f.processingType)
        val studiesTable = Table("study",
                                 f.study.enable.toOption.value,
                                 f.study.retire.toOption.value)

        forAll (studiesTable) { study =>
          studyRepository.put(study)
          val procType = factory.createProcessingType
          processingTypeRepository.put(procType)

          val url = uri(study.id.id, procType.id.id, procType.version.toString)
          val reply = makeAuthRequest(DELETE, url).value
          reply must beBadRequestWithMessage("InvalidStatus: study not disabled")

        }
      }

    }

    describe("DELETE /api/studies/proctypes/annottype/:id/:ver/:uniqueId") {

      def commonSetup = {
        val f = collectionSpecimenDefinitionFixtures
        val annotationType = factory.createAnnotationType
        val processingType = f.processingType.copy(annotationTypes = Set(annotationType))
        (processingType, annotationType)
      }

      it("remove an annotation type") {
        val (processingType, annotationType) = commonSetup
        processingTypeRepository.put(processingType)
        val url = uri("annottype",
                      processingType.studyId.id,
                      processingType.id.id,
                      processingType.version.toString,
                      annotationType.id.id)
        val reply = makeAuthRequest(DELETE, url).value
        reply must beOkResponseWithJsonReply
        val updatedPt = processingType.copy(version         = processingType.version + 1,
                                            annotationTypes = Set.empty[AnnotationType],
                                            timeModified    = Some(OffsetDateTime.now))
        reply must matchUpdatedProcessingType(updatedPt)
      }

      it("fail when removing annotation type and an invalid version") {
        val (processingType, annotationType) = commonSetup
        processingTypeRepository.put(processingType)
        val badVersion = processingType.version + 1
        val url = uri("annottype",
                      processingType.studyId.id,
                      processingType.id.id,
                      badVersion.toString,
                      annotationType.id.id)
        val reply = makeAuthRequest(DELETE, url).value
        reply must beBadRequestWithMessage("expected version doesn't match current version")
      }

      it("fail when removing annotation type and study ID does not exist") {
        val (processingType, annotationType) = commonSetup
        processingTypeRepository.put(processingType)
        val url = uri("annottype",
                      nameGenerator.next[Study],
                      processingType.id.id,
                      processingType.version.toString,
                      annotationType.id.id)

        val reply = makeAuthRequest(DELETE, url).value
        reply must beNotFoundWithMessage("IdNotFound.*study")
      }

      it("fail when removing annotation type and collection event type ID does not exist") {
        val (processingType, annotationType) = commonSetup
        val url = uri("annottype",
                      processingType.studyId.id,
                      processingType.id.id,
                      processingType.version.toString,
                      annotationType.id.id)

        val reply = makeAuthRequest(DELETE, url).value
        reply must beNotFoundWithMessage("IdNotFound.*processing type")
      }

      it("fail when removing an annotation type that does not exist") {
        val (processingType, annotationType) = commonSetup
        processingTypeRepository.put(processingType)
        val url = uri("annottype",
                      processingType.studyId.id,
                      processingType.id.id,
                      processingType.version.toString,
                      nameGenerator.next[AnnotationType])
        val reply = makeAuthRequest(DELETE, url).value
        reply must beNotFoundWithMessage("annotation type does not exist")
      }

      it("not delete an annotation type when study is not disabled") {
        val f = collectionSpecimenDefinitionFixtures
        processingTypeRepository.put(f.processingType)
        val annotationType = factory.createAnnotationType

        val url = uri("annottype",
                      f.processingType.studyId.id,
                      f.processingType.id.id,
                      f.processingType.version.toString,
                      annotationType.id.id)

        val studiesTable = Table("study",
                                 f.study.enable.toOption.value,
                                 f.study.retire.toOption.value)

        forAll (studiesTable) { study =>
          studyRepository.put(study)
          val reply = makeAuthRequest(DELETE, url).value
          reply must beBadRequestWithMessage("InvalidStatus: study not disabled")
        }
      }

    }
  }

  private def uri(paths: String*): String = {
    if (paths.isEmpty) "/api/studies/proctypes"
    else "/api/studies/proctypes/" + paths.mkString("/")
  }

  private def listSingleProcessingType(offset:    Long = 0,
                                       maybeNext: Option[Int] = None,
                                       maybePrev: Option[Int] = None)
                                      (setupFunc: () => (Url, ProcessingType)) = {

    it("list single processing types") {
      val (url, expectedProcessingType) = setupFunc()
      val reply = makeAuthRequest(GET, url.path).value
      reply must beOkResponseWithJsonReply

      val json = contentAsJson(reply)
      json must beSingleItemResults(offset, maybeNext, maybePrev)

      val replyProcessingTypes = (json \ "data" \ "items").validate[List[ProcessingType]]
      replyProcessingTypes must be (jsSuccess)
      replyProcessingTypes.get.foreach { _ must matchProcessingType(expectedProcessingType) }
    }
  }

  private def listMultipleProcessingTypes(offset:    Long = 0,
                                          maybeNext: Option[Int] = None,
                                          maybePrev: Option[Int] = None)
                                         (setupFunc: () => (Url, List[ProcessingType])) = {

    it("list multiple processing types") {
      val (url, expectedProcessingTypes) = setupFunc()

      val reply = makeAuthRequest(GET, url.path).value
      reply must beOkResponseWithJsonReply

      val json = contentAsJson(reply)
      json must beMultipleItemResults(offset = offset,
                                      total = expectedProcessingTypes.size.toLong,
                                      maybeNext = maybeNext,
                                      maybePrev = maybePrev)

      val replyProcessingTypes = (json \ "data" \ "items").validate[List[ProcessingType]]
      replyProcessingTypes must be (jsSuccess)

      (replyProcessingTypes.get zip expectedProcessingTypes).foreach {
        case (replyProcessingType, processingType) =>
          replyProcessingType must matchProcessingType(processingType)
      }
    }
  }

  private def matchUpdatedProcessingType(processingType: ProcessingType) =
    new Matcher[Future[Result]] {
      def apply (left: Future[Result]) = {
        val replyProcessingType = (contentAsJson(left) \ "data").validate[ProcessingType]
        val jsSuccessMatcher = jsSuccess(replyProcessingType)

        if (!jsSuccessMatcher.matches) {
          jsSuccessMatcher
        } else {
          val replyMatcher = matchProcessingType(processingType)(replyProcessingType.get)

          if (!replyMatcher.matches) {
            MatchResult(false,
                        s"reply does not match expected: ${replyMatcher.failureMessage}",
                        s"reply matches expected: ${replyMatcher.failureMessage}")
          } else {
            matchRepositoryProcessingType(processingType)
          }
        }
      }
    }

  private def matchRepositoryProcessingType =
    new Matcher[ProcessingType] {
      def apply (left: ProcessingType) = {
        processingTypeRepository.getByKey(left.id).fold(
          err => {
            MatchResult(false, s"not found in repository: ${err.head}", "")

          },
          repoCet => {
            val repoMatcher = matchProcessingType(left)(repoCet)
            MatchResult(repoMatcher.matches,
                        s"repository event type does not match expected: ${repoMatcher.failureMessage}",
                        s"repository event type matches expected: ${repoMatcher.failureMessage}")
          }
        )
      }
    }

  protected def collectionSpecimenDefinitionFixtures() = {
    val f = new CollectionSpecimenDefinitionFixtures
    Set(f.study, f.collectionEventType).foreach(addToRepository)
    f
  }

  protected def processedSpecimenDefinitionFixtures() = {
    val f = new ProcessedSpecimenDefinitionFixtures
    Set(f.study, f.inputProcessingType).foreach(addToRepository)
    f
  }

  private def procTypeToAddJson(procType: ProcessingType) = {
    val input = procType.input
    val output = procType.output
    val outputDefinition = output.specimenDefinition

    Json.obj(
      "studyId"             -> procType.studyId.id,
      "name"                -> procType.name,
      "description"         -> procType.description,
      "enabled"             -> procType.enabled,
      "input"  -> Json.obj(
        "expectedChange"       -> input.expectedChange,
        "count"                -> input.count,
        "containerTypeId"      -> input.containerTypeId,
        "definitionType"       -> input.definitionType.id,
        "entityId"             -> input.entityId.toString,
        "specimenDefinitionId" -> input.specimenDefinitionId
      ),
      "output"  -> Json.obj(
        "expectedChange"       -> output.expectedChange,
        "count"                -> output.count,
        "containerTypeId"      -> output.containerTypeId,
        "specimenDefinition"   -> Json.obj(
          "name"                    -> outputDefinition.name,
          "description"             -> outputDefinition.description,
          "units"                   -> outputDefinition.units,
            "anatomicalSourceType"    -> outputDefinition.anatomicalSourceType,
          "preservationType"        -> outputDefinition.preservationType,
          "preservationTemperature" -> outputDefinition.preservationTemperature,
          "specimenType"            -> outputDefinition.specimenType
        )
      ),
      "annotationTypes"   -> procType.annotationTypes)
  }

  private def addOnNonDisabledStudySharedBehaviour(setupFunc: () => (Url, ProcessingType)) {

    it("must be bad request") {
      val (url, processingType) = setupFunc()
      val reply = makeAuthRequest(POST, url.path, procTypeToAddJson(processingType)).value
      reply must beBadRequestWithMessage("InvalidStatus: study not disabled")
    }
  }

  private def makeUpdateRequest(processingType: ProcessingType,
                                property:       String,
                                newValue:       JsValue): Option[Future[Result]] = {
    var json = Json.obj("expectedVersion" -> processingType.version,
                        "property"        -> property)

    if (newValue !== JsNull) {
      json = json ++ Json.obj("newValue" -> newValue)
    }
    makeAuthRequest(POST, uri("update", processingType.studyId.id, processingType.id.id), json)
  }

  private def updateSharedBehaviour(setupFunc: () => (String, JsValue)) {

    it("not update a processing type on a non disabled study") {
      val (property, value) = setupFunc()
      val f = collectionSpecimenDefinitionFixtures
      val studiesTable = Table("study",
                               f.study.enable.toOption.value,
                               f.study.retire.toOption.value)

      processingTypeRepository.put(f.processingType)
      forAll (studiesTable) { study =>
        studyRepository.put(study)
        val reply = makeUpdateRequest(f.processingType, property, value).value
        reply must beBadRequestWithMessage("InvalidStatus: study not disabled")
      }
    }

    it("fail for an invalid study ID") {
      val (property, value) = setupFunc()
      val f = new CollectionSpecimenDefinitionFixtures
      Set(f.collectionEventType, f.processingType).foreach(addToRepository)
      val reply = makeUpdateRequest(f.processingType, property, value).value
      reply must beNotFoundWithMessage("IdNotFound: study id")
    }

    it("fail for an invalid processing type ID") {
      val (property, value) = setupFunc()
      val f = new CollectionSpecimenDefinitionFixtures
      Set(f.study, f.collectionEventType).foreach(addToRepository)
      val reply = makeUpdateRequest(f.processingType, property, value).value
      reply must beNotFoundWithMessage("IdNotFound: processing type id")
    }

    describe("fail for an non disabled study") {
      updateWithNonDisabledStudySharedBehaviour { processingType =>
        val (property, value) = setupFunc()

        (new Url(uri("update", processingType.studyId.id, processingType.id.id)),
         Json.obj("property" -> property,
                  "newValue" -> value))
      }
    }

  }

  private def updateExpectedChangeSharedBehaviour() = {

    def updatedEntities(specimenProcessingType: String,
                        processingType: ProcessingType,
                        newValue: BigDecimal) = {
      if (specimenProcessingType == "inputSpecimenProcessing") {
        val updateLens = lens[ProcessingType].version ~
          lens[ProcessingType].timeModified ~
          lens[ProcessingType].input.expectedChange

        val updatedProcessingType =
          updateLens.set(processingType)(Tuple3(processingType.version + 1,
                                                Some(OffsetDateTime.now),
                                                newValue))

        (Json.toJson(updatedProcessingType.input), updatedProcessingType)

      } else if (specimenProcessingType == "outputSpecimenProcessing") {
        val updateLens = lens[ProcessingType].version ~
          lens[ProcessingType].timeModified ~
          lens[ProcessingType].output.expectedChange

        val updatedProcessingType =
          updateLens.set(processingType)(Tuple3(processingType.version + 1,
                                                Some(OffsetDateTime.now),
                                                newValue))

        (Json.toJson(updatedProcessingType.output), updatedProcessingType)
      } else {
        fail(s"invalid specimen processing type: $specimenProcessingType")
      }
    }

    it("must update expected change with valid values") {
      val f = collectionSpecimenDefinitionFixtures
      val newValue = BigDecimal(0.0001)

      val table = Table("specimen processing", "inputSpecimenProcessing", "outputSpecimenProcessing")
      forAll (table) { specimenProcessingType =>
        processingTypeRepository.put(f.processingType)
        val (json, updatedProcessingType) = updatedEntities(specimenProcessingType,
                                                            f.processingType,
                                                            newValue)

        val reply = makeUpdateRequest(f.processingType, specimenProcessingType, json).value
        reply must beOkResponseWithJsonReply
        reply must matchUpdatedProcessingType(updatedProcessingType)
      }
    }

    it("must not update with a negative number") {
      val f = collectionSpecimenDefinitionFixtures
      processingTypeRepository.put(f.processingType)
      val newValue = BigDecimal(-0.0001)

      val table = Table("specimen processing", "inputSpecimenProcessing", "outputSpecimenProcessing")
      forAll (table) { specimenProcessingType =>
        val (json, updatedProcessingType) = updatedEntities(specimenProcessingType,
                                                            f.processingType,
                                                            newValue)
        val reply = makeUpdateRequest(f.processingType, specimenProcessingType, json).value
        reply must beBadRequestWithMessage("InvalidPositiveNumber")
      }
    }

  }

  private def updateCountSharedBehaviour() = {

    def updatedEntities(specimenProcessingType: String,
                        processingType: ProcessingType,
                        newValue: Int) = {
      if (specimenProcessingType == "inputSpecimenProcessing") {
        val updateLens = lens[ProcessingType].version ~
          lens[ProcessingType].timeModified ~
          lens[ProcessingType].input.count

        val updatedProcessingType =
          updateLens.set(processingType)(Tuple3(processingType.version + 1,
                                                Some(OffsetDateTime.now),
                                                newValue))

        (Json.toJson(updatedProcessingType.input), updatedProcessingType)

      } else if (specimenProcessingType == "outputSpecimenProcessing") {
        val updateLens = lens[ProcessingType].version ~
          lens[ProcessingType].timeModified ~
          lens[ProcessingType].output.count

        val updatedProcessingType =
          updateLens.set(processingType)(Tuple3(processingType.version + 1,
                                                Some(OffsetDateTime.now),
                                                newValue))

        (Json.toJson(updatedProcessingType.output), updatedProcessingType)
      } else {
        fail(s"invalid specimen processing type: $specimenProcessingType")
      }
    }

    it("must update with a valid value") {
      val f = collectionSpecimenDefinitionFixtures
      processingTypeRepository.put(f.processingType)
      val newValue = 10

      val table = Table("specimen processing", "inputSpecimenProcessing", "outputSpecimenProcessing")
      forAll (table) { specimenProcessingType =>
        processingTypeRepository.put(f.processingType)
        val (json, updatedProcessingType) = updatedEntities(specimenProcessingType,
                                                            f.processingType,
                                                            newValue)
        val reply = makeUpdateRequest(f.processingType, specimenProcessingType, json).value
        reply must beOkResponseWithJsonReply
        reply must matchUpdatedProcessingType(updatedProcessingType)
      }
    }

    it("must not update with a negative number") {
      val f = collectionSpecimenDefinitionFixtures
      val newValue = -1

      val table = Table("specimen processing", "inputSpecimenProcessing", "outputSpecimenProcessing")
      forAll (table) { specimenProcessingType =>
        processingTypeRepository.put(f.processingType)
        val (json, updatedProcessingType) = updatedEntities(specimenProcessingType,
                                                            f.processingType,
                                                            newValue)
        val reply = makeUpdateRequest(f.processingType, specimenProcessingType, json).value
        reply must beBadRequestWithMessage("InvalidPositiveNumber")
      }
    }

  }

  private def updateContainerTypeSharedBehaviour() = {


    def updatedEntities(specimenProcessingType: String,
                        processingType: ProcessingType,
                        newValue: Option[ContainerTypeId]) = {
      if (specimenProcessingType == "inputSpecimenProcessing") {
        val updateLens = lens[ProcessingType].version ~
          lens[ProcessingType].timeModified ~
          lens[ProcessingType].input.containerTypeId

        val updatedProcessingType =
          updateLens.set(processingType)(Tuple3(processingType.version + 1,
                                                Some(OffsetDateTime.now),
                                                newValue))

        (Json.toJson(updatedProcessingType.input), updatedProcessingType)

      } else if (specimenProcessingType == "outputSpecimenProcessing") {
        val updateLens = lens[ProcessingType].version ~
          lens[ProcessingType].timeModified ~
          lens[ProcessingType].output.containerTypeId

        val updatedProcessingType =
          updateLens.set(processingType)(Tuple3(processingType.version + 1,
                                                Some(OffsetDateTime.now),
                                                newValue))

        (Json.toJson(updatedProcessingType.output), updatedProcessingType)
      } else {
        fail(s"invalid specimen processing type: $specimenProcessingType")
      }
    }

    it("must update container type with valid values") {
      val f = collectionSpecimenDefinitionFixtures
      val newValues = List(Some(ContainerTypeId(nameGenerator.next[ContainerType])), None)

      newValues.foreach { newValue =>
        processingTypeRepository.put(f.processingType)
        val table = Table("specimen processing", "inputSpecimenProcessing", "outputSpecimenProcessing")
        forAll (table) { specimenProcessingType =>
          processingTypeRepository.put(f.processingType)
          val (json, updatedProcessingType) = updatedEntities(specimenProcessingType,
                                                              f.processingType,
                                                              newValue)
          val reply = makeUpdateRequest(f.processingType, specimenProcessingType, json).value
          reply must beOkResponseWithJsonReply
          reply must matchUpdatedProcessingType(updatedProcessingType)
        }
      }
    }

    it("must not update with an invalid value") {
      val f = collectionSpecimenDefinitionFixtures
      val table = Table("specimen processing", "inputSpecimenProcessing", "outputSpecimenProcessing")
      forAll (table) { specimenProcessingType =>
        processingTypeRepository.put(f.processingType)
        val (json, updatedProcessingType) = updatedEntities(specimenProcessingType,
                                                            f.processingType,
                                                            Some(ContainerTypeId("")))
        val reply = makeUpdateRequest(f.processingType, specimenProcessingType, json).value
        reply must beBadRequestWithMessage("ContainerTypeIdRequired")
      }
    }

  }

  private def updateWithInvalidVersionSharedBehaviour(func: ProcessingType => (Url, JsValue)) {

    it("should be a bad request") {
      val f = collectionSpecimenDefinitionFixtures
      var reqJson = Json.obj("id"              -> f.processingType.id.id,
                             "studyId"         -> f.study.id,
                             "expectedVersion" -> (f.processingType.version + 1))
      val (url, json) = func(f.processingType)
      if (json != JsNull) {
        reqJson = reqJson ++ json.as[JsObject]
      }

      val reply = makeAuthRequest(POST, url.path, reqJson).value
      reply must beBadRequestWithMessage (".*expected version doesn't match current version.*")
    }
  }

  private def updateWithNonDisabledStudySharedBehaviour(func: ProcessingType => (Url, JsValue)) {

    it("should be a bad request") {
      val f = collectionSpecimenDefinitionFixtures
      var reqJson = Json.obj("id"              -> f.processingType.id.id,
                             "studyId"         -> f.study.id,
                             "expectedVersion" -> (f.processingType.version + 1))
      val (url, json) = func(f.processingType)
      if (json != JsNull) {
        reqJson = reqJson ++ json.as[JsObject]
      }

      addToRepository(f.processingType)
      val studiesTable = Table("study",
                               f.study.enable.toOption.value,
                               f.study.retire.toOption.value)

      forAll (studiesTable) { study =>
        addToRepository(study)
        val reply = makeAuthRequest(POST, url.path, reqJson).value
        reply must beBadRequestWithMessage("InvalidStatus: study not disabled")
      }
    }
  }

}
