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

    resolveShipment.$inject = ['Shipment', '$transition$', '$state'];
    function resolveShipment(Shipment, $transition$, $state) {
      return Shipment.get($transition$.params().shipmentId)
        .catch(function () {
          $state.go('404', null, { location: false });
        });
    }

    $stateProvider
      .state('home.shipping', {
        url: 'shipping',
        views: {
          'main@': {
            component: 'shippingHome'
          }
        }
      })
      .state('home.shipping.centre', {
        abstract: true,
        url: '/centres/{centreId}',
        resolve: {
          centre: ['Centre', '$transition$', function (Centre, $transition$) {
            return Centre.get($transition$.params().centreId);
          }]
        },
        views: {
          'main@': {
            component: 'centreShipments'
          }
        }
      })
      .state('home.shipping.centre.incoming', {
        url: '/incoming',
        views: {
          'shipments': {
            component: 'shipmentsIncoming'
          }
        }
      })
      .state('home.shipping.centre.outgoing', {
        url: '/outgoing',
        views: {
          'shipments': {
            component: 'shipmentsOutgoing'
          }
        }
      })
      .state('home.shipping.centre.completed', {
        url: '/completed',
        views: {
          'shipments': {
            component: 'shipmentsCompleted'
          }
        }
      })
      .state('home.shipping.add', {
        url: '/add',
        views: {
          'main@': {
            component: 'shipmentAdd'
          }
        }
      })
      .state('home.shipping.addItems', {
        url: '/additems/{shipmentId}',
        resolve: {
          shipment: resolveShipment
        },
        views: {
          'main@': {
            component: 'shipmentAddItems'
          }
        }
      })
      .state('home.shipping.shipment', {
        url: '/{shipmentId}',
        resolve: {
          shipment: resolveShipment
        },
        views: {
          'main@': {
            component: 'shipmentView'
          }
        }
      })
      .state('home.shipping.shipment.unpack', {
        abstract: true,
        url: '/unpack',
        views: {
          'main@': {
            component: 'unpackedShipmentView'
          }
        }
      })
      .state('home.shipping.shipment.unpack.info', {
        url: '/information',
        views: {
          'unpackedShipmentDetails': {
            component: 'unpackedShipmentInfo'
          }
        }
      })
      .state('home.shipping.shipment.unpack.unpack', {
        url: '/unpack',
        views: {
          'unpackedShipmentDetails': {
            component: 'unpackedShipmentUnpack'
          }
        }
      })
      .state('home.shipping.shipment.unpack.received', {
        url: '/received',
        resolve: {
          itemState: ['ShipmentItemState', function (ShipmentItemState) {
            return ShipmentItemState.RECEIVED;
          }]
        },
        views: {
          'unpackedShipmentDetails': {
            component: 'unpackedShipmentItems'
          }
        }
      })
      .state('home.shipping.shipment.unpack.missing', {
        url: '/missing',
        views: {
          'unpackedShipmentDetails': {
            template: [
              '<unpacked-shipment-items ',
              '  shipment="vm.shipment"',
              '  item-state="{{vm.itemState}}">',
              '</unpacked-shipment-items>'
            ].join(''),
            controller: [
              'shipment',
              'ShipmentItemState',
              function (shipment, ShipmentItemState) {
                this.shipment = shipment;
                this.itemState = ShipmentItemState.MISSING;
              }
            ],
            controllerAs: 'vm'
          }
        }
      })
      .state('home.shipping.shipment.unpack.extra', {
        url: '/extra',
        views: {
          'unpackedShipmentDetails': {
            component: 'unpackedShipmentExtra'
          }
        }
      });

    ShipmentController.$inject = ['shipment'];

    function ShipmentController(shipment) {
      this.shipment = shipment;
    }

    CentreController.$inject = [ 'centre' ];

    function CentreController(centre) {
      this.centre = centre;
    }

  }

  return config;
});
