/**
 * Configure routes of centres module.
 */
define(['../module'], function(module) {
  'use strict';

  module.config(config);

  config.$inject = ['$urlRouterProvider', '$stateProvider', 'authorizationProvider'];

  function config($urlRouterProvider, $stateProvider, authorizationProvider ) {

    resolveCentre.$inject = ['$stateParams', 'centresService'];
    function resolveCentre($stateParams, centresService) {
      if ($stateParams.centreId) {
        return centresService.get($stateParams.centreId);
      }
      throw new Error('state parameter centreId is invalid');
    }

    resolveCentreCounts.$inject = ['centresService'];
    function resolveCentreCounts(centresService) {
      return centresService.getCentreCounts();
    }

    $urlRouterProvider.otherwise('/');

    /**
     * Centres - view all centres
     */
    $stateProvider.state('home.admin.centres', {
      url: '/centres',
      resolve: {
        user: authorizationProvider.requireAuthenticatedUser,
        centreCounts: resolveCentreCounts
      },
      views: {
        'main@': {
          templateUrl: '/assets/javascripts/admin/centres/centres.html',
          controller: 'CentresCtrl as vm'
        }
      },
      data: {
        displayName: 'Centres'
      }
    });

    /**
     * Centre add
     */
    $stateProvider.state('home.admin.centres.add', {
      url: '/add',
      resolve: {
        user: authorizationProvider.requireAuthenticatedUser,
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
    $stateProvider.state('home.admin.centres.centre', {
      abstract: true,
      url: '/{centreId}',
      resolve: {
        user: authorizationProvider.requireAuthenticatedUser
      },
      views: {
        'main@': {
          templateUrl: '/assets/javascripts/admin/centres/centreView.html',
          controller: 'CentreCtrl as vm'
        }
      },
      data: {
        breadcrumProxy: 'home.admin.centres.centre.summary'
      }
    });

    /**
     * Centre add
     */
    $stateProvider.state('home.admin.centres.centre.update', {
      url: '/add',
      resolve: {
        user: authorizationProvider.requireAuthenticatedUser,
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
    $stateProvider.state('home.admin.centres.centre.summary', {
      url: '/summary',
      resolve: {
        user: authorizationProvider.requireAuthenticatedUser,
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
    $stateProvider.state('home.admin.centres.centre.locations', {
      url: '/locations',
      resolve: {
        user: authorizationProvider.requireAuthenticatedUser,
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
    $stateProvider.state('home.admin.centres.centre.locationAdd', {
      url: '/location/add',
      resolve: {
        user: authorizationProvider.requireAuthenticatedUser,
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
    $stateProvider.state('home.admin.centres.centre.locationUpdate', {
      url: '/location/update/:locationId',
      resolve: {
        user: authorizationProvider.requireAuthenticatedUser,
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
    $stateProvider.state('home.admin.centres.centre.studies', {
      url: '/studies',
      resolve: {
        user: authorizationProvider.requireAuthenticatedUser,
        centre: resolveCentre,
        centreStudies: ['centresService', 'centre', function(centresService, centre) {
          return centresService.studies(centre.id);
        }],
        studyNames: ['studiesService', function(studiesService) {
          return studiesService.getStudyNames();
        }]
      },
      views: {
        'centreDetails': {
          template: '<accordion close-others="false">' +
            '<centre-studies-panel  ' +
            '  centre="centre" ' +
            '  centre-studies="centreStudies" ' +
            '  study-names="studyNames"> ' +
            '  </centre-studies-panel>' +
            '</accordion>',
          controller: [
            '$scope', 'centre', 'centreStudies', 'studyNames',
            function($scope, centre, centreStudies, studyNames) {
              $scope.centre        = centre;
              $scope.centreStudies = centreStudies;
              $scope.studyNames    = studyNames;
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
