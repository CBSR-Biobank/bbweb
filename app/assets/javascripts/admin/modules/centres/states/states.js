/**
 * Configure routes of centres module.
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */

import _ from 'lodash'

/* @ngInject */
function config($stateProvider) {

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
      url: '/add',
      views: {
        'main@': 'centreLocationAdd'
      }
    })
    .state('home.admin.centres.centre.locations.locationView', {
      url: '/view/:locationId',
      resolve: {
        location: resolveLocation
      },
      views: {
        'main@': 'centreLocationView'
      }
    })
    .state('home.admin.centres.centre.studies', {
      url: '/studies',
      views: {
        'centreDetails': 'centreStudiesPanel'
      }
    });

  /* @ngInject */
  function resolveCentre($state, $log, $transition$, Centre) {
    const id = $transition$.params().locationId
    return Centre.get(id)
      .catch(() => {
        $log.error(`centre ID not found: centreId/${id}`)
        $state.go('404', null, { location: false })
      })
  }

  /* @ngInject */
  function resolveLocation($state, $log, $transition$, centre) {
    const id     = $transition$.params().locationId,
          result = _.find(centre.locations, { id });
    if (!result) {
      $log.error(`location ID not found in centre: centreId/${centre.id}, locationId/${id}`)
      $state.go('404', null, { location: false })
    }
    return result;
  }

}

export default ngModule => ngModule.config(config)
