/*
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

import { LabelService } from '../../../../base/services/LabelService.js'

/**
 * An AngularJs service that returns
 *
 * @memberOf
 */
class ProcessingTypeAddTasksService extends LabelService {

  constructor(BbwebError, gettextCatalog) {
    'ngInject';
    super(BbwebError,
          [
            { id: 1, label: () => gettextCatalog.getString('Information') },
            { id: 2, label: () => gettextCatalog.getString('Input specimen') },
            { id: 3, label: () => gettextCatalog.getString('Output specimen') }
          ]);
  }

  getTaskData() {
    return Object.keys(this.labels).map(key => ({
      id:     key,
      label:  this.labels[key],
      status: false
    }));
  }
}

export default ngModule => ngModule.service('ProcessingTypeAddTasks', ProcessingTypeAddTasksService)
