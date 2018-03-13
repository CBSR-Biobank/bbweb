/*
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

import { ShipmentTasksService } from '../shipmentTasks/shipmentTasksService';

/**
 * An AngularJS service that converts *receiving* {@link domain.centres.Shipment Shipment} {@link
 * common.components.progressTracker.progressTrackerComponent progressTrackerComponent} task names to
 * *translated strings* that can be displayed to the user.
 *
 * @memberOf centres.services
 */
class ShipmentReceiveTasksService extends ShipmentTasksService {

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
            { id: 1, label: () => gettextCatalog.getString('Sent') },
            { id: 2, label: () => gettextCatalog.getString('Received') },
            { id: 3, label: () => gettextCatalog.getString('Unpacked') },
            { id: 4, label: () => gettextCatalog.getString('Completed') }
          ]);
  }

}

export default ngModule => ngModule.service('shipmentReceiveTasksService', ShipmentReceiveTasksService)
