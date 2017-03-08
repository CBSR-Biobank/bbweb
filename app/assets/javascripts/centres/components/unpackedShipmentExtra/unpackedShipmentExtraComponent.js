/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(function (require) {
  'use strict';

  var _ = require('lodash');

  var component = {
    templateUrl: '/assets/javascripts/centres/components/unpackedShipmentExtra/unpackedShipmentExtra.html',
    controller: UnpackedShipmentExtraController,
    controllerAs: 'vm',
    bindings: {
      shipment: '<'
    }
  };

  UnpackedShipmentExtraController.$inject = [
    '$q',
    '$scope',
    'ShipmentSpecimen',
    'ShipmentItemState',
    'modalService',
    'domainNotificationService',
    'notificationsService',
    'gettextCatalog'
  ];

  /*
   * Controller for this component.
   */
  function UnpackedShipmentExtraController($q,
                                           $scope,
                                           ShipmentSpecimen,
                                           ShipmentItemState,
                                           modalService,
                                           domainNotificationService,
                                           notificationsService,
                                           gettextCatalog) {
    var vm = this;

    vm.$onInit = onInit;
    vm.refreshTable = 0;

    vm.actions =  [
      {
        id:    'tag-as-extra',
        class: 'btn-warning',
        title: gettextCatalog.getString('Remove'),
        icon:  'glyphicon-remove'
      }
    ];

    vm.getExtraSpecimens    = getExtraSpecimens;
    vm.onInventoryIdsSubmit = onInventoryIdsSubmit;
    vm.tableActionSelected  = tableActionSelected;

    //----

    function onInit() {
      $scope.$emit('tabbed-page-update', 'tab-selected');
    }

    function getExtraSpecimens(options) {
      if (!vm.shipment) { return $q.when({ items: [], maxPages: 0 }); }

      options = options || {};
      _.extend(options, { filter: 'state:in:' + ShipmentItemState.EXTRA });

      return ShipmentSpecimen.list(vm.shipment.id, options)
        .then(function (paginatedResult) {
          return { items: paginatedResult.items, maxPages: paginatedResult.maxPages };
        });
    }

    /*
     * User entered inventory IDs entered to be marked as extra in this shipment.
     */
    function onInventoryIdsSubmit() {
      var inventoryIds = _.map(vm.inventoryIds.split(','), function (nonTrimmedInventoryId) {
        return nonTrimmedInventoryId.trim();
      });
      return vm.shipment.tagSpecimensAsExtra(inventoryIds)
        .then(function () {
          vm.inventoryIds = '';
          vm.refreshTable += 1;
        })
        .catch(function (err) {
          var modalMsg;

          if (err.message) {
            modalMsg = errorIsAlreadyInShipment(err.message);
            if (_.isUndefined(modalMsg)) {
              modalMsg = errorIsInAnotherShipment(err.message);
            }
            if (_.isUndefined(modalMsg)) {
              modalMsg = errorIsInvalidInventoryId(err.message);
            }
            if (_.isUndefined(modalMsg)) {
              modalMsg = errorIsInvalidCentre(err.message);
            }
          }

          if (modalMsg) {
            modalService.modalOk(gettextCatalog.getString('Invalid inventory IDs'), modalMsg);
            return;
          }

          modalService.modalOk(gettextCatalog.getString('Server error'), JSON.stringify(err));
        });
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

    function errorIsInvalidInventoryId(errMsg) {
      var regex = /EntityCriteriaError: invalid inventory Ids: (.*)/g,
          match = regex.exec(errMsg);
      if (match) {
        return gettextCatalog.getString(
          'The following inventory IDs are invalid:<br>{{ids}}',
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

    /*
     * User wishes to remove this shipment specimen from this shipment.
     */
    function tableActionSelected(shipmentSpecimen) {
      domainNotificationService.removeEntity(
        promiseFn,
        gettextCatalog.getString('Remove extra specimen'),
        gettextCatalog.getString(
          'Are you sure you want to remove specimen with inventory ID <strong>{{id}}</strong> ' +
            'as an <i>Extra</i> specimen from this shipment?',
          { id: shipmentSpecimen.specimen.inventoryId }),
        gettextCatalog.getString('Remove failed'),
        gettextCatalog.getString(
          'Specimen with ID {{id}} cannot be removed',
          { id: shipmentSpecimen.specimen.inventoryId }));

      function promiseFn() {
        return shipmentSpecimen.remove().then(function () {
          notificationsService.success(gettextCatalog.getString('Specimen removed'));
          vm.refreshTable += 1;
        });
      }
    }

  }

  return component;
});
