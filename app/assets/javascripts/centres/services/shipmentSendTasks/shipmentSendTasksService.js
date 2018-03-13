/*
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

import { ShipmentTasksService } from '../shipmentTasks/shipmentTasksService';

/**
 * An AngularJS service that converts *sending* {@link domain.centres.Shipment Shipment} {@link
 * common.components.progressTracker.progressTrackerComponent progressTrackerComponent} task names to
 * *translated strings* that can be displayed to the user.
 *
 * @memberOf centres.services
 */
class ShipmentSendTasksService extends ShipmentTasksService {

  /**
   * @param {base.BbwebError} BbwebError - AngularJS factory for exceptions.
   *
   * @param {object} gettextCatalog - The service that allows strings to be translated to other languages.
   */
  constructor(BbwebError, gettextCatalog) {
    'ngInject';

    super(BbwebError,
          gettextCatalog,
          [
            { id: 1, label: () => gettextCatalog.getString('Shipping information') },
            { id: 2, label: () => gettextCatalog.getString('Items to ship') },
            { id: 3, label: () => gettextCatalog.getString('Packed') }
          ]);
  }

}

export default ngModule => ngModule.service('shipmentSendTasksService', ShipmentSendTasksService)
