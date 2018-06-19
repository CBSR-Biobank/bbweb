/**
 * Jasmine test suite
 *
 */
/* global angular */

import { ComponentTestSuiteMixin } from 'test/mixins/ComponentTestSuiteMixin';
import ngModule from '../../../../../app';

describe('processingTypeInputComponent', function() {

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


})
