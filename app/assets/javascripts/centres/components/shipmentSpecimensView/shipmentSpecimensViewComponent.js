/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(function (require) {
  'use strict';

  var component = {
    templateUrl : '/assets/javascripts/centres/components/shipmentSpecimensView/shipmentSpecimensView.html',
    controller: ShipmentSpecimensViewController,
    controllerAs: 'vm',
    bindings: {
      shipment: '<',
      readOnly: '<'
    }
  };

  ShipmentSpecimensViewController.$inject = [
    '$q',
    '$controller',
    'ShipmentSpecimen'
  ];

  /**
   *
   */
  function ShipmentSpecimensViewController($q,
                                           $controller,
                                           ShipmentSpecimen) {
    var vm = this;

    $controller('ShipmentSpecimenController',
                {
                  vm:               vm,
                  $q:               $q,
                  ShipmentSpecimen: ShipmentSpecimen
                });
  }

  return component;
});
