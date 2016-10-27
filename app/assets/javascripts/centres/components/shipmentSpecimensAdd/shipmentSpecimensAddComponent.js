/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(function (require) {
  'use strict';

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
   *
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
    $controller('ShipmentSpecimenController',
                {
                  vm:               vm,
                  $q:               $q,
                  ShipmentSpecimen: ShipmentSpecimen
                });

    vm.refreshSpecimensTable  = 0;
    vm.addSpecimen            = addSpecimen;
    vm.removeShipmentSpecimen = removeShipmentSpecimen;

    vm.actions =  [{
      id:    'remove',
      class: 'btn-warning',
      title: gettextCatalog.getString('Remove specimen'),
      icon:  'glyphicon-remove'
    }];

    //---

    function addSpecimen() {
      modalInput.text(gettextCatalog.getString('Add specimen to ship'),
                      gettextCatalog.getString('Inventory ID'),
                      vm.inventoryId,
                      { required: true, minLength: 2 }).result
        .then(function (inventoryId) {
          return vm.shipment.canAddInventoryId(inventoryId)
            .then(function (specimen) {
              ShipmentSpecimen.add(vm.shipment.id, specimen.id)
                .then(specimenAddConfirm)
                .catch(function (error) {
                  modalService.modalOk(gettextCatalog.getString('Specimen error'),
                                       JSON.stringify(error));
                });
            })
            .catch(function (error) {
              var header = gettextCatalog.getString('Specimen cannot be added to shipment'),
                  body;

              if (error.data && error.data.message) {
                  if (error.data.message.match(/inventory ID not found/)) {
                    body = gettextCatalog.getString(
                      'Specimen with inventory ID <b>{{id}}</b> is not present in the system.',
                      { id: inventoryId });
                  } else if (error.data.message.match(/specimen is already in active shipment/)) {
                    body = gettextCatalog.getString(
                      'Specimen with inventory ID <b>{{id}}</b> is already in this shipment or another shipment.',
                      { id: inventoryId });
                  } else if (error.data.message.match(/specimen not at shipment's from location/)) {
                    body = gettextCatalog.getString(
                      'Specimen with inventory ID <b>{{id}}</b> is at a different location than' +
                        'where shipment is coming from.',
                      { id: inventoryId });
                  } else {
                    body = error.data.message;
                  }
              } else {
                body = JSON.stringify(error);
              }

              modalService.modalOk(header, body);
            });
        });
    }

    function specimenAddConfirm() {
      notificationsService.success(gettextCatalog.getString('Specimen added'));
      refreshSpecimensTable();
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
