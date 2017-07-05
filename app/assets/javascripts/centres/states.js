/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 *
 * Configures routes for the administration module.
 */
define(function () {
  'use strict';

  config.$inject = [ '$stateProvider' ];

  function config($stateProvider) {

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
          'main@': 'shippingHome'
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
          'main@': 'centreShipments'
        }
      })
      .state('home.shipping.centre.incoming', {
        url: '/incoming',
        views: {
          'shipments': 'shipmentsIncoming'
        }
      })
      .state('home.shipping.centre.outgoing', {
        url: '/outgoing',
        views: {
          'shipments': 'shipmentsOutgoing'
        }
      })
      .state('home.shipping.centre.completed', {
        url: '/completed',
        views: {
          'shipments': 'shipmentsCompleted'
        }
      })
      .state('home.shipping.add', {
        url: '/add',
        views: {
          'main@': 'shipmentAdd'
        }
      })
      .state('home.shipping.addItems', {
        url: '/additems/{shipmentId}',
        resolve: {
          shipment: resolveShipment
        },
        views: {
          'main@': 'shipmentAddItems'
        }
      })
      .state('home.shipping.shipment', {
        url: '/{shipmentId}',
        resolve: {
          shipment: resolveShipment
        },
        views: {
          'main@': 'shipmentView'
        }
      })
      .state('home.shipping.shipment.unpack', {
        abstract: true,
        url: '/unpack',
        views: {
          'main@': 'unpackedShipmentView'
        }
      })
      .state('home.shipping.shipment.unpack.info', {
        url: '/information',
        views: {
          'unpackedShipmentDetails': 'unpackedShipmentInfo'
        }
      })
      .state('home.shipping.shipment.unpack.unpack', {
        url: '/unpack',
        views: {
          'unpackedShipmentDetails': 'unpackedShipmentUnpack'
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
          'unpackedShipmentDetails': 'unpackedShipmentItems'
        }
      })
      .state('home.shipping.shipment.unpack.missing', {
        url: '/missing',
        views: {
          'unpackedShipmentDetails': 'unpackedShipmentItems'
        }
      })
      .state('home.shipping.shipment.unpack.extra', {
        url: '/extra',
        views: {
          'unpackedShipmentDetails': 'unpackedShipmentExtra'
        }
      });

  }

  return config;
});
