/**
 * UI Router states for {@link domain.centres.Centre Centre} administration.
 *
 * @namespace admin.centres.states
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

import _ from 'lodash'

/**
 * UI Router states used for {@link domain.centres.Centre Centre} Administration.
 *
 * @name admin.centres.states.adminCentresUiRouterConfig
 * @function
 *
 * @param {AngularJS_Service} $stateProvider
 */
/* @ngInject */
function adminCentresUiRouterConfig($stateProvider) {

  $stateProvider
    .state('home.admin.centres', {
      /* The entry state */
      url: '/centres',
      views: {
        'main@': {
          template: '<centres-admin></centres-admin>'
        }
      }
    })
    .state('home.admin.centres.add', {
      /* Adds a centre */
      url: '/add',
      views: {
        'main@': 'centreAdd'
      }
    })
    .state('home.admin.centres.centre', {
      /* virtual state for viewing a centre */
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
      /* for viewing the summary for a centre */
      url: '/summary',
      views: {
        'centreDetails': 'centreSummary'
      }
    })
    .state('home.admin.centres.centre.locations', {
      /* shows the locations for a centre */
      url: '/locations',
      views: {
        'centreDetails': 'locationsPanel'
      }
    })
    .state('home.admin.centres.centre.locations.locationAdd', {
      /* adds a loction to a centre */
      url: '/add',
      views: {
        'main@': 'centreLocationAdd'
      }
    })
    .state('home.admin.centres.centre.locations.locationView', {
      /* views a single location on a centre */
      url: '/view/:locationSlug',
      resolve: {
        location: resolveLocation
      },
      views: {
        'main@': 'centreLocationView'
      }
    })
    .state('home.admin.centres.centre.studies', {
      /* shows the studies associated with a centre */
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

export default ngModule => ngModule.config(adminCentresUiRouterConfig)
