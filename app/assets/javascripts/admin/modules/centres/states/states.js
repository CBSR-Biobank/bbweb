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
      url: '/{centreSlug}',
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
      url: '/view/:locationSlug',
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
  function resolveCentre($transition$, resourceErrorService, Centre) {
    const slug = $transition$.params().centreSlug
    return Centre.get(slug)
      .catch(resourceErrorService.goto404(`centre ID not found: ${slug}`))
  }

  /* @ngInject */
  function resolveLocation($q, $transition$, resourceErrorService, centre) {
    const slug     = $transition$.params().locationSlug,
          location = _.find(centre.locations, { slug }),
          result   = location ? $q.when(location) : $q.reject('invalid location ID')
    return result.catch(resourceErrorService.goto404(`invalid location ID: ${slug}`))
  }

}

export default ngModule => ngModule.config(config)
