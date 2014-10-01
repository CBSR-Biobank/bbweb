/**
 * Configure routes of centres module.
 */
define(['./module', 'underscore'], function(module, _) {
  'use strict';

  module.config(config);

  config.$inject = ['$urlRouterProvider', '$stateProvider', 'userResolve'];

  function config($urlRouterProvider, $stateProvider, userResolve ) {

    $urlRouterProvider.otherwise('/');

    /**
     * Centres - view all centres in panels
     */
    $stateProvider.state('admin.centres', {
      url: '/centres',
      resolve: {
        user: userResolve.user,
        centres: ['centreService', function(centreService) {
          return centreService.list().then(function(centres) {
            return _.sortBy(centres, function(centre) { return centre.name; });
          });
        }]
      },
      views: {
        'main@': {
          templateUrl: '/assets/javascripts/admin/centres/centresPanels.html',
          controller: 'CentresCtrl as vm'
        }
      },
      data: {
        displayName: 'Centres'
      }
    });

    /**
     * Centres - view all centres in a table
     */
    $stateProvider.state('admin.centres.table', {
      url: '',
      resolve: {
        user: userResolve.user,
        centres: ['centreService', function(centreService) {
          return centreService.list().then(function(centres) {
            return _.sortBy(centres, function(centre) { return centre.name; });
          });
        }]
      },
      views: {
        'main@': {
          templateUrl: '/assets/javascripts/admin/centres/centresTable.html',
          controller: 'CentresTableCtrl as vm'
        }
      },
      data: {
        displayName: false
      }
    });

    /**
     * Centre add
     */
    $stateProvider.state('admin.centres.add', {
      url: '/add',
      resolve: {
        user: userResolve.user,
        centre: function() {
          return { name: '', description: null };
        }
      },
      views: {
        'main@': {
          templateUrl: '/assets/javascripts/admin/centres/centreForm.html',
          controller: 'CentreEditCtrl as vm'
        }
      },
      data: {
        displayName: 'Add centre',
        returnState: 'admin.centres'
      }
    });

    /**
     * Centre view
     */
    $stateProvider.state('admin.centres.centre', {
      abstract: true,
      url: '/{centreId}',
      resolve: {
        user: userResolve.user,
        centre: ['$stateParams', 'centreService', function($stateParams, centreService) {
          if ($stateParams.centreId) {
            return centreService.query($stateParams.centreId);
          }
          throw new Error('state parameter centreId is invalid');
        }]
      },
      views: {
        'main@': {
          templateUrl: '/assets/javascripts/admin/centres/centreView.html'
        }
      },
      data: {
        breadcrumProxy: 'admin.studies.centre.summary'
      }
    });

    /**
     * Centre view summary information
     */
    $stateProvider.state('admin.centres.centre.summary', {
      url: '/summary',
      resolve: {
        user: userResolve.user
      },
      views: {
        'centreDetails': {
          templateUrl: '/assets/javascripts/admin/centres/centreSummaryTab.html',
          controller: 'CentreSummaryTabCtrl as vm'
        }
      },
      data: {
        displayName: '{{centre.name}}'
      }
    });

    /**
     * Centre view participatns information
     */
    $stateProvider.state('admin.centres.centre.locations', {
      url: '/locations',
      resolve: {
        user: userResolve.user,
        locations: [
          'centreLocationService', 'centre',
          function(centreLocationService, centre) {
            return centreLocationService.list(centre.id);
          }
        ]
      },
      views: {
        'centreDetails': {
          template: '<accordion close-others="false">' +
            '<locations-annot-types-panel></locations-annot-types-panel>' +
            '</accordion>',
          controller: 'LocationsTabCtrl'
        }
      },
      data: {
        displayName: '{{centre.name}}'
      }
    });

  }

});
