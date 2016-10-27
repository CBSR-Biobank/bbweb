/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 *
 * Configures routes for the administration module.
 */
define(function () {
  'use strict';

  config.$inject = [
    '$urlRouterProvider',
    '$stateProvider'
  ];

  function config($urlRouterProvider, $stateProvider) {

    resolveShipment.$inject = ['Shipment', '$stateParams'];
    function resolveShipment(Shipment, $stateParams) {
      return Shipment.get($stateParams.shipmentId);
    }

    $urlRouterProvider.otherwise('/');

    $stateProvider.state('home.shipping', {
      url: 'shipping',
      views: {
        'main@': {
          template: '<shipping-home></shipping-home>'
        }
      },
      data: {
        displayName: 'Shipping'
      }
    });

    $stateProvider.state('home.shipping.centre', {
      url: '/centres/{centreId}',
      resolve: {
        centre: ['Centre', '$stateParams', function (Centre, $stateParams) {
          return Centre.get($stateParams.centreId);
        }]
      },
      views: {
        'main@': {
          template: [
            '<centre-shipments',
            '  centre="vm.centre">',
            '</centre-shipments>'
          ].join(''),
          controller: [ 'centre', function (centre) {
            this.centre = centre;
          }],
          controllerAs: 'vm'
        }
      },
      data: {
        displayName: '{{centre.name}}'
      }
    });

    $stateProvider.state('home.shipping.add', {
      url: '/add',
      views: {
        'main@': {
          template: '<shipment-add></shipment-add>'
        }
      },
      data: {
        displayName: 'Add'
      }
    });

    $stateProvider.state('home.shipping.addItems', {
      url: '/additems/{shipmentId}',
      resolve: {
        shipment: resolveShipment
      },
      views: {
        'main@': {
          template: '<shipment-add-items shipment="vm.shipment"></shipment-add-items>',
          controller: ShipmentController,
          controllerAs: 'vm'
        }
      },
      data: {
        displayName: '{{shipment.courierName}} - {{shipment.trackingNumber}}: Items to ship'
      }
    });

    $stateProvider.state('home.shipping.shipment', {
      url: '/{shipmentId}',
      resolve: {
        shipment: resolveShipment
      },
      views: {
        'main@': {
          template: '<shipment-view shipment="vm.shipment"></shipment-view>',
          controller: ShipmentController,
          controllerAs: 'vm'
        }
      },
      data: {
        displayName: 'Shipment: {{shipment.courierName}} - {{shipment.trackingNumber}}'
      }
    });

    $stateProvider.state('home.shipping.unpack', {
      url: '/unpack/{shipmentId}',
      resolve: {
        shipment: resolveShipment
      },
      views: {
        'main@': {
          template: '<shipment-unpack shipment="vm.shipment"></shipment-unpack>',
          controller: ShipmentController,
          controllerAs: 'vm'
        }
      },
      data: {
        displayName: 'Unpack shipment: {{shipment.courierName}} - {{shipment.trackingNumber}}'
      }
    });

    ShipmentController.$inject = ['shipment'];

    function ShipmentController(shipment) {
      this.shipment = shipment;
    }

  }

  return config;
});
