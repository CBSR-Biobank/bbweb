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

    resolveShipment.$inject = ['Shipment', '$stateParams', '$state'];
    function resolveShipment(Shipment, $stateParams, $state) {
      return Shipment.get($stateParams.shipmentId)
        .catch(function (error) {
          $state.go('404', null, { location: false });
        });
    }

    $stateProvider
      .state('home.shipping', {
        url: 'shipping',
        views: {
          'main@': {
            template: '<shipping-home></shipping-home>'
          }
        },
        data: {
          displayName: 'Shipping'
        }
      })
      .state('home.shipping.centre', {
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
      })
      .state('home.shipping.add', {
        url: '/add',
        views: {
          'main@': {
            template: '<shipment-add></shipment-add>'
          }
        },
        data: {
          displayName: 'Add'
        }
      })
      .state('home.shipping.addItems', {
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
      })
      .state('home.shipping.shipment', {
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
      })
      .state('home.shipping.unpack', {
        abstract: true,
        url: '/unpack/{shipmentId}',
        resolve: {
          shipment: resolveShipment
        },
        views: {
          'main@': {
            template: '<unpacked-shipment-view shipment="vm.shipment"></unpacked-shipment-view>',
            controller: ShipmentController,
            controllerAs: 'vm'
          }
        },
        data: {
          breadcrumProxy: 'home.shipping.unpack.info'
        }
      })
      .state('home.shipping.unpack.info', {
        url: '/information',
        views: {
          'unpackedShipmentDetails': {
            template: '<unpacked-shipment-info shipment="vm.shipment"></unpacked-shipment-info>',
            controller: ShipmentController,
            controllerAs: 'vm'
          }
        },
        data: {
          displayName: 'Unpack shipment: {{shipment.courierName}} - {{shipment.trackingNumber}}'
        }
      })
      .state('home.shipping.unpack.receive', {
        url: '/receive',
        views: {
          'unpackedShipmentDetails': {
            template: '<unpacked-shipment-receive shipment="vm.shipment"></unpacked-shipment-receive>',
            controller: ShipmentController,
            controllerAs: 'vm'
          }
        },
        data: {
          displayName: 'Unpack shipment: {{shipment.courierName}} - {{shipment.trackingNumber}}'
        }
      })
      .state('home.shipping.unpack.unpacked', {
        url: '/unpacked',
        views: {
          'unpackedShipmentDetails': {
            template: '<unpacked-shipment-unpacked shipment="vm.shipment"></unpacked-shipment-unpacked>',
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
