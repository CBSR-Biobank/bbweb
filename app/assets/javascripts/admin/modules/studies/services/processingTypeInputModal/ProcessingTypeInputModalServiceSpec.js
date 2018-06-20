/**
 * Jasmine test suite
 *
 */
/* global angular */

import { ModalTestSuiteMixin } from 'test/mixins/ModalTestSuiteMixin';
import ProcessingTypeFixture from 'test/fixtures/ProcessingTypeFixture';
import { ServerReplyMixin } from 'test/mixins/ServerReplyMixin';
import ngModule from '../../index';

describe('ProcessingTypeInputModalService', function() {

  beforeEach(() => {
    angular.mock.module('ngAnimateMock', ngModule, 'biobank.test');
    angular.mock.inject(function() {
      Object.assign(this, ModalTestSuiteMixin, ServerReplyMixin);

      this.injectDependencies('$q',
                              '$httpBackend',
                              'Study',
                              'ProcessingType',
                              'CollectionEventType',
                              'ProcessingTypeInputModal',
                              'Factory');

      this.processingTypeFixture = new ProcessingTypeFixture(this.Factory,
                                                             this.Study,
                                                             this.CollectionEventType,
                                                             this.ProcessingType);
      this.addModalMatchers();
      this.addCustomMatchers();

      this.openModal =
        (study,
         processingType,
         plainEventTypes = [],
         plainProcessingTypes = []
        ) => {
          if (processingType.specimenProcessing.input.isCollected()) {
            this.$httpBackend.expectGET(this.url('studies/cetypes/spcdefs', study.slug))
              .respond(this.reply(plainEventTypes));
          }

          this.$httpBackend.expectGET(this.url('studies/proctypes/spcdefs', study.id))
            .respond(this.reply(plainProcessingTypes));

          this.modal = this.ProcessingTypeInputModal.open(study, processingType);
          this.modal.result.then(angular.noop, angular.noop);
          this.$rootScope.$digest();
          this.modalElement = this.modalElementFind();
          this.scope = this.modalElement.scope();
        };
    });
  });

  describe('can open modal', function() {

    it('when processing type is a processing type for a collected specimen', function() {
      const f = this.processingTypeFixture.fixture({
        numEventTypes: 1,
        numProcessingTypesFromCollected: 1,
        numProcessingTypesFromProcessed: 1
      });

      this.openModal(f.study, f.processingTypesFromCollected[0].processingType);
      expect(this.$document).toHaveModalsOpen(1);
      this.dismiss();
      expect(this.$document).toHaveModalsOpen(0);
    });

    it('when processing type is a processing type for a processed specimen', function() {
      const f = this.processingTypeFixture.fixture({
        numEventTypes: 1,
        numProcessingTypesFromCollected: 1,
        numProcessingTypesFromProcessed: 1
      });

      this.openModal(f.study, f.processingTypesFromProcessed[0].processingType);
      expect(this.$document).toHaveModalsOpen(1);
      this.dismiss();
      expect(this.$document).toHaveModalsOpen(0);
    });

  });

  it('ok button can be pressed', function() {
    const f = this.processingTypeFixture.fixture({
      numEventTypes: 1,
      numProcessingTypesFromCollected: 1,
      numProcessingTypesFromProcessed: 1
    });

    this.openModal(f.study, f.processingTypesFromProcessed[0].processingType);
    expect(this.$document).toHaveModalsOpen(1);
    this.scope.vm.okPressed();
    this.flush();
    expect(this.$document).toHaveModalsOpen(0);
  });

  it('close button can be pressed', function() {
    const f = this.processingTypeFixture.fixture({
      numEventTypes: 1,
      numProcessingTypesFromCollected: 1,
      numProcessingTypesFromProcessed: 1
    });

    this.openModal(f.study, f.processingTypesFromProcessed[0].processingType);
    expect(this.$document).toHaveModalsOpen(1);
    this.scope.vm.closePressed();
    this.flush();
    expect(this.$document).toHaveModalsOpen(0);
  });

});
