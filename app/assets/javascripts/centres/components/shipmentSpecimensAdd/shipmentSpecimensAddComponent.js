/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(function (require) {
  'use strict';

  var _ = require('lodash');

  var component = {
    templateUrl : '/assets/javascripts/centres/components/shipmentSpecimensAdd/shipmentSpecimensAdd.html',
    controller: ShipmentSpecimensAddController,
    controllerAs: 'vm',
    bindings: {
      shipment: '<',
      readOnly: '<'
    }
  };

  ShipmentSpecimensAddController.$inject = [
    '$q',
    '$controller',
    '$log',
    'gettextCatalog',
    'modalService',
    'modalInput',
    'domainNotificationService',
    'notificationsService',
    'Specimen',
    'ShipmentSpecimen'
  ];

  /**
   * This controller subclasses {@link ShipmentSpecimenController}.
   */
  function ShipmentSpecimensAddController($q,
                                          $controller,
                                          $log,
                                          gettextCatalog,
                                          modalService,
                                          modalInput,
                                          domainNotificationService,
                                          notificationsService,
                                          Specimen,
                                          ShipmentSpecimen) {
    var vm = this;

    // initialize this controller's base class
    $controller('ShipmentSpecimenController',
                {
                  vm:               vm,
                  $q:               $q,
                  ShipmentSpecimen: ShipmentSpecimen
                });

    vm.inventoryIds           = [];
    vm.refreshSpecimensTable  = 0;
    vm.addSpecimens           = addSpecimens;
    vm.removeShipmentSpecimen = removeShipmentSpecimen;

    vm.actions =  [{
      id:    'remove',
      class: 'btn-warning',
      title: gettextCatalog.getString('Remove specimen'),
      icon:  'glyphicon-remove'
    }];

    //---

    function addSpecimens() {
      var inventoryIdsArr;

      if (!_.isString(vm.inventoryIds)) {
        // nothing to do
        return;
      }

      inventoryIdsArr = vm.inventoryIds.split(',');
      vm.shipment.addSpecimens(inventoryIdsArr)
        .then(specimenAddConfirm)
        .catch(function (error) {
          var header = gettextCatalog.getString('Specimens cannot be added to shipment'),
              body,
              inventoryIds;

          if (error.data && error.data.message) {
            inventoryIds = error.data.message.split(': ');

            if (error.data.message.match(/invalid specimen inventory IDs/)) {
              body = gettextCatalog.getString(
                'Inventory IDs not present in the system:<br><b>{{ids}}</b>',
                { ids: inventoryIds[2] });
            } else if (error.data.message.match(/specimens are already in an active shipment/)) {
              body = gettextCatalog.getString(
                'Inventory IDs already in this shipment or another shipment:<br><b>{{ids}}</b>',
                { ids: inventoryIds[2] });
            } else if (error.data.message.match(/invalid centre for specimen inventory IDs/)) {
              body = gettextCatalog.getString(
                'Inventory IDs at a different location than where shipment is coming from:<br><b>{{ids}}</b> ',
                { ids: inventoryIds[2] });
            } else {
              body = error.data.message;
            }
          } else {
            body = JSON.stringify(error);
          }

          modalService.modalOk(header, body);
        });
    }

    function specimenAddConfirm() {
      notificationsService.success(gettextCatalog.getString('Specimens added'));
      refreshSpecimensTable();
      vm.inventoryIds = '';
    }

    function refreshSpecimensTable() {
      vm.refreshSpecimensTable += 1;
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
          refreshSpecimensTable();
        });
      }
    }

  }

  return component;
});
