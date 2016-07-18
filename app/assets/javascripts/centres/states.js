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

    $urlRouterProvider.otherwise('/');

    $stateProvider.state('home.shipping', {
      url: 'shipping',
      resolve: {
        centreLocations: ['Centre', function (Centre) {
          return Centre.allLocations().then(function (centreLocations) {
            return Centre.centreLocationToNames(centreLocations);
          });
        }]
      },
      views: {
        'main@': {
          template: '<shipping-home centre-locations="vm.centreLocations"></shipping-home>',
          controller: [ 'centreLocations', function (centreLocations) {
            this.centreLocations = centreLocations;
          }],
          controllerAs: 'vm'
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
            '<shipments-table',
            '  centre="vm.centre"',
            '</shipments-table>'
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
          template: '<shipment-add centre-locations="vm.centreLocations"></shipment-add>',
          controller: [ 'centreLocations', function (centreLocations) {
            this.centreLocations = centreLocations;
          }],
          controllerAs: 'vm'
        }
      },
      data: {
        displayName: 'Add'
      }
    });

    $stateProvider.state('home.shipping.addSpecimens', {
      url: '/{shipmentId}',
      resolve: {
        shipment: ['Shipment', '$stateParams', function (Shipment, $stateParams) {
          return Shipment.get($stateParams.shipmentId);
        }]
      },
      views: {
        'main@': {
          template: '<shipment-add-specimens shipment="vm.shipment"></shipment-add-specimens>',
          controller: [ 'shipment', function (shipment) {
            this.shipment = shipment;
          }],
          controllerAs: 'vm'
        }
      },
      data: {
        displayName: 'Add'
      }
    });
  }

  return config;
});
