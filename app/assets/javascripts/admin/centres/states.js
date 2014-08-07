/**
 * Configure routes of centres module.
 */
define(['angular'], function(angular) {
  'use strict';

  var mod = angular.module('admin.centres.states', [
    'ui.router',
    'user.services',
    //'admin.centres.controllers',
    //'centres.services'
  ]);

  mod.config([
    '$urlRouterProvider', '$stateProvider', 'userResolve',
    function($urlRouterProvider, $stateProvider, userResolve ) {

      $urlRouterProvider.otherwise('/');

      /**
       * Centres - view all centres in panels
       */
      $stateProvider.state('admin.centres', {
        url: '/centres',
        resolve: {
          user: userResolve.user
        },
        views: {
          'main@': {
            templateUrl: '/assets/javascripts/admin/centres/centresPanels.html',
            controller: 'CentresCtrl'
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
          user: userResolve.user
        },
        views: {
          'main@': {
            templateUrl: '/assets/javascripts/admin/centres/centresTable.html',
            controller: 'CentresTableCtrl'
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
            return { name: "", description: null };
          }
        },
        views: {
          'main@': {
            templateUrl: '/assets/javascripts/admin/centres/centreForm.html',
            controller: 'CentreAddCtrl'
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
        url: '/{centreId}',
        resolve: {
          user: userResolve.user,
          centre: ['$stateParams', 'CentreService', function($stateParams, CentreService) {
            if ($stateParams.centreId) {
              return CentreService.query($stateParams.centreId).then(function(response) {
                return response.data;
              });
            }
            throw new Error("state parameter centreId is invalid");
          }]
        },
        views: {
          'main@': {
            templateUrl: '/assets/javascripts/admin/centres/centreView.html',
            controller: [
              '$scope', '$state', '$timeout', 'centre',
              function($scope, $state, $timeout, centre) {
                $scope.centre = centre;

                if ($state.current.name === 'admin.centres.centre') {
                  $state.go('admin.centres.centre.summary', { centreId: centre.id });
                  return;
                }

                /*
                 * At the moment, static tabs overwrite whatever is passed to active when the directive is
                 * run, which is a bug. As a kludge, a timeout with 0 seconds delay is used to set the active
                 * state.
                 */
                $scope.tabActive = {
                  locations: false,
                  studies: false
                };

                if ($state.current.name === 'admin.centres.centre.locations') {
                  $timeout(function() {
                    $scope.tabActive.participants = true;
                  }, 0);
                } else if ($state.current.name === 'admin.centres.centre.studies') {
                  $timeout(function() {
                    $scope.tabActive.specimens = true;
                  }, 0);
                }
              }]
          }
        },
        data: {
          displayName: "{{centre.name}}"
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
            controller: 'CentreSummaryTabCtrl'
          }
        },
        data: {
          displayName: false
        }
      });

      /**
       * Centre view participatns information
       */
      $stateProvider.state('admin.centres.centre.locations', {
        url: '/locations',
        resolve: {
          user: userResolve.user,
          annotTypes: [
            'CentreLocationService', 'centre',
            function(CentreLocationService, centre) {
              return CentreLocationService.getAll(centre.id).then(function(response) {
                return response.data;
              });
            }]
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
          displayName: false
        }
      });

    }]);
  return mod;
});
