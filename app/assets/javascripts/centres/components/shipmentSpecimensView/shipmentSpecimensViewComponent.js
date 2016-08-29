/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(function (require) {
  'use strict';

  var _ = require('lodash');

  var component = {
    templateUrl : '/assets/javascripts/centres/components/shipmentSpecimensView/shipmentSpecimensView.html',
    controller: ShipmentSpecimensViewController,
    controllerAs: 'vm',
    bindings: {
      shipment: '<'
    }
  };

  ShipmentSpecimensViewController.$inject = [
    '$log',
    'gettextCatalog',
    'modalService',
    'modalInput',
    'domainNotificationService',
    'notificationsService',
    'Specimen',
    'Shipment',
    'ShipmentSpecimen'
  ];

  /**
   *
   */
  function ShipmentSpecimensViewController($log,
                                           gettextCatalog,
                                           modalService,
                                           modalInput,
                                           domainNotificationService,
                                           notificationsService,
                                           Specimen,
                                           Shipment,
                                           ShipmentSpecimen) {
    var vm = this;

    vm.shipmentSpecimens = [];
    vm.panelOpen = true;

    vm.panelButtonClicked     = panelButtonClicked;
    vm.addSpecimen            = addSpecimen;
    vm.getTableData           = getTableData;
    vm.removeShipmentSpecimen = removeShipmentSpecimen;

    //---

    function panelButtonClicked() {
      vm.panelOpen = !vm.panelOpen;
    }

    function addSpecimen() {
      modalInput.text(gettextCatalog.getString('Add specimen to ship'),
                      gettextCatalog.getString('Inventory ID'),
                      vm.inventoryId,
                      { required: true, minLength: 2 }).result
        .then(function (inventoryId) {

          var exists = _.find(vm.shipmentSpecimens, function (ss) {
            return ss.specimen.inventoryId === inventoryId;
          });

          if (!exists) {
            return Specimen.getByInventoryId(inventoryId)
              .then(function (specimen) {
                if (specimen.locationInfo.locationId !== vm.shipment.fromLocationInfo.locationId) {
                  return modalService.modalOk(
                    gettextCatalog.getString('Specimen location error'),
                    gettextCatalog.getString(
                      'Specimen with inventory ID <b>{{id}}</b> is not present at the centre ' +
                        'this shipment is comes from.',
                      { id: inventoryId }));
                }

                return ShipmentSpecimen.add(vm.shipment.id, specimen.id)
                  .then(reloadTableData)
                  .catch(function (error) {
                    var message;

                    if (error.data && error.data.message.match(/inventory ID not found/)) {
                      message = gettextCatalog.getString(
                        'Specimen with inventory ID <b>{{id}}</b> is not present in the system.',
                        { id: inventoryId });
                    } else if (error.data &&
                               error.data.message.match(/specimen is already in active shipment/)) {
                      message = gettextCatalog.getString(
                        'Specimen with inventory ID <b>{{id}}</b> is already in another shipment.',
                        { id: inventoryId });
                    } else {
                      message = error.data.message;
                    }

                    modalService.modalOk(gettextCatalog.getString('Specimen error'), message);
                  });
              });
          }

          modalService.modalOk(gettextCatalog.getString('Specimen already in shipment'),
                               gettextCatalog.getString(
                                 'Specimen with inventory ID <b>{{id}}</b> ' +
                                   'has already been added to this shipment.',
                                 { id: inventoryId }));

          return false;
        });
    }

    function getTableData(tableState, controller) {
      if (!vm.shipment) { return; }

      var pagination    = tableState.pagination,
          sortPredicate = tableState.sort.predicate || 'inventoryId',
          sortOrder     = tableState.sort.reverse || false,
          options = {
            sort:     sortPredicate,
            page:     1 + (pagination.start / vm.pageSize),
            pageSize: vm.pageSize,
            order:    sortOrder ? 'desc' : 'asc'
          };

      if (!vm.tableController && controller) {
        vm.tableController = controller;
      }

      vm.tableDataLoading = true;

      ShipmentSpecimen.list(vm.shipment.id, options).then(function (paginatedResult) {
        vm.shipmentSpecimens = paginatedResult.items;
        tableState.pagination.numberOfPages = paginatedResult.maxPages;
        vm.tableDataLoading = false;
      });
    }

    function reloadTableData() {
      getTableData(vm.tableController.tableState());
    }

    function removeShipmentSpecimen(shipmentSpecimen) {
      domainNotificationService.removeEntity(
        promiseFn,
        gettextCatalog.getString('Remove specimen'),
        gettextCatalog.getString(
          'Are you sure you want to remove specimen with inventory ID <strong>{{id}}</strong> ' +
            'from this shipment?',
          { id: shipmentSpecimen.specimen.inventoryId }),
        gettextCatalog.getString('Remove failed'),
        gettextCatalog.getString(
          'Specimen with ID {{id}} cannot be removed',
          { id: shipmentSpecimen.specimen.inventoryId }));

      function promiseFn() {
        return shipmentSpecimen.remove().then(function () {
          notificationsService.success(gettextCatalog.getString('Specimen removed'));
          reloadTableData();
        });
      }
    }

  }

  return component;
});
