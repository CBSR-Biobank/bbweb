/**
 * Jasmine test suite
 *
 */
/* global angular */

import { ComponentTestSuiteMixin } from 'test/mixins/ComponentTestSuiteMixin';
import ProcessingTypeFixture from 'test/fixtures/ProcessingTypeFixture';
import { ServerReplyMixin } from 'test/mixins/ServerReplyMixin';
import ngModule from '../../index';

describe('processingTypeInputFormComponent', function() {

  beforeEach(() => {
    angular.mock.module(ngModule, 'biobank.test');
    angular.mock.inject(function() {
      Object.assign(this, ComponentTestSuiteMixin, ServerReplyMixin);

      this.injectDependencies('$q',
                              '$state',
                              '$httpBackend',
                              'Study',
                              'CollectionEventType',
                              'ProcessingType',
                              'ProcessingTypeInputFormButtonConfig',
                              'ProcessingTypeAdd',
                              'notificationsService',
                              'modalService',
                              'Factory');

      this.processingTypeFixture = new ProcessingTypeFixture(this.Factory,
                                                             this.Study,
                                                             this.CollectionEventType,
                                                             this.ProcessingType);

      this.createController =
        (
          fixture,
          processingType,
          buttonConfig = this.ProcessingTypeInputFormButtonConfig.TWO_BUTTON
        ) => {
          this.prevSpy   = jasmine.createSpy('prevSpy');
          this.submitSpy = jasmine.createSpy('submitSpy');
          this.cancelSpy = jasmine.createSpy('cancelSpy');

          const getCollectionSpecimenDefinitions = () => {
            if (this.ProcessingTypeAdd.eventTypes === undefined) {
              this.$httpBackend.expectGET(this.url('studies/cetypes/spcdefs', fixture.study.slug))
                .respond(fixture.collectionSpecimenDefinitionNames);
            }

            return this.ProcessingTypeAdd.getCollectionSpecimenDefinitions(fixture.study);
          };

          const getProcessedSpecimenDefinitions = () => {
            if (this.ProcessingTypeAdd.processingTypes === undefined) {
              this.$httpBackend.expectGET(this.url('studies/proctypes/spcdefs', fixture.study.id))
                .respond(fixture.processedSpecimenDefinitionNames);
            }

            return this.ProcessingTypeAdd.getProcessedSpecimenDefinitions(fixture.study);
          };

          ComponentTestSuiteMixin.createController.call(
            this,
            `<processing-type-input-form
                processing-type="vm.processingType"
                get-collection-specimen-definitions="vm.getCollectionSpecimenDefinitions"
                get-processed-specimen-definitions="vm.getProcessedSpecimenDefinitions"
                button-config=${buttonConfig}
                on-previous="vm.onPrevious"
                on-submit="vm.onSubmit"
                on-cancel="vm.onCancel">
             </processing-type-input-form>`,
            {
              processingType: processingType,
              getCollectionSpecimenDefinitions,
              getProcessedSpecimenDefinitions,
              onPrevious: this.prevSpy,
              onSubmit: this.submitSpy,
              onCancel: this.cancelSpy
            },
            'processingTypeInputForm');
          this.$httpBackend.flush();
        };

      this.ProcessingTypeAdd.init();
    });
  });

  afterEach(function() {
    this.$httpBackend.verifyNoOutstandingExpectation();
    this.$httpBackend.verifyNoOutstandingRequest();
  });

  it('should have valid scope', function() {
    const f = this.processingTypeFixture.fixture();
    const processingType = new this.ProcessingType();
    this.createController(f,
                          processingType,
                          this.ProcessingTypeInputFormButtonConfig.TWO_BUTTON);

    expect(this.controller.processingType).toBe(processingType);
    expect(this.controller.getCollectionSpecimenDefinitions()).toBeFunction();
    expect(this.controller.getProcessedSpecimenDefinitions()).toBeFunction();
    expect(this.controller.buttonConfig).toEqual(this.ProcessingTypeInputFormButtonConfig.TWO_BUTTON);
    expect(this.controller.onPrevious()).toBeFunction();
    expect(this.controller.onSubmit()).toBeFunction();
    expect(this.controller.onCancel()).toBeFunction();
  });

  describe('for button config', function() {

    it('2 button config has 2 buttons', function() {
      const f = this.processingTypeFixture.fixture();
      const processingType = new this.ProcessingType();
      this.createController(f,
                            processingType,
                            this.ProcessingTypeInputFormButtonConfig.TWO_BUTTON);

      const buttons = this.element.find('button');
      expect(buttons.length).toBe(2);
      expect(buttons.eq(0).text()).toContain('Submit');
      expect(buttons.eq(1).text()).toContain('Cancel');
    });

    it('3 button config has 3 buttons', function() {
      const f = this.processingTypeFixture.fixture();
      const processingType = new this.ProcessingType();
      this.createController(f,
                            processingType,
                            this.ProcessingTypeInputFormButtonConfig.THREE_BUTTON);
      const buttons = this.element.find('button');
      expect(buttons.length).toBe(3);
      expect(buttons.eq(0).text()).toContain('Previous step');
      expect(buttons.eq(1).text()).toContain('Next step');
      expect(buttons.eq(2).text()).toContain('Cancel');
    });

    it('invalid button config throws exception', function() {
      const f = this.processingTypeFixture.fixture();
      expect(() => {
        this.createController(f, new this.ProcessingType(), this.Factory.stringNext());
      }).toThrowError(/invalid value for button config/);
    });

  });

  describe(' when adding a processing type', function() {

    describe('component initialized correctly', function() {

      it('when there is only one collection event type and there are no processing types', function() {
        const f = this.processingTypeFixture.fixture({
          numEventTypes: 1,
          numProcessingTypesFromCollected: 0,
          numProcessingTypesFromProcessed: 0
        });
        const processingType = new this.ProcessingType();
        this.createController(f, processingType);

        expect(this.controller.validInputType).toBeTrue();
        expect(this.controller.eventTypes).toBeArrayOfSize(1);
        expect(this.controller.eventType).toBe(f.collectionSpecimenDefinitionNames[0].slug);
        expect(this.controller.specimenDefinition).toBeUndefined();
        expect(this.controller.inputTypeIsCollected).toBeTrue();

        expect(this.controller.haveProcessingTypes).toBeFalse();
        expect(this.controller.inputProcessingType).toBeUndefined();
      });

      it('when there are multiple collection event types and there are no processing types', function() {
        const f = this.processingTypeFixture.fixture({
          numEventTypes: 2,
          numProcessingTypesFromCollected: 0,
          numProcessingTypesFromProcessed: 0
        });
        const processingType = new this.ProcessingType();
        this.createController(f, processingType);

        expect(this.controller.validInputType).toBeTrue();
        expect(this.controller.eventTypes).toBeArrayOfSize(f.eventTypes.length);
        expect(this.controller.eventType).toBeUndefined();
        expect(this.controller.specimenDefinition).toBeUndefined();
        expect(this.controller.inputTypeIsCollected).toBeTrue();

        expect(this.controller.haveProcessingTypes).toBeFalse();
        expect(this.controller.inputProcessingType).toBeUndefined();
      });

      it('when there are multiple collection event types and multiple processing types', function() {
        const f = this.processingTypeFixture.fixture({
          numEventTypes: 2,
          numProcessingTypesFromCollected: 2,
          numProcessingTypesFromProcessed: 0
        });
        const processingType = new this.ProcessingType();
        this.createController(f, processingType);

        expect(this.controller.validInputType).toBeUndefined();
        expect(this.controller.eventTypes).toBeUndefined();
        expect(this.controller.eventType).toBeUndefined();
        expect(this.controller.specimenDefinition).toBeUndefined();
        expect(this.controller.inputTypeIsCollected).toBeUndefined();

        expect(this.controller.haveProcessingTypes).toBeTrue();
        expect(this.controller.inputProcessingType).toBeUndefined();
      });

      it('pressing previous button assigns values and calls specified function', function() {
        const f = this.processingTypeFixture.fixture();
        const processingType = new this.ProcessingType();
        this.createController(f, processingType);

        this.controller.assignValues = jasmine.createSpy();
        this.controller.previous();
        expect(this.controller.assignValues).toHaveBeenCalled();
        expect(this.prevSpy).toHaveBeenCalled();
      });

    });

    describe('has shared behaviour', function() {
      var context = {};

      beforeEach(function() {
        context.createController = (fixture, processingType) => {
          this.createController(fixture,
                                processingType,
                                this.ProcessingTypeInputFormButtonConfig.THREE_BUTTON);
        };
      });

      sharedBehaviour(context);

    });

  });

  describe('when modifying an existing processing type', function() {

    describe('is initialized correctly', function() {

      it('when input specimen is a collected specimen', function() {
        const f = this.processingTypeFixture.fixture();
        const processingType = f.processingTypesFromCollected[0].processingType;
        this.createController(f, processingType);

        expect(this.controller.validInputType).toBeTrue();
        expect(this.controller.eventTypes).toBeArrayOfSize(f.eventTypes.length);
        expect(this.controller.eventType).toBeDefined();
        expect(this.controller.specimenDefinition).toBeDefined();
      });

      it('when input specimen is a processed specimen', function() {
        const f = this.processingTypeFixture.fixture();
        const processingType = f.processingTypesFromProcessed[0].processingType;
        this.createController(f, processingType);

        expect(this.controller.validInputType).toBeTrue();
        expect(this.controller.eventTypes).toBeUndefined();
        expect(this.controller.processingTypes).toBeArrayOfSize(f.processingTypesFromCollected.length +
                                                                f.processingTypesFromProcessed.length);
        expect(this.controller.haveProcessingTypes).toBeTrue();
        expect(this.controller.inputProcessingType).toBeDefined();
      });

    });

    describe('shared behaviour', function() {
      var context = {};

      beforeEach(function() {
        context.createController = (fixture, processingType) => {
          expect(fixture.eventTypes[0]).toBeDefined();
          this.createController(fixture,
                                processingType,
                                this.ProcessingTypeInputFormButtonConfig.TWO_BUTTON);
        };
      });

      sharedBehaviour(context);

    });

  });

  function sharedBehaviour(context) {

    describe('(shared)', function() {

      it('radio buttons are not shown if there are no existing processing types', function() {
        const f = this.processingTypeFixture.fixture({
          numEventTypes: 1,
          numProcessingTypesFromCollected: 0,
          numProcessingTypesFromProcessed: 0
        });
        const plainProcessingType = this.processingTypeFixture.fromEventType(f.eventTypes[0].plainEventType);
        const processingType = this.ProcessingType.create(plainProcessingType);
        context.createController(f, processingType);

        const radioButtons = this.element.find('[type="radio"]');
        expect(radioButtons.length).toBe(0);
      });

      it('radio buttons are shown if there are existing processing types', function() {
        const f = this.processingTypeFixture.fixture({
          numEventTypes: 1,
          numProcessingTypesFromCollected: 1,
          numProcessingTypesFromProcessed: 0
        });
        const plainProcessingType = this.processingTypeFixture.fromEventType(f.eventTypes[0].plainEventType);
        const processingType = this.ProcessingType.create(plainProcessingType);
        context.createController(f, processingType);

        const radioButtons = this.element.find('[type="radio"]');
        expect(radioButtons.length).toBe(2);
      });

      it('when collected specimen is selected as input', function() {
        const f = this.processingTypeFixture.fixture({
          numEventTypes: 1,
          numProcessingTypesFromCollected: 1,
          numProcessingTypesFromProcessed: 1
        });
        context.createController(f, f.processingTypesFromProcessed[0].processingType);

        expect(this.controller.inputTypeIsCollected).toBeFalse();

        const radioButtons = this.element.find('[name="collectedSpecimen"]');
        expect(radioButtons.length).toBe(1);
        radioButtons.eq(0).click().trigger('change');
        this.$httpBackend.flush();

        expect(this.controller.inputTypeIsCollected).toBeTrue();
        expect(this.specimenDefinition).toBeUndefined();
      });

      describe('when processed specimen is selected as input', function() {

        it('and there is only 1 processing type', function() {
          const f = this.processingTypeFixture.fixture({
          numEventTypes: 1,
            numProcessingTypesFromCollected: 1,
          numProcessingTypesFromProcessed: 0
        });
          const plainProcessingType = this.processingTypeFixture.fromEventType(f.eventTypes[0].plainEventType);
          const processingType = this.ProcessingType.create(plainProcessingType);
          context.createController(f, processingType);

          expect(this.controller.inputTypeIsCollected).toBeTrue();
          const radioButtons = this.element.find('[name="processedSpecimen"]');
          expect(radioButtons.length).toBe(1);
          radioButtons.eq(0).click().trigger('change');
          expect(this.controller.inputTypeIsCollected).toBeFalse();
          expect(this.controller.inputProcessingType).toBeDefined();
        });

        it('and there are multiple processing types', function() {
          const f = this.processingTypeFixture.fixture();
          const plainProcessingType = this.processingTypeFixture.fromEventType(f.eventTypes[0].plainEventType);
          const processingType = this.ProcessingType.create(plainProcessingType);
          context.createController(f, processingType);

          expect(this.controller.inputTypeIsCollected).toBeTrue();
          const radioButtons = this.element.find('[name="processedSpecimen"]');
          expect(radioButtons.length).toBe(1);
          radioButtons.eq(0).click().trigger('change');
          expect(this.controller.inputTypeIsCollected).toBeFalse();
          expect(this.controller.inputProcessingType).toBeUndefined();
        });

      });

      describe('on submit', function() {

        describe('processing type is updated', function() {

          it('when input selected is collection event type', function() {
            const f = this.processingTypeFixture.fixture();
            const processingType = new this.ProcessingType();
            context.createController(f, processingType);

            this.controller.inputTypeIsCollected = true;
            this.controller.eventType = f.eventTypes[0].eventType;
            this.controller.specimenDefinition = f.eventTypes[0].eventType.specimenDefinitions[0];
            this.controller.expectedChange = 1.1;
            this.controller.count = 10;
            this.controller.containerTypeId = null;
            this.controller.assignValues();

            expect(processingType.specimenProcessing.input.definitionType).toBe('collected');
            expect(processingType.specimenProcessing.input.specimenDefinitionId)
              .toBe(f.eventTypes[0].eventType.specimenDefinitions[0].id);
            expect(processingType.specimenProcessing.input.entityId).toBe(f.eventTypes[0].eventType.id);

            ['expectedChange', 'count', 'containerTypeId'].forEach(attr => {
              expect(processingType.specimenProcessing.input[attr]).toBe(this.controller[attr]);
            });
          });

          it('when input selected is processing type', function() {
            const f = this.processingTypeFixture.fixture();
            const processingType = new this.ProcessingType();
            context.createController(f, processingType);

            this.controller.inputTypeIsCollected = false;
            this.controller.inputProcessingType = f.processedSpecimenDefinitionNames[0];
            this.controller.expectedChange = 1.1;
            this.controller.count = 10;
            this.controller.containerTypeId = null;
            this.controller.assignValues();

            expect(processingType.specimenProcessing.input.definitionType).toBe('processed');
            expect(processingType.specimenProcessing.input.specimenDefinitionId)
              .toBe(f.processedSpecimenDefinitionNames[0].specimenDefinitionName.id);
            expect(processingType.specimenProcessing.input.entityId)
              .toBe(f.processedSpecimenDefinitionNames[0].id);

            ['expectedChange', 'count', 'containerTypeId'].forEach(attr => {
              expect(processingType.specimenProcessing.input[attr]).toBe(this.controller[attr]);
            });
          });

        });

        it('pressing submit button assigns values and calls specified function', function() {
          const f = this.processingTypeFixture.fixture();
          const processingType = new this.ProcessingType();
          context.createController(f, processingType);

          this.controller.assignValues = jasmine.createSpy();
          this.controller.submit();
          expect(this.controller.assignValues).toHaveBeenCalled();
          expect(this.submitSpy).toHaveBeenCalled();
        });

        it('pressing cancel button calls specified function', function() {
          const f = this.processingTypeFixture.fixture();
          const processingType = new this.ProcessingType();
          context.createController(f, processingType);

          this.controller.cancel();
          expect(this.cancelSpy).toHaveBeenCalled();
        });

      });

    });

  }

});
