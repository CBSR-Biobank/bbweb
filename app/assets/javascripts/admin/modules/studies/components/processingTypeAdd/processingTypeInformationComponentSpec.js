/**
 * Jasmine test suite
 *
 */
/* global angular */

import { ComponentTestSuiteMixin } from 'test/mixins/ComponentTestSuiteMixin';
import ngModule from '../../../../../app';

describe('processingTypeInformationComponent', function() {

  beforeEach(() => {
    angular.mock.module(ngModule, 'biobank.test');
    angular.mock.inject(function() {
      Object.assign(this, ComponentTestSuiteMixin);

      this.injectDependencies('$q',
                              '$httpBackend',
                              'Study',
                              'ProcessingType',
                              'ProcessingTypeAdd',
                              'ProcessingTypeAddTasks',
                              'Factory');
      this.init();
      this.createController = (study, initialize) => {
        this.createControllerInternal(
          `<processing-type-information
              study="vm.study"
              initialize="vm.initialize">
           <processing-type-information>`,
          {
            study,
            initialize
          },
          'processingTypeInformation');
      };
    });
  });

  it('has valid scope', function() {
    const study = this.Study.create(this.Factory.study());
    this.createController(study, true);
    expect(this.controller.progressInfo).toBeDefined();

    const taskData = this.ProcessingTypeAddTasks.getTaskData();

    expect(this.controller.progressInfo).toBeDefined();
    expect(this.controller.progressInfo).toBeArrayOfSize(Object.keys(taskData).length);
    taskData.forEach((taskInfo, index) => {
      taskInfo.status = (index < 1);
      expect(this.controller.progressInfo).toContain(taskInfo);
    });
  });

  describe('for `initialize` parameter', function() {

    it('initializes the service`s processing type', function() {
      const study = this.Study.create(this.Factory.study());
      this.ProcessingTypeAdd.init = jasmine.createSpy().and.callThrough();
      this.createController(study, true);
      expect(this.ProcessingTypeAdd.init).toHaveBeenCalled();
    });

    it('initializes the service`s processing type when parameter not specified', function() {
      const study = this.Study.create(this.Factory.study());
      this.ProcessingTypeAdd.init = jasmine.createSpy().and.callThrough();
      this.createController(study, undefined);
      expect(this.ProcessingTypeAdd.init).toHaveBeenCalled();
    });

    it('does not initialize the service`s processing type', function() {
      const study = this.Study.create(this.Factory.study());
      this.ProcessingTypeAdd.processingType = this.ProcessingType.create(this.Factory.processingType());
      this.ProcessingTypeAdd.initIfRequired = jasmine.createSpy().and.callThrough();
      this.createController(study, false);
      expect(this.ProcessingTypeAdd.initIfRequired).toHaveBeenCalled();
    });

  });

  it('calling `next` assigns processing type information and changes state correctly', function() {
    const study = this.Study.create(this.Factory.study());
    const processingType = this.ProcessingType.create(this.Factory.processingType());
    this.createController(study, true);

    this.$state.go = jasmine.createSpy().and.returnValue(null);
    this.controller.name        = processingType.name;
    this.controller.description = processingType.description;
    this.controller.enabled     = processingType.enabled;

    this.controller.next();

    expect(this.$state.go).toHaveBeenCalledWith('^.input');
    expect(this.ProcessingTypeAdd.processingType.name).toBe(processingType.name);
    expect(this.ProcessingTypeAdd.processingType.description).toBe(processingType.description);
    expect(this.ProcessingTypeAdd.processingType.enabled).toBe(processingType.enabled);
  });

  it('calling `cancel` changes state correctly', function() {
    const plainStudy = this.Factory.study();
    const study = this.Study.create(this.Factory.study());

    this.$httpBackend.expectGET(this.url('studies', study.slug))
      .respond(this.reply(plainStudy));

    this.gotoUrl(`/admin/studies/${study.slug}/processing/add/information`);
    this.$httpBackend.flush();
    expect(this.$state.current.name).toBe('home.admin.studies.study.processing.addType.information');

    this.$state.go = jasmine.createSpy().and.returnValue(null);
    this.createController(study, true);

    this.controller.cancel();
    expect(this.$state.go).toHaveBeenCalledWith('^.^');
  });

})
