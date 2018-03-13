/**
 * AngularJS component available to the rest of the application.
 *
 * @namespace common.components.progressTracker
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

import { LabelService } from '../../../base/services/LabelService';

class ProgressTrackerController {

  constructor(BbwebError) {
    'ngInject';
    Object.assign(this, { BbwebError });
    this.labelService = new LabelService(BbwebError);
  }

  $onInit() {
    if (this.taskData) {
      this.labelService.setLabels(this.taskData);
      this.tasks = this.taskData.map(taskItem => ({
        id:    taskItem.id,
        class: taskItem.status ? 'progtrckr-done' : 'progtrckr-todo'
      }));
    }
  }
}

/**
 * An AngularJS component that displays a progress tracker that gives visual feedback to the user when a
 * number of steps need to be completed in a certain order.
 *
 * @memberOf common.components.progressTracker
 *
 * @param {Array<common.components.progressTracker.TaskInfo>} taskData
 */
const progressTrackerComponent = {
  template: require('./progressTracker.html'),
  controller: ProgressTrackerController,
  controllerAs: 'vm',
  bindings: {
    taskData: '<'
  }
};

/**
 * Used to describe the tasks that are tracked by the {@link
 * common.components.progressTracker.progressTrackerComponent progressTrackerComponent}.
 *
 * @typedef common.components.progressTracker.TaskInfo
 *
 * @type object
 *
 * @property {string} id - the ID to associate with the task.
 *
 * @property {function} label - a function that returns the a text label for the task in the selected
 * language.
 *
 * @property {boolean} status - when set to `TRUE` the item is marked as done.
 */

export default ngModule => ngModule.component('progressTracker', progressTrackerComponent)
