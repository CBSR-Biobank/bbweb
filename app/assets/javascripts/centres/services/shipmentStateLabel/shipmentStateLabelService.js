/*
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

import { LabelService } from '../../../base/services/LabelService';

/**
 * An AngularJS service that converts a {@link domain.centres.ShipmentState ShipmentState} to a *translated
 * string* that can be displayed to the user.
 *
 * @memberOf centres.services
 */
class ShipmentStateLabelService extends LabelService {

  /**
   * @param {base.BbwebError} BbwebError - AngularJS factory for exceptions.
   *
   * @param {domain.centres.ShipmentState} ShipmentState - AngularJS constant that enumerates all the shipment
   * states.
   *
   * @param {AngularJS_Service} gettextCatalog - The service that allows strings to be translated to other
   * languages.
   *
   */
  constructor(BbwebError, ShipmentState, gettextCatalog) {
    'ngInject';

    super(BbwebError,
          [
            { id: ShipmentState.CREATED,   label: () => gettextCatalog.getString('Created') },
            { id: ShipmentState.PACKED,    label: () => gettextCatalog.getString('Packed') },
            { id: ShipmentState.SENT,      label: () => gettextCatalog.getString('Sent') },
            { id: ShipmentState.RECEIVED,  label: () => gettextCatalog.getString('Received') },
            { id: ShipmentState.UNPACKED,  label: () => gettextCatalog.getString('Unpacked') },
            { id: ShipmentState.COMPLETED, label: () => gettextCatalog.getString('Completed') },
            { id: ShipmentState.LOST,      label: () => gettextCatalog.getString('Lost') }
          ]);
    Object.assign(this, { ShipmentState, gettextCatalog });
  }

  /**
   * Returns the function that should be called to display the label for a {@link domain.centres.ShipmentState
   * ShipmentState}.
   *
   * @param {domain.centres.ShipmentState} state - the state to get a function for.
   *
   * @return {function} a function that returns a label that can be displayed to the user.
   */
  stateToLabelFunc(state) {
    return this.getLabel(state);
  }

}

export default ngModule => ngModule.service('shipmentStateLabelService', ShipmentStateLabelService)
