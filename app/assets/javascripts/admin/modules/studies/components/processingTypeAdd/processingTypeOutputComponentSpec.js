/**
 * Jasmine test suite
 *
 */
/* global angular */

import { ComponentTestSuiteMixin } from 'test/mixins/ComponentTestSuiteMixin';
import ProcessingTypeFixture from 'test/fixtures/ProcessingTypeFixture';
import ngModule from '../../../../../app';

describe('processingTypeOutputComponent', function() {

  beforeEach(() => {
    angular.mock.module(ngModule, 'biobank.test');
    angular.mock.inject(function() {
      Object.assign(this, ComponentTestSuiteMixin);

      this.injectDependencies('$q',
                              '$httpBackend',
                              'Study',
                              'ProcessingType',
                              'CollectionEventType',
                              'ProcessingTypeAdd',
                              'ProcessingTypeAddTasks',
                              'notificationsService',
                              'domainNotificationService',
                              'modalService',
                              'Factory');

      this.processingTypeFixture = new ProcessingTypeFixture(this.Factory,
                                                             this.Study,
                                                             this.CollectionEventType,
                                                             this.ProcessingType);

      this.init();
      this.createController = () => {
        this.createControllerInternal(
          '<processing-type-output><processing-type-output>',
          {},
          'processingTypeOutput');
      };

      this.stateInit = (plainStudy) => {
        this.$httpBackend.expectGET(this.url('studies', plainStudy.slug))
          .respond(this.reply(plainStudy));

        this.gotoUrl(`/admin/studies/${plainStudy.slug}/processing/add/output`);
        this.$httpBackend.flush();
        expect(this.$state.current.name).toBe('home.admin.studies.study.processing.addType.output');
      };
    });
  });

  it('has valid initialization', function() {
    this.ProcessingTypeAdd.init();
    this.ProcessingTypeAdd.initIfRequired = jasmine.createSpy().and.callThrough();
    this.ProcessingTypeAdd.isValid = jasmine.createSpy().and.returnValue(true);
    this.createController();

    expect(this.ProcessingTypeAdd.initIfRequired).toHaveBeenCalled();

    expect(this.controller.progressInfo).toBeDefined();
    const taskData = this.ProcessingTypeAddTasks.getTaskData();

    expect(this.controller.progressInfo).toBeDefined();
    expect(this.controller.progressInfo).toBeArrayOfSize(Object.keys(taskData).length);
    taskData.forEach((taskInfo, index) => {
      taskInfo.status = (index < 3);
      expect(this.controller.progressInfo).toContain(taskInfo);
    });
  });

  it('returns to first sibling state if service is not initialized', function() {
    const f = this.processingTypeFixture.fixture();
    this.stateInit(f.plainStudy);
    this.ProcessingTypeAdd.isValid = jasmine.createSpy().and.returnValue(false);
    this.createController();
    this.$rootScope.$digest();
    expect(this.$state.current.name).toBe('home.admin.studies.study.processing.addType.information');
  });

  describe('for state transitions', function() {

    beforeEach(function() {
      this.fixture = this.processingTypeFixture.fixture();
      this.stateInit(this.fixture.plainStudy);
      this.ProcessingTypeAdd.processingType = this.fixture.processingTypesFromProcessed[0].processingType;
      this.createController(this.fixture.study);
    });

    it('when `previous` is called', function() {
      this.controller.previous();
      this.$rootScope.$digest();
      expect(this.$state.current.name).toBe('home.admin.studies.study.processing.addType.input');
    });

    it('when `cancel` is called', function() {
      this.controller.cancel();
      this.$rootScope.$digest();
      expect(this.$state.current.name).toBe('home.admin.studies.study.processing');
    });

  });

  describe('for `submit`', function() {

    beforeEach(function() {
      const fixture = this.processingTypeFixture.fixture();
      const processingType = fixture.processingTypesFromProcessed[0].processingType;

      this.plainProcessingType = fixture.processingTypesFromProcessed[0].plainProcessingType;
      this.ProcessingTypeAdd.processingType = processingType;
      this.stateInit(fixture.plainStudy);
      this.createController();

      this.requestHandler = this.$httpBackend
        .expectPOST(this.url('studies/proctypes', processingType.studyId),
                    {
                      name:               processingType.name,
                      description:        processingType.description,
                      enabled:            processingType.enabled,
                      specimenProcessing: processingType.specimenProcessing
                    });
    });

    it('makes a POST request to the server', function() {
      this.requestHandler.respond(this.reply(this.plainProcessingType));
      spyOn(this.notificationsService, 'submitSuccess').and.returnValue(null);
      this.controller.submit();
      this.$httpBackend.flush();
      expect(this.notificationsService.submitSuccess).toHaveBeenCalled();
      expect(this.$state.current.name).toBe('home.admin.studies.study.processing');
    });

    it('duplicate name error reply from server is handled', function() {
      this.requestHandler.respond(400, this.errorReply('name already exists'));
      spyOn(this.modalService, 'modalOk').and.returnValue(null);
      this.controller.submit();
      this.$httpBackend.flush();
      expect(this.modalService.modalOk).toHaveBeenCalled();
      expect(this.$state.current.name).toBe('home.admin.studies.study.processing.addType.output');
    });

    it('error other than duplicate name reply from server is handled', function() {
      this.requestHandler.respond(400, this.errorReply('simulated error'));
      spyOn(this.domainNotificationService, 'updateErrorModal').and.returnValue(null);
      this.controller.submit();
      this.$httpBackend.flush();
      expect(this.domainNotificationService.updateErrorModal).toHaveBeenCalled();
      expect(this.$state.current.name).toBe('home.admin.studies.study.processing.addType.output');
    });

  });

});
