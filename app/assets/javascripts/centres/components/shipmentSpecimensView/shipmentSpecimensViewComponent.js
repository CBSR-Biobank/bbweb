/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(function () {
  'use strict';

  var component = {
    template: require('./shipmentSpecimensView.html'),
    controller: ShipmentSpecimensViewController,
    controllerAs: 'vm',
    bindings: {
      shipment:      '<',
      readOnly:      '<',
      showItemState: '<'
    }
  };

  ShipmentSpecimensViewController.$inject = [
    '$q',
    '$controller',
    'ShipmentSpecimen'
  ];

  function ShipmentSpecimensViewController($q,
                                           $controller,
                                           ShipmentSpecimen) {
    var vm = this;
    vm.$onInit = onInit;

    //--

    function onInit() {
      $controller('ShipmentSpecimenController',
                  {
                    vm:               vm,
                    $q:               $q,
                    ShipmentSpecimen: ShipmentSpecimen
                  });
    }
  }

  return component;
});
