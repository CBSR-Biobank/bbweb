/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(function (require) {
  'use strict';

  var _ = require('lodash');

  var component = {
    templateUrl: '/assets/javascripts/centres/components/unpackedShipmentUnpack/unpackedShipmentUnpack.html',
    controller: UnpackedShipmentUnpackController,
    controllerAs: 'vm',
    bindings: {
      shipment: '<'
    }
  };

  UnpackedShipmentUnpackController.$inject = [
    '$q',
    '$scope',
    'Shipment',
    'ShipmentSpecimen',
    'ShipmentItemState',
    'gettextCatalog',
    'modalService'
  ];

  /*
   * Allows user to interact with Shipment Specimens in PRESENT state.
   *
   * The user can receive the specimens, mark them as EXTRA or MISSING.
   */
  function UnpackedShipmentUnpackController($q,
                                            $scope,
                                            Shipment,
                                            ShipmentSpecimen,
                                            ShipmentItemState,
                                            gettextCatalog,
                                            modalService) {
    var vm = this;

    vm.$onInit = onInit;
    vm.refreshNonReceivedSpecimensTable = 0;

    vm.actions =  [
      {
        id:    'tag-as-missing',
        class: 'btn-warning',
        title: gettextCatalog.getString('Tag as missing'),
        icon:  'glyphicon-cloud'
      }
    ];

    vm.getPresentSpecimens  = getPresentSpecimens;
    vm.onInventoryIdsSubmit = onInventoryIdsSubmit;
    vm.tableActionSelected  = tableActionSelected;

    //----

    function onInit() {
      $scope.$emit('tabbed-page-update', 'tab-selected');
    }

    function getPresentSpecimens(options) {
      if (!vm.shipment) { return $q.when({ items: [], maxPages: 0 }); }

      _.extend(options, { filter: 'state:in:' + ShipmentItemState.PRESENT });

      return ShipmentSpecimen.list(vm.shipment.id, options)
        .then(function (paginatedResult) {
          return { items: paginatedResult.items, maxPages: paginatedResult.maxPages };
        });
    }

    /*
     * Inventory IDs entered by the user
     */
    function onInventoryIdsSubmit() {
      var inventoryIds = _.map(vm.inventoryIds.split(','), function (nonTrimmedInventoryId) {
        return nonTrimmedInventoryId.trim();
      });
      return vm.shipment.tagSpecimensAsReceived(inventoryIds)
        .then(function () {
          vm.inventoryIds = '';
          vm.refreshNonReceivedSpecimensTable += 1;
        })
        .catch(function (err) {
          var modalMsg;

          if (err.data.message) {
            modalMsg = errorIsInvalidInventoryIds(err.data.message);
            if (_.isUndefined(modalMsg)) {
              modalMsg = errorIsShipSpecimensNotInShipment(err.data.message);
            }
            if (_.isUndefined(modalMsg)) {
              modalMsg = errorIsShipSpecimensNotPresent(err.data.message);
            }
          }

          if (_.isUndefined(modalMsg)) {
            throw new Error('could not parse error message');
          }

          modalService.modalOk(gettextCatalog.getString('Invalid inventory IDs'), modalMsg);
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

    function tableActionSelected(shipmentSpecimen, action) {
      var tagFunction;
      switch (action) {
      case 'tag-as-extra':
        tagFunction = vm.shipment.tagSpecimensAsExtra;
        break;
      case 'tag-as-missing':
        tagFunction = vm.shipment.tagSpecimensAsMissing;
        break;
      default:
        throw new Error('invalid action from table selection:' + action);
      }

      return tagFunction.call(vm.shipment, [ shipmentSpecimen.specimen.inventoryId ])
        .then(function () {
          vm.refreshNonReceivedSpecimensTable++;
        });
    }

  }

  return component;
});
