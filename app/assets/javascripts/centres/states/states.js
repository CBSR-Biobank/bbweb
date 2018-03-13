/**
 * UI Router states used {@link domain.centres.Shipment Shipping}.
 *
 * @namespace centres.states
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

/**
 * UI Router states used {@link domain.centres.Shipment Shipping}.
 *
 * @name centres.states.centresUiRouterConfig
 * @function
 *
 * @param {AngularJS_Service} $stateProvider
 */
/* @ngInject */
function centresUiRouterConfig($stateProvider) {

  $stateProvider
    .state('home.shipping', {
      // this state is checked for an authorized user, see uiRouterIsAuthorized() in app.js
      url: 'shipping',
      views: {
        'main@': 'shippingHome'
      }
    })
    .state('home.shipping.centre', {
      abstract: true,
      url: '/centres/{centreSlug}',
      resolve: {
        centre: resolveCentre
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
      resolve: {
        itemState: ['ShipmentItemState', function (ShipmentItemState) {
          return ShipmentItemState.MISSING;
        }]
      },
      views: {
        'unpackedShipmentDetails': 'unpackedShipmentItems'
      }
    })
    .state('home.shipping.shipment.unpack.extra', {
      url: '/extra',
      resolve: {
        itemState: ['ShipmentItemState', function (ShipmentItemState) {
          return ShipmentItemState.EXTRA;
        }]
      },
      views: {
        'unpackedShipmentDetails': 'unpackedShipmentExtra'
      }
    });

  /* @ngInject */
  function resolveCentre($transition$, resourceErrorService, Centre) {
    const slug = $transition$.params().centreSlug
    return Centre.get(slug)
      .catch(resourceErrorService.goto404(`centre slug not found: centreId/${slug}`))
  }

  /* @ngInject */
  function resolveShipment(Shipment, $transition$, resourceErrorService) {
    const id = $transition$.params().shipmentId
    return Shipment.get(id)
      .catch(resourceErrorService.goto404(`shipment ID not found: shipmentId/${id}`))
  }

}

export default ngModule => ngModule.config(centresUiRouterConfig)
