/**
 * Configure routes of centres module.
 */
define([], function() {
  'use strict';

  centreStates.$inject = ['$urlRouterProvider', '$stateProvider', 'authorizationProvider'];

  function centreStates($urlRouterProvider, $stateProvider, authorizationProvider ) {

    resolveCentre.$inject = ['$stateParams', 'Centre'];
    function resolveCentre($stateParams, Centre) {
      return Centre.get($stateParams.centreId);
    }

    resolveCentreCounts.$inject = ['CentreCounts'];
    function resolveCentreCounts(CentreCounts) {
      return CentreCounts.get();
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
        centre: ['Centre', function(Centre) {
          return new Centre();
        }]
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
        user: authorizationProvider.requireAuthenticatedUser,
        centre: resolveCentre
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
        locations: ['centre', function(centre) {
          return centre.getLocations();
        }]
      },
      views: {
        'centreDetails': {
          template: '<accordion close-others="false">' +
            '<locations-panel centre="centre"></locations-panel>' +
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
        centre: resolveCentre
      },
      views: {
        'main@': {
          templateUrl: '/assets/javascripts/admin/centres/locationForm.html',
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
        centre: [
          '$stateParams', 'centre',
          function($stateParams, centre) {
            return centre.getLocation($stateParams.locationId);
          }
        ]
      },
      views: {
        'main@': {
          templateUrl: '/assets/javascripts/admin/centres/locationForm.html',
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
        centreStudies: ['centre', function(centre) {
          return centre.getStudyIds();
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

  return centreStates;
});
