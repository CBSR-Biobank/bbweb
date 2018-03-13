/*
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

import { LabelService } from '../../../base/services/LabelService';
import angular from 'angular'

/**
 * An AngularJS service base class used to convert {@link domain.centres.Shipment Shipment} {@link
 * common.components.progressTracker.progressTrackerComponent progressTrackerComponent} task names to
 * *translated strings* that can be displayed to the user.
 *
 * @memberOf centres.services
 */
class ShipmentTasksService extends LabelService {

  /**
   * @param {base.BbwebError} BbwebError - AngularJS factory for exceptions.
   *
   * @param {object} UserState - AngularJS constant that enumerates all the user states.
   *
   * @param {object} gettextCatalog - The service that allows strings to be translated to other languages.
   *
   * @param {Array<base.services.LabelService.LabelInfo>} labelData
   */
  constructor(BbwebError, gettextCatalog, labelData) {
    super(BbwebError, labelData);
  }

  getTaskData() {
    return Object.keys(this.labels).map(key => ({
      id:     key,
      label:  this.labels[key],
      status: false
    }));
  }

}

export { ShipmentTasksService }

// this controller does not need to be included in AngularJS since it is imported by the controllers that
// extend it
export default () => angular.noop
