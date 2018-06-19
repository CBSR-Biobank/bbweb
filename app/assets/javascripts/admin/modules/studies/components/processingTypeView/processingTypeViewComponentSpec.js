/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */
/* global angular */

import { ComponentTestSuiteMixin } from 'test/mixins/ComponentTestSuiteMixin';
import ProcessingTypeFixture from 'test/fixtures/ProcessingTypeFixture';
import entityUpdateSharedBehaviour from 'test/behaviours/entityUpdateSharedBehaviour';
import ngModule from '../../index'

describe('processingTypeViewComponent', function() {

  beforeEach(() => {
    angular.mock.module(ngModule, 'biobank.test');
    angular.mock.inject(function() {
      Object.assign(this, ComponentTestSuiteMixin);

      this.injectDependencies('$q',
                              '$rootScope',
                              '$httpBackend',
                              '$state',
                              'Study',
                              'StudyState',
                              'CollectionEventType',
                              'ProcessingType',
                              'AnnotationType',
                              'ProcessingTypeInputModal',
                              'ProcessingTypeOutputModal',
                              'StudyState',
                              'notificationsService',
                              'domainNotificationService',
                              'modalService',
                              'Factory');

      this.processingTypeFixture = new ProcessingTypeFixture(this.Factory,
                                                             this.Study,
                                                             this.CollectionEventType,
                                                             this.ProcessingType);

      this.$state.reload = jasmine.createSpy().and.returnValue(null);
      this.$state.go = jasmine.createSpy().and.returnValue(null);

      this.createController = (fixture, processingType) => {
        this.createControllerInternal(
          `<processing-type-view
              study="vm.study"
              processing-type="vm.processingType">
           </processing-type-view>`,
          {
            study:          fixture.study,
            processingType: processingType
          },
          'processingTypeView');
      };

      this.createControllerForCollected = (fixture, processingType, plainReply) => {
        const url = this.url('studies/cetypes/id',
                             fixture.study.id,
                             processingType.specimenProcessing.input.entityId);
        this.$httpBackend.expectGET(url).respond(this.reply(plainReply));
        this.createController(fixture, processingType);
        this.$httpBackend.flush();
      }

      this.createControllerForProcessed = (fixture, processingType, plainReply) => {
        const url = this.url('studies/proctypes/id',
                             fixture.study.id,
                             processingType.specimenProcessing.input.entityId);
        this.$httpBackend.expectGET(url).respond(this.reply(plainReply));
        this.createController(fixture, processingType);
        this.$httpBackend.flush();
      }
    });
  });

  afterEach(function() {
    this.$httpBackend.verifyNoOutstandingExpectation();
    this.$httpBackend.verifyNoOutstandingRequest();
  });

  describe('scope should be valid', function() {

    it('when processing type is from a collected specimen', function() {
      const f = this.processingTypeFixture.fixture();
      const processingType = f.processingTypesFromCollected[0].processingType;

      expect(processingType.specimenProcessing.input.entityId)
        .toBe(f.eventTypes[0].plainEventType.id);

      this.createControllerForCollected(f, processingType, f.eventTypes[0].plainEventType);
      expect(this.controller.processingType).toBe(processingType);
    });

    it('when processing type is from a processed specimen', function() {
      const f = this.processingTypeFixture.fixture();
      const processingType = f.processingTypesFromProcessed[0].processingType;

      expect(processingType.specimenProcessing.input.entityId)
        .toBe(f.processingTypesFromCollected[0].plainProcessingType.id);

      this.createControllerForProcessed(f,
                                        processingType,
                                        f.processingTypesFromCollected[0].plainProcessingType);
      expect(this.controller.processingType).toBe(processingType);
    });

  });

  describe('when modifying', function() {
    const context = {};

    beforeEach(function() {
      context.createController = () => {
        const f = this.processingTypeFixture.fixture();
        const processingType = f.processingTypesFromProcessed[0].processingType;
        this.createControllerForProcessed(f,
                                          processingType,
                                          f.processingTypesFromCollected[0].plainProcessingType);
      };

    });

    describe('updates to name', function () {

      beforeEach(function () {
        context.entity             = this.ProcessingType;
        context.updateFuncName     = 'updateName';
        context.controllerFuncName = 'editName';
        context.modalInputFuncName = 'text';
        context.newValue           = this.Factory.stringNext();
      });

      entityUpdateSharedBehaviour(context);

    });

    describe('updates to description', function () {

      beforeEach(function () {
        context.entity             = this.ProcessingType;
        context.updateFuncName     = 'updateDescription';
        context.controllerFuncName = 'editDescription';
        context.modalInputFuncName = 'textArea';
        context.newValue           = this.Factory.stringNext();
      });

      entityUpdateSharedBehaviour(context);

    });

    describe('updates to enabled', function () {

      beforeEach(function () {
        context.entity             = this.ProcessingType;
        context.updateFuncName     = 'updateEnabled';
        context.controllerFuncName = 'editEnabled';
        context.modalInputFuncName = 'boolean';
        context.newValue           = false;
      });

      entityUpdateSharedBehaviour(context);

    });

    it('calling addAnnotationType should change to the correct state', function() {
      context.createController();
      this.controller.addAnnotationType();
      this.scope.$digest();
      expect(this.$state.go)
        .toHaveBeenCalledWith('home.admin.studies.study.processing.viewType.annotationTypeAdd');
    });

    it('calling editAnnotationType should change to the correct state', function() {
      var annotType = new this.AnnotationType(this.Factory.annotationType());

      context.createController();
      this.controller.editAnnotationType(annotType);
      this.scope.$digest();
      expect(this.$state.go).toHaveBeenCalledWith(
        'home.admin.studies.study.processing.viewType.annotationTypeView',
        { annotationTypeSlug: annotType.slug });
    });

    describe('removing an annotation type', function() {

      it('can be removed when in valid state', function() {
        const f = this.processingTypeFixture.fixture();
        const processingType = f.processingTypesFromCollected[0].processingType;

        expect(processingType.specimenProcessing.input.entityId)
          .toBe(f.eventTypes[0].plainEventType.id);
        expect(processingType.annotationTypes).toBeNonEmptyArray();

        this.createControllerForCollected(f, processingType, f.eventTypes[0].plainEventType);

        this.modalService.modalOkCancel = jasmine.createSpy().and.returnValue(this.$q.when('OK'));
        const url = this.url('studies/proctypes/annottype',
                             f.study.id,
                             processingType.id,
                             processingType.version,
                             processingType.annotationTypes[0].id);

        this.$httpBackend.expectDELETE(url)
          .respond(this.reply(f.processingTypesFromCollected[0].plainProcessingType));

        this.controller.annotationTypeIdsInUse = [];
        this.controller.removeAnnotationType(processingType.annotationTypes[0]);
        this.scope.$digest();

        expect(this.modalService.modalOkCancel).toHaveBeenCalled();
        this.$httpBackend.flush();
      });

      it('throws an error if modifications are not allowed', function() {
        const f = this.processingTypeFixture.fixture();
        const processingType = f.processingTypesFromCollected[0].processingType;
        var annotationType = new this.AnnotationType(this.Factory.annotationType());

        this.modalService.modalOk = jasmine.createSpy().and.returnValue(this.$q.when('OK'));

        this.createControllerForCollected(f, processingType, f.eventTypes[0].plainEventType);
        this.controller.annotationTypeIdsInUse = [ annotationType.id ];
        this.controller.removeAnnotationType(annotationType);
        expect(this.modalService.modalOk).toHaveBeenCalled();
      });

      it('throws an error if study is not disabled', function() {
        const f = this.processingTypeFixture.fixture();
        const processingType = f.processingTypesFromCollected[0].processingType;
        var annotationType = new this.AnnotationType(this.Factory.annotationType());

        this.createControllerForCollected(f, processingType, f.eventTypes[0].plainEventType);
        this.controller.annotationTypeIdsInUse = [ ];

        [this.StudyState.ENABLED, this.StudyState.RETIRED].forEach(state => {
          f.study.state = state

          expect(() => {
            this.controller.removeAnnotationType(annotationType);
          }).toThrowError(/modifications not allowed/);
        });
      });

    });

    describe('for updates to the input specimen', function() {

      it('update request is sent to server', function() {
        const f = this.processingTypeFixture.fixture();
        const processingType = f.processingTypesFromCollected[0].processingType;
        const input = processingType.specimenProcessing.input;
        this.createControllerForCollected(f, processingType, f.eventTypes[0].plainEventType);

        this.ProcessingTypeInputModal.open = jasmine.createSpy()
          .and.returnValue({ result: this.$q.when(input) });

        const url = this.url('studies/proctypes/update', processingType.studyId, processingType.id);
        this.$httpBackend
          .expectPOST(url,
                      {
                        property: 'inputSpecimenProcessing',
                        expectedVersion: processingType.version,
                        newValue: input
                      })
          .respond(this.reply(f.processingTypesFromCollected[0].plainProcessingType));

        this.controller.inputSpecimenUpdate();
        this.$httpBackend.flush();
        expect(this.ProcessingTypeInputModal.open)
          .toHaveBeenCalledWith(f.study, processingType);
      });

      it('user can press cancel button on modal', function() {
        const f = this.processingTypeFixture.fixture();
        const processingType = f.processingTypesFromCollected[0].processingType;
        this.createControllerForCollected(f, processingType, f.eventTypes[0].plainEventType);

        this.ProcessingTypeInputModal.open = jasmine.createSpy()
          .and.returnValue({ result: this.$q.reject('cancel') });

        this.notificationsService.updateError = jasmine.createSpy().and.callThrough();
        this.controller.inputSpecimenUpdate();

        expect(this.notificationsService.updateError).not.toHaveBeenCalled();
      });

      it('displays a notification if the server replies with a failure', function() {
        const f = this.processingTypeFixture.fixture();
        const processingType = f.processingTypesFromCollected[0].processingType;
        const input = processingType.specimenProcessing.input;
        this.createControllerForCollected(f, processingType, f.eventTypes[0].plainEventType);

        this.ProcessingTypeInputModal.open = jasmine.createSpy()
          .and.returnValue({ result: this.$q.when(input) });

        const url = this.url('studies/proctypes/update', processingType.studyId, processingType.id);
        this.$httpBackend
          .expectPOST(url,
                      {
                        property: 'inputSpecimenProcessing',
                        expectedVersion: processingType.version,
                        newValue: input
                      })
          .respond(400, this.errorReply());
        this.notificationsService.updateError = jasmine.createSpy().and.callThrough();

        this.controller.inputSpecimenUpdate();
        this.$httpBackend.flush();

        expect(this.notificationsService.updateError).toHaveBeenCalled();
      });

    });

    describe('for updates to the output specimen', function() {

      it('update request is sent to server', function() {
        const f = this.processingTypeFixture.fixture();
        const processingType = f.processingTypesFromCollected[0].processingType;
        const output = processingType.specimenProcessing.output;
        this.createControllerForCollected(f, processingType, f.eventTypes[0].plainEventType);

        this.ProcessingTypeOutputModal.open = jasmine.createSpy()
          .and.returnValue({ result: this.$q.when(output) });

        const url = this.url('studies/proctypes/update', processingType.studyId, processingType.id);
        this.$httpBackend
          .expectPOST(url,
                      {
                        property: 'outputSpecimenProcessing',
                        expectedVersion: processingType.version,
                        newValue: output
                      })
          .respond(this.reply(f.processingTypesFromCollected[0].plainProcessingType));

        this.controller.outputSpecimenUpdate();
        this.$httpBackend.flush();
        expect(this.ProcessingTypeOutputModal.open)
          .toHaveBeenCalledWith(f.study, processingType);
      });

      it('user can press cancel button on modal', function() {
        const f = this.processingTypeFixture.fixture();
        const processingType = f.processingTypesFromCollected[0].processingType;
        this.createControllerForCollected(f, processingType, f.eventTypes[0].plainEventType);

        this.ProcessingTypeOutputModal.open = jasmine.createSpy()
          .and.returnValue({ result: this.$q.reject('cancel') });

        this.notificationsService.updateError = jasmine.createSpy().and.callThrough();
        this.controller.outputSpecimenUpdate();

        expect(this.notificationsService.updateError).not.toHaveBeenCalled();
      });

      it('displays a notification if the server replies with a failure', function() {
        const f = this.processingTypeFixture.fixture();
        const processingType = f.processingTypesFromCollected[0].processingType;
        const output = processingType.specimenProcessing.output;
        this.createControllerForCollected(f, processingType, f.eventTypes[0].plainEventType);

        this.ProcessingTypeOutputModal.open = jasmine.createSpy()
          .and.returnValue({ result: this.$q.when(output) });

        const url = this.url('studies/proctypes/update', processingType.studyId, processingType.id);
        this.$httpBackend
          .expectPOST(url,
                      {
                        property: 'outputSpecimenProcessing',
                        expectedVersion: processingType.version,
                        newValue: output
                      })
          .respond(400, this.errorReply());
        this.notificationsService.updateError = jasmine.createSpy().and.callThrough();

        this.controller.outputSpecimenUpdate();
        this.$httpBackend.flush();

        expect(this.notificationsService.updateError).toHaveBeenCalled();
      });

    });

  });

  describe('removing a processing type', function() {

    it('can remove correctly', function() {
      const f = this.processingTypeFixture.fixture();
      const processingType = f.processingTypesFromCollected[0].processingType;
      this.createControllerForCollected(f, processingType, f.eventTypes[0].plainEventType);

      this.$httpBackend
        .expectGET(this.url('studies/proctypes/inuse', processingType.slug))
        .respond(this.reply(false));

      this.$httpBackend
        .expectDELETE(this.url('studies/proctypes',
                               processingType.studyId,
                               processingType.id,
                               processingType.version))
        .respond(this.reply(true));

      spyOn(this.domainNotificationService, 'removeEntity').and.callThrough();
      spyOn(this.modalService, 'modalOkCancel').and.returnValue(this.$q.when('OK'));
      spyOn(this.notificationsService, 'success').and.returnValue(null);

      this.controller.removeProcessingType();
      this.$httpBackend.flush();

      expect(this.domainNotificationService.removeEntity).toHaveBeenCalled();
      expect(this.notificationsService.success).toHaveBeenCalled();
      expect(this.$state.go).toHaveBeenCalledWith('^', {}, { reload: true });
    });

    it('user is informed if it cannot be removed', function() {
      const f = this.processingTypeFixture.fixture();
      const processingType = f.processingTypesFromCollected[0].processingType;

      this.createControllerForCollected(f, processingType, f.eventTypes[0].plainEventType);

      this.$httpBackend
        .expectGET(this.url('studies/proctypes/inuse', processingType.slug))
        .respond(this.reply(true));

      spyOn(this.modalService, 'modalOk').and.returnValue(this.$q.when('OK'));

      this.controller.removeProcessingType();
      this.$httpBackend.flush();

      expect(this.modalService.modalOk).toHaveBeenCalled();
    });

  });

});
