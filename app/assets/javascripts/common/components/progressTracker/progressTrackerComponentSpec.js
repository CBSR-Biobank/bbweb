/*
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

/* global angular */

import _ from 'lodash';
import ngModule from '../../index'

describe('progressTrackerComponent', function() {

  beforeEach(() => {
    angular.mock.module(ngModule, 'biobank.test');
    angular.mock.inject(function(ComponentTestSuiteMixin) {
      _.extend(this, ComponentTestSuiteMixin);

      this.injectDependencies('$q', '$rootScope', '$compile', 'Factory');

      this.createController = (taskData) => {
        ComponentTestSuiteMixin.createController.call(
          this,
          '<progress-tracker task-data="vm.taskData"></progress-tracker>',
          { taskData },
          'progressTracker');
      };

      const labelFunc = () => this.Factory.stringNext();

      this.createTaskData = () => _.range(3).map(() => ({
        id:     this.Factory.stringNext(),
        label:  () => labelFunc,
        status: false
      }));
    });
  });

  it('has valid scope', function() {
    const tasks = this.createTaskData();

    this.createController(tasks);
    expect(this.controller.tasks.length).toBe(tasks.length);
    expect(this.controller.tasks).toBeArrayOfSize(tasks.length);
  });

  it('all steps can be marked as todo', function() {
    const tasks = this.createTaskData();
    this.createController(tasks, 0);
    expect(this.controller.tasks.length).toBe(tasks.length);
    this.controller.tasks.forEach((step) => {
      expect(step.class).toBe('progtrckr-todo');
    });
  });

  it('all steps can be marked as done', function() {
    const tasks = this.createTaskData();
    tasks.forEach(task => {
      task.status = true;
    });

    this.createController(tasks, tasks[tasks.length - 1].id);
    expect(this.controller.tasks.length).toBe(tasks.length);
    this.controller.tasks.forEach(task => {
      expect(task.class).toBe('progtrckr-done');
    });
  });

});
