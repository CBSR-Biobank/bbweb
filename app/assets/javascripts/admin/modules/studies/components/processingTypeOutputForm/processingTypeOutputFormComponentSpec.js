/**
 * Jasmine test suite
 *
 */
/* global angular */

import { ComponentTestSuiteMixin } from 'test/mixins/ComponentTestSuiteMixin';
import ProcessingTypeFixture from 'test/fixtures/ProcessingTypeFixture';
import { ServerReplyMixin } from 'test/mixins/ServerReplyMixin';
import ngModule from '../../index';

describe('processingTypeOutputFormComponent', function() {

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
                              'ProcessingTypeOutputFormButtonConfig',
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
          buttonConfig = this.ProcessingTypeOutputFormButtonConfig.TWO_BUTTON
        ) => {
          this.prevSpy   = jasmine.createSpy('prevSpy');
          this.submitSpy = jasmine.createSpy('submitSpy').and.returnValue(1);
          this.cancelSpy = jasmine.createSpy('cancelSpy');

          ComponentTestSuiteMixin.createController.call(
            this,
            `<processing-type-output-form
                processing-type="vm.processingType"
                button-config=${buttonConfig}
                on-previous="vm.onPrevious"
                on-submit="vm.onSubmit"
                on-cancel="vm.onCancel">
             </processing-type-output-form>`,
            {
              processingType: processingType,
              onPrevious: this.prevSpy,
              onSubmit: this.submitSpy,
              onCancel: this.cancelSpy
            },
            'processingTypeOutputForm');
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
    const processingTypes = [
      new this.ProcessingType(),
      f.processingTypesFromProcessed[0].processingType
    ];

    processingTypes.forEach(processingType => {
      this.createController(f,
                            processingType,
                            this.ProcessingTypeOutputFormButtonConfig.TWO_BUTTON);
      const output = processingType.specimenProcessing.output;

      expect(this.controller.expectedChange).toBe(output.expectedChange);
      expect(this.controller.count).toBe(output.count);
      expect(this.controller.containerTypeId).toBe(output.containerTypeId);
      expect(this.controller.name).toBe(output.specimenDefinition.name);
      expect(this.controller.description).toBe(output.specimenDefinition.description);
      expect(this.controller.units).toBe(output.specimenDefinition.units);
      expect(this.controller.anatomicalSourceType).toBe(output.specimenDefinition.anatomicalSourceType);
      expect(this.controller.preservationType).toBe(output.specimenDefinition.preservationType);
      expect(this.controller.preservationTemperature).toBe(output.specimenDefinition.preservationTemperature);
      expect(this.controller.specimenType).toBe(output.specimenDefinition.specimenType);
    });

  });

  describe('on submit', function() {

    it('processing type is updated', function() {
      const f = this.processingTypeFixture.fixture();
      const processingType = new this.ProcessingType();
      this.createController(f, processingType);

      const output = f.processingTypesFromProcessed[0].processingType.specimenProcessing.output;

      this.controller.expectedChange          = output.expectedChange;
      this.controller.count                   = output.count;
      this.controller.containerTypeId         = output.containerTypeId;
      this.controller.name                    = output.specimenDefinition.name;
      this.controller.description             = output.specimenDefinition.description;
      this.controller.units                   = output.specimenDefinition.units;
      this.controller.anatomicalSourceType    = output.specimenDefinition.anatomicalSourceType;
      this.controller.preservationType        = output.specimenDefinition.preservationType;
      this.controller.preservationTemperature = output.specimenDefinition.preservationTemperature;
      this.controller.specimenType            = output.specimenDefinition.specimenType;

      this.controller.assignValues();

      expect(output.expectedChange).toBe(this.controller.expectedChange);
      expect(output.count).toBe(this.controller.count);
      expect(output.containerTypeId).toBe(this.controller.containerTypeId);
      expect(output.specimenDefinition.name).toBe(this.controller.name);
      expect(output.specimenDefinition.description).toBe(this.controller.description);
      expect(output.specimenDefinition.units).toBe(this.controller.units);
      expect(output.specimenDefinition.anatomicalSourceType).toBe(this.controller.anatomicalSourceType);
      expect(output.specimenDefinition.preservationType).toBe(this.controller.preservationType);
      expect(output.specimenDefinition.preservationTemperature).toBe(this.controller.preservationTemperature);
      expect(output.specimenDefinition.specimenType).toBe(this.controller.specimenType);
    });

    it('pressing submit button assigns values and calls specified function', function() {
      const f = this.processingTypeFixture.fixture();
      const processingType = new this.ProcessingType();
      this.createController(f, processingType);

      this.controller.assignValues = jasmine.createSpy();
      this.controller.submit();
      expect(this.controller.assignValues).toHaveBeenCalled();
      expect(this.submitSpy).toHaveBeenCalled();
    });

    it('pressing previous button assigns values and calls specified function', function() {
      const f = this.processingTypeFixture.fixture();
      const processingType = new this.ProcessingType();
      this.createController(f,
                            processingType,
                            this.ProcessingTypeOutputFormButtonConfig.THREE_BUTTON);

      this.controller.assignValues = jasmine.createSpy();
      this.controller.previous();
      expect(this.controller.assignValues).toHaveBeenCalled();
      expect(this.prevSpy).toHaveBeenCalled();
    });

    it('pressing cancel button calls specified function', function() {
      const f = this.processingTypeFixture.fixture();
      const processingType = new this.ProcessingType();
      this.createController(f, processingType);

      this.controller.cancel();
      expect(this.cancelSpy).toHaveBeenCalled();
    });

  });

});
