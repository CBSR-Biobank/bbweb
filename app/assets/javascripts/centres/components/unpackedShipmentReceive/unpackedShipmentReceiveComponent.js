/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(function (require) {
  'use strict';

  var _ = require('lodash');

  var component = {
    templateUrl: '/assets/javascripts/centres/components/unpackedShipmentReceive/unpackedShipmentReceive.html',
    controller: UnpackedShipmentReceiveController,
    controllerAs: 'vm',
    bindings: {
      shipment: '<'
    }
  };

  UnpackedShipmentReceiveController.$inject = [
    '$q',
    '$scope',
    'Shipment',
    'ShipmentSpecimen',
    'ShipmentItemState',
    'gettextCatalog',
    'modalService'
  ];

  /**
   * Allows user to interact with Shipment Specimens in PRESENT state.
   *
   * The user can receive the specimens, mark them as EXTRA or MISSING.
   */
  function UnpackedShipmentReceiveController($q,
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
        id:    'tag-as-extra',
        class: 'btn-warning',
        title: gettextCatalog.getString('Tag as extra'),
        icon:  'glyphicon-question-sign'
      }, {
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
      $scope.$emit('tabbed-page-update', 'shipment-receive-selected');
    }

    function getPresentSpecimens(options) {
      if (!vm.shipment) { return $q.when({ items: [], maxPages: 0 }); }

      _.extend(options, { filter: 'state:in:' + ShipmentItemState.PRESENT });

      return ShipmentSpecimen.list(vm.shipment.id, options)
        .then(function (paginatedResult) {
          return { items: paginatedResult.items, maxPages: paginatedResult.maxPages };
        });
    }

    /**
     * Inventory IDs entered by the user
     */
    function onInventoryIdsSubmit() {
      var inventoryIds = _.map(vm.inventoryIds.split(','), function (nonTrimmedInventoryId) {
        return nonTrimmedInventoryId.trim();
      });
      return vm.shipment.tagSpecimensAsReceived(inventoryIds)
        .then(function () {
          vm.inventoryIds = '';
          vm.refreshNonReceivedSpecimensTable++;
        })
        .catch(function (err) {
          var errors;
          if (err.data.message) {
            errors = err.data.message.split(', ');
            console.log(errors);
          }
        });
    }

    // function specimensNotPresent(errMsg) {
    //   return modalService.modalOk(
    //     gettextCatalog.getString(''),
    //     gettextCatalog.getString(
    //       'Unique ID <strong>{{id}}</strong> is already in use by a participant ' +
    //         'in another study. Please use a diffent one.',
    //       { id: vm.uniqueId }))
    //     .then(function () {
    //       vm.uniqueId = '';
    //     });
    // }

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
