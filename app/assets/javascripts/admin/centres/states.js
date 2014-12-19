/**
 * Configure routes of centres module.
 */
define(['../module', 'underscore'], function(module, _) {
  'use strict';

  module.config(config);

  config.$inject = ['$urlRouterProvider', '$stateProvider', 'userResolve'];

  function config($urlRouterProvider, $stateProvider, userResolve ) {

    resolveCentre.$inject = ['$stateParams', 'centresService'];

    function resolveCentre($stateParams, centresService) {
      if ($stateParams.centreId) {
        return centresService.get($stateParams.centreId);
      }
      throw new Error('state parameter centreId is invalid');
    }

    $urlRouterProvider.otherwise('/');

    /**
     * Centres - view all centres in panels
     */
    $stateProvider.state('admin.centres', {
      url: '/centres',
      resolve: {
        user: userResolve.user,
        centres: ['centresService', function(centresService) {
          return centresService.list().then(function(centres) {
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
        centres: ['centresService', function(centresService) {
          return centresService.list().then(function(centres) {
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
        displayName: 'Add centre'
      }
    });

    /**
     * Centre view
     */
    $stateProvider.state('admin.centres.centre', {
      abstract: true,
      url: '/{centreId}',
      resolve: {
        user: userResolve.user
      },
      views: {
        'main@': {
          templateUrl: '/assets/javascripts/admin/centres/centreView.html',
          controller: 'CentreCtrl as vm'
        }
      },
      data: {
        breadcrumProxy: 'admin.studies.centre.summary'
      }
    });

    /**
     * Centre add
     */
    $stateProvider.state('admin.centres.centre.update', {
      url: '/add',
      resolve: {
        user: userResolve.user,
        centre: resolveCentre
      },
      views: {
        'main@': {
          templateUrl: '/assets/javascripts/admin/centres/centreForm.html',
          controller: 'CentreEditCtrl as vm'
        }
      },
      data: {
        displayName: 'Add centre'
      }
    });

    /**
     * Centre view summary information
     */
    $stateProvider.state('admin.centres.centre.summary', {
      url: '/summary',
      resolve: {
        user: userResolve.user,
        centre: resolveCentre
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
     * Centre view location information
     */
    $stateProvider.state('admin.centres.centre.locations', {
      url: '/locations',
      resolve: {
        user: userResolve.user,
        centre: resolveCentre,
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
            '<locations-panel centre="centre" locations="locations"></locations-panel>' +
            '</accordion>',
          controller: [
            '$scope', 'centre', 'locations',
            function($scope, centre, locations) {
              $scope.centre = centre;
              $scope.locations = locations;
            }
          ]
        }
      },
      data: {
        displayName: '{{centre.name}}'
      }
    });

    /**
     * Used to add a centre location.
     */
    $stateProvider.state('admin.centres.centre.locationAdd', {
      url: '/location/add',
      resolve: {
        user: userResolve.user,
        centre: resolveCentre,
        location: [function() {
          return {
            name           : '',
            street         : '',
            city           : '',
            province       : '',
            postalCode     : '',
            poBoxNumber    : '',
            countryIsoCode : ''
          };
        }]
      },
      views: {
        'main@': {
          templateUrl: '/assets/javascripts/admin/locationForm.html',
          controller: 'LocationEditCtrl as vm'
        }
      },
      data: {
        displayName: 'Specimen Group'
      }
    });

    /**
     * Used to update a centre location.
     */
    $stateProvider.state('admin.centres.centre.locationUpdate', {
      url: '/location/update/:locationId',
      resolve: {
        user: userResolve.user,
        centre: resolveCentre,
        location: [
          '$stateParams', 'centreLocationService', 'centre',
          function($stateParams, centreLocationService, centre) {
            return centreLocationService.query(centre.id, $stateParams.locationId);
          }
        ]
      },
      views: {
        'main@': {
          templateUrl: '/assets/javascripts/admin/locationForm.html',
          controller: 'LocationEditCtrl as vm'
        }
      },
      data: {
        displayName: 'Specimen Group'
      }
    });

    /**
     * Centre view studies information
     */
    $stateProvider.state('admin.centres.centre.studies', {
      url: '/studies',
      resolve: {
        user: userResolve.user,
        centre: resolveCentre,
        centreStudies: [
          'centresService', 'centre',
          function(centresService, centre) {
            return centresService.studies(centre.id);
          }
        ],
        allStudies: [
          'studiesService',
          function(studiesService) {
            return studiesService.getAll();
          }
        ]
      },
      views: {
        'centreDetails': {
          template: '<accordion close-others="false">' +
            '<centre-studies-panel  ' +
            '  centre="centre" ' +
            '  centre-studies="centreStudies" ' +
            '  all-studies="allStudies"></centre-studies-panel>' +
            '</accordion>',
          controller: [
            '$scope', 'centre', 'centreStudies', 'allStudies',
            function($scope, centre, centreStudies, allStudies) {
              $scope.centre = centre;
              $scope.centreStudies = centreStudies;
              $scope.allStudies = allStudies;
            }
          ]
        }
      },
      data: {
        displayName: '{{centre.name}}'
      }
    });
  }

});
