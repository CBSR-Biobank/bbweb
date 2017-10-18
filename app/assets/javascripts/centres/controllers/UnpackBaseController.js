/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2017 Canadian BioSample Repository (CBSR)
 */

import _ from 'lodash'

/*
 * Common code to tag specimens as extra.
 */
/* @ngInject */
function UnpackBaseController(vm, modalService, gettextCatalog) {

  Object.assign(vm, {
    tagSpecimensAsExtra,
    errorIsInvalidInventoryIds,
    errorIsShipSpecimensNotInShipment,
    errorIsShipSpecimensNotPresent,
    errorIsAlreadyInShipment,
    errorIsInAnotherShipment,
    errorIsInvalidCentre
  })

  //--

  function tagSpecimensAsExtra(inventoryIds) {

    return vm.shipment.tagSpecimensAsExtra(inventoryIds)
      .then(function () {
        vm.inventoryIds = '';
        vm.refreshTable += 1;
      })
      .catch(function (err) {
        var modalMsg;

        if (err.message) {
          modalMsg = vm.errorIsAlreadyInShipment(err.message);
          if (_.isUndefined(modalMsg)) {
            modalMsg = errorIsInAnotherShipment(err.message);
          }
          if (_.isUndefined(modalMsg)) {
            modalMsg = vm.errorIsInvalidInventoryIds(err.message);
          }
          if (_.isUndefined(modalMsg)) {
            modalMsg = vm.errorIsInvalidCentre(err.message);
          }
        }

        if (modalMsg) {
          modalService.modalOk(gettextCatalog.getString('Invalid inventory IDs'), modalMsg);
          return;
        }

        modalService.modalOk(gettextCatalog.getString('Server error'), JSON.stringify(err));
      });
  }

  function errorIsInvalidInventoryIds(errMsg) {
    var regex = /EntityCriteriaError: invalid inventory Ids: (.*)/g,
        match = regex.exec(errMsg);
    if (match) {
      return gettextCatalog.getString('The following inventory IDs are invalid:<br>{{ids}}',
                                      { ids: match[1] });
    }
    return undefined;
  }

  function errorIsShipSpecimensNotInShipment(errMsg) {
    var regex = /EntityCriteriaError: specimens not in this shipment: (.*)/g,
        match = regex.exec(errMsg);
    if (match) {
      return gettextCatalog.getString(
        'The following inventory IDs are for specimens not present in this shipment:<br>{{ids}}',
        { ids: match[1] });
    }
    return undefined;
  }

  function errorIsShipSpecimensNotPresent(errMsg) {
    var regex = /EntityCriteriaError: shipment specimens not present: (.*)/g,
        match = regex.exec(errMsg);
    if (match) {
      return gettextCatalog.getString(
        'The following inventory IDs are for have already been unpacked:<br>{{ids}}',
        { ids: match[1] });
    }
    return undefined;
  }

  function errorIsAlreadyInShipment(errMsg) {
    var regex = /EntityCriteriaError: specimen inventory IDs already in this shipment: (.*)/g,
        match = regex.exec(errMsg);
    if (match) {
      return gettextCatalog.getString(
        'The following inventory IDs are are already in this shipment:<br>{{ids}}',
        { ids: match[1] });
    }
    return undefined;
  }

  function errorIsInAnotherShipment(errMsg) {
    var regex = /EntityCriteriaError: specimens are already in an active shipment: (.*)/g,
        match = regex.exec(errMsg);
    if (match) {
      return gettextCatalog.getString(
        'The following inventory IDs are in another shipment:<br>{{ids}}' +
          '<p>Remove them from the other shipment first to mark them as extra in this shipment.',
        { ids: match[1] });
    }
    return undefined;
  }

  function errorIsInvalidCentre(errMsg) {
    var regex = /EntityCriteriaError: invalid centre for specimen inventory IDs: (.*)/g,
        match = regex.exec(errMsg);
    if (match) {
      return gettextCatalog.getString(
        'The following inventory IDs are not at the centre this shipment is coming from:<br>{{ids}}',
        { ids: match[1] });
    }
    return undefined;
  }

}

export default ngModule => ngModule.controller('UnpackBaseController', UnpackBaseController)
