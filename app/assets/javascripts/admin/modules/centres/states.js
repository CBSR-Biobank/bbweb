/**
 * Configure routes of centres module.
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(['lodash'], function (_) {
  'use strict';

  centreStates.$inject = [ '$stateProvider' ];

  function centreStates($stateProvider) {

    $stateProvider
      .state('home.admin.centres', {
        url: '/centres',
        views: {
          'main@': {
            template: '<centres-admin></centres-admin>'
          }
        }
      })
      .state('home.admin.centres.add', {
        url: '/add',
        views: {
          'main@': 'centreAdd'
        }
      })
      .state('home.admin.centres.centre', {
        abstract: true,
        url: '/{centreId}',
        resolve: {
          centre: resolveCentre
        },
        views: {
          'main@': 'centreView'
        }
      })
      .state('home.admin.centres.centre.summary', {
        url: '/summary',
        views: {
          'centreDetails': 'centreSummary'
        }
      })
      .state('home.admin.centres.centre.locations', {
        url: '/locations',
        views: {
          'centreDetails': 'locationsPanel'
        }
      })
      .state('home.admin.centres.centre.locations.locationAdd', {
        url: '/location/add',
        views: {
          'main@': 'centreLocationAdd'
        }
      })
      .state('home.admin.centres.centre.locations.locationView', {
        url: '/location/view/:locationId',
        resolve: {
          location: [
            '$transition$',
            'centre',
            function ($transition$, centre) {
              return _.find(centre.locations, { id: $transition$.params().locationId });
            }
          ]
        },
        views: {
          'main@': 'centreLocationView'
        }
      })
      .state('home.admin.centres.centre.studies', {
        url: '/studies',
        resolve: {
          centre: resolveCentre
        },
        views: {
          'centreDetails': 'centreStudiesPanel'
        }
      });

    resolveCentre.$inject = ['$transition$', 'Centre'];
    function resolveCentre($transition$, Centre) {
      return Centre.get($transition$.params().centreId);
    }

  }

  return centreStates;
});
