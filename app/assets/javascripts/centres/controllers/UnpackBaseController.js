/*
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

import _ from 'lodash'

/**
 * Base controller for components that modify {@link domain.centres.Shipment Shipment} item states.
 *
 * @memberOf centres.controllers
 */
class UnpackBaseController {

  constructor(modalService, gettextCatalog) {
    Object.assign(this, { modalService, gettextCatalog });
  }

  /**
   * Used to tag {@link domain.participants.Specimen Specimens} in a {@link domain.centres.Shipment Shipment}
   * as {@link domain.centres.ShipmentItemState EXTRA}.
   *
   * @param {Array<string>} inventoryIds - the specimen inventory IDs to tag as EXTRA.
   */
  tagSpecimensAsExtra(inventoryIds) {

    return this.shipment.tagSpecimensAsExtra(inventoryIds)
      .then(() => {
        this.inventoryIds = '';
        this.refreshTable += 1;
      })
      .catch(err => {
        var modalMsg;

        if (err.message) {
          modalMsg = this.errorIsAlreadyInShipment(err.message);
          if (_.isUndefined(modalMsg)) {
            modalMsg = this.errorIsInAnotherShipment(err.message);
          }
          if (_.isUndefined(modalMsg)) {
            modalMsg = this.errorIsInvalidInventoryIds(err.message);
          }
          if (_.isUndefined(modalMsg)) {
            modalMsg = this.errorIsInvalidCentre(err.message);
          }
        }

        if (modalMsg) {
          this.modalService.modalOk(this.gettextCatalog.getString('Invalid inventory IDs'), modalMsg);
          return;
        }

        this.modalService.modalOk(this.gettextCatalog.getString('Server error'), JSON.stringify(err));
      });
  }

  /** @private */
  errorIsInvalidInventoryIds(errMsg) {
    var regex = /EntityCriteriaError: invalid inventory Ids: (.*)/g,
        match = regex.exec(errMsg);
    if (match) {
      return this.gettextCatalog.getString('The following inventory IDs are invalid:<br>{{ids}}',
                                           { ids: match[1] });
    }
    return undefined;
  }

  /** @private */
  errorIsShipSpecimensNotInShipment(errMsg) {
    var regex = /EntityCriteriaError: specimens not in this shipment: (.*)/g,
        match = regex.exec(errMsg);
    if (match) {
      return this.gettextCatalog.getString(
        'The following inventory IDs are for specimens not present in this shipment:<br>{{ids}}',
        { ids: match[1] });
    }
    return undefined;
  }

  /** @private */
  errorIsShipSpecimensNotPresent(errMsg) {
    var regex = /EntityCriteriaError: shipment specimens not present: (.*)/g,
        match = regex.exec(errMsg);
    if (match) {
      return this.gettextCatalog.getString(
        'The following inventory IDs are for have already been unpacked:<br>{{ids}}',
        { ids: match[1] });
    }
    return undefined;
  }

  /** @private */
  errorIsAlreadyInShipment(errMsg) {
    var regex = /EntityCriteriaError: specimen inventory IDs already in this shipment: (.*)/g,
        match = regex.exec(errMsg);
    if (match) {
      return this.gettextCatalog.getString(
        'The following inventory IDs are are already in this shipment:<br>{{ids}}',
        { ids: match[1] });
    }
    return undefined;
  }

  /** @private */
  errorIsInAnotherShipment(errMsg) {
    var regex = /EntityCriteriaError: specimens are already in an active shipment: (.*)/g,
        match = regex.exec(errMsg);
    if (match) {
      return this.gettextCatalog.getString(
        'The following inventory IDs are in another shipment:<br>{{ids}}' +
          '<p>Remove them from the other shipment first to mark them as extra in this shipment.',
        { ids: match[1] });
    }
    return undefined;
  }

  /** @private */
  errorIsInvalidCentre(errMsg) {
    var regex = /EntityCriteriaError: invalid centre for specimen inventory IDs: (.*)/g,
        match = regex.exec(errMsg);
    if (match) {
      return this.gettextCatalog.getString(
        'The following inventory IDs are not at the centre this shipment is coming from:<br>{{ids}}',
        { ids: match[1] });
    }
    return undefined;
  }

}

// this controller does not need to be included in AngularJS since it is imported by the controllers that
// extend it, see ShipmentSpecimensController for example.
export { UnpackBaseController }
export default () => {}
