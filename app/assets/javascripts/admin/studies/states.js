/**
 * Configure routes of studies module.
 */
define(['angular'], function(angular) {
  'use strict';

  var mod = angular.module('admin.studies.states', [
    'ui.router', 'user.services', 'admin.studies.controllers', 'studies.services']);

  mod.config([
    '$urlRouterProvider', '$stateProvider', 'userResolve',
    function($urlRouterProvider, $stateProvider, userResolve ) {

      $urlRouterProvider.otherwise('/');

      /**
       * Studies - view all studies in panels
       */
      $stateProvider.state('admin.studies', {
        url: '/studies',
        resolve: {
          user: userResolve.user
        },
        views: {
          'main@': {
            templateUrl: '/assets/javascripts/admin/studies/studiesPanels.html',
            controller: 'StudiesCtrl'
          }
        },
        data: {
          displayName: 'Studies'
        }
      });

      /**
       * Studies - view all studies in a table
       */
      $stateProvider.state('admin.studies.table', {
        url: '',
        resolve: {
          user: userResolve.user
        },
        views: {
          'main@': {
            templateUrl: '/assets/javascripts/admin/studies/studiesTable.html',
            controller: 'StudiesTableCtrl'
          }
        },
        data: {
          displayName: false
        }
      });

      /**
       * Study add
       */
      $stateProvider.state('admin.studies.add', {
        url: '/add',
        resolve: {
          user: userResolve.user,
          study: function() {
            return { name: "", description: null };
          }
        },
        views: {
          'main@': {
            templateUrl: '/assets/javascripts/admin/studies/studyForm.html',
            controller: 'StudyAddCtrl'
          }
        },
        data: {
          displayName: 'Add Study'
        }
      });

      /**
       * Study view
       */
      $stateProvider.state('admin.studies.study', {
        url: '/{studyId}',
        resolve: {
          user: userResolve.user,
          study: ['$stateParams', 'StudyService', function($stateParams, StudyService) {
            if ($stateParams.studyId) {
              return StudyService.query($stateParams.studyId).then(function(response) {
                return response.data;
              });
            }
            throw new Error("state parameter studyId is invalid");
          }]
        },
        views: {
          'main@': {
            templateUrl: '/assets/javascripts/admin/studies/studyView.html',
            controller: [
              '$scope', '$state', '$timeout', '$log', 'study',
              function($scope, $state, $timeout, $log, study) {
                $scope.study = study;

                if ($state.current.name === 'admin.studies.study') {
                  $state.go('admin.studies.study.summary', { studyId: study.id });
                  return;
                }

                /*
                 * At the moment, static tabs overwrite whatever is passed to active when the directive is run,
                 * which is a bug. As a kludge, a timeout with 0 seconds delay is used to set the active state.
                 *
                 */
                $scope.tabActive = {
                  participants: false,
                  specimens: false,
                  collection: false
                };

                if ($state.current.name === 'admin.studies.study.participants') {
                  $timeout(function() {
                    $scope.tabActive.participants = true;
                  }, 0);
                } else if ($state.current.name === 'admin.studies.study.specimens') {
                  $timeout(function() {
                    $scope.tabActive.specimens = true;
                  }, 0);
                } else if ($state.current.name === 'admin.studies.study.collection') {
                  $timeout(function() {
                    $scope.tabActive.collection = true;
                  }, 0);
                }
              }]
          }
        },
        data: {
          displayName: "{{study.name}}"
        }
      });

      /**
       * Study view summary information
       */
      $stateProvider.state('admin.studies.study.summary', {
        url: '/summary',
        resolve: {
          user: userResolve.user
        },
        views: {
          'studyDetails': {
            templateUrl: '/assets/javascripts/admin/studies/studySummaryPane.html',
            controller: 'StudySummaryCtrl'
          }
        },
        data: {
          displayName: false
        }
      });

      /**
       * Study view participatns information
       */
      $stateProvider.state('admin.studies.study.participants', {
        url: '/participants',
        resolve: {
          user: userResolve.user,
          annotTypes: [
            '$stateParams',
            'ParticipantAnnotTypeService',
            '$log',
            function($stateParams, ParticipantAnnotTypeService, $log) {
              if ($stateParams.studyId) {
                return ParticipantAnnotTypeService.getAll($stateParams.studyId).then(function(response) {
                  return response.data;
                });
              }
              throw new Error("state parameter studyId is invalid");
            }]
        },
        views: {
          'studyDetails': {
            template: '<ng-include src="\'/assets/javascripts/admin/studies/annotationTypePanel.html\'"></ng-include>',
            controller: 'ParticipantsPaneCtrl'
          }
        },
        data: {
          displayName: false
        }
      });

      /**
       * Study view specimen information
       */
      $stateProvider.state('admin.studies.study.specimens', {
        url: '/specimens',
        resolve: {
          user: userResolve.user,
          specimenGroups: [
            '$stateParams',
            'SpecimenGroupService',
            function($stateParams, SpecimenGroupService) {
              if ($stateParams.studyId) {
                return SpecimenGroupService.getAll($stateParams.studyId).then(function(response) {
                  return response.data;
                });
              }
              throw new Error("state parameter studyId is invalid");
            }]
        },
        views: {
          'studyDetails': {
            templateUrl: '/assets/javascripts/admin/studies/studySpecimensPane.html',
            controller: 'SpecimensPaneCtrl'
          }
        },
        data: {
          displayName: false
        }
      });

      /**
       * Study view collection information
       */
      $stateProvider.state('admin.studies.study.collection', {
        url: '/collection',
        resolve: {
          user: userResolve.user,
          ceventTypes: [
            '$stateParams',
            'CeventTypeService',
            function($stateParams, CeventTypeService) {
              if ($stateParams.studyId) {
                return CeventTypeService.getAll($stateParams.studyId).then(function(response) {
                  return response.data;
                });
              }
              throw new Error("state parameter studyId is invalid");
            }],
          annotTypes: [
            '$stateParams',
            'CeventAnnotTypeService',
            function($stateParams, CeventAnnotTypeService) {
              if ($stateParams.studyId) {
                return CeventAnnotTypeService.getAll($stateParams.studyId).then(function(response) {
                  return response.data;
                });
              }
              throw new Error("state parameter studyId is invalid");
            }],
          specimenGroups: [
            '$stateParams',
            'SpecimenGroupService',
            function($stateParams, SpecimenGroupService) {
              if ($stateParams.studyId) {
                return SpecimenGroupService.getAll($stateParams.studyId).then(function(response) {
                  return response.data;
                });
              }
              throw new Error("state parameter studyId is invalid");
            }]
        },
        views: {
          'studyDetails': {
            templateUrl: '/assets/javascripts/admin/studies/studyCollectionPane.html',
            controller: 'CollectionPaneCtrl'
          }
        },
        data: {
          displayName: false
        }
      });

      /**
       * Study summary information update
       */
      $stateProvider.state('admin.studies.study.update', {
        url: '/update',
        resolve: {
          user: userResolve.user
        },
        views: {
          'main@': {
            templateUrl: '/assets/javascripts/admin/studies/studyForm.html',
            controller: 'StudyUpdateCtrl'
          }
        }
      });

      /**
       * Study error
       */
      $stateProvider.state('admin.studies.error', {
        url: '/error',
        resolve: {
          user: userResolve.user
        },
        views: {
          'main@': {
            template: '<div><h1>Study does not exist</h1></div>'
          }
        }
      });
    }]);
  return mod;
});
