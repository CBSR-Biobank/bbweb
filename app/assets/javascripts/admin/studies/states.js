/**
 * Configure routes of studies module.
 */
define(['angular'], function(angular) {
  'use strict';

  var mod = angular.module('admin.studies.states', [
    'ui.router', 'admin.studies.controllers', 'studies.services']);

  mod.config(['$stateProvider', 'userResolve', function($stateProvider, userResolve ) {
    $stateProvider
      .state('admin.studies', {
        abstract: true,
        url: '/studies',
        resolve: userResolve,
        data: {
          breadcrumbProxy: 'admin.studies.panels'
        }
      })
      .state('admin.studies.panels', {
        url: '',
        views: {
          'main@': {
            templateUrl: '/assets/javascripts/admin/studies/studiesPanels.html',
            controller: 'StudiesCtrl'
          }
        },
        resolve: userResolve,
        data: {
          displayName: 'Studies'
        }
      })
      .state('admin.studies.table', {
        url: '',
        views: {
          'main@': {
            templateUrl: '/assets/javascripts/admin/studies/studiesTable.html',
            controller: 'StudiesTableCtrl'
          }
        },
        resolve: userResolve,
        data: {
          displayName: false
        }
      })
      .state('admin.studies.add', {
        url: '/add',
        resolve: {
          study: function() {
            return { name: "", description: null };
          },
          user: function() {
            return userResolve;
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
      })
      .state('admin.studies.study', {
        url: '/{studyId}',
        resolve: {
          study: ['$stateParams', 'StudyService', function($stateParams, StudyService) {
            if ($stateParams.studyId) {
              return StudyService.query($stateParams.studyId).then(function(response) {
                return response.data;
              });
            }
            throw new Error("state parameter studyId is invalid");
          }],
          user: function() {
            return userResolve;
          }
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
                  participants: false
                };
                if ($state.current.name === 'admin.studies.study.participants') {
                  $timeout(function() {
                    $scope.tabActive.participants = true;
                  }, 0);
                  return;
                }

                // if ($state.current.name === 'admin.studies.study') {
                // } else {
                // }
              }]
          }
        },
        data: {
          displayName: "{{study.name}}"
        }
      })
      .state('admin.studies.study.summary', {
        url: '/summary',
        resolve: {
          user: function() {
            return userResolve;
          }
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
      })
      .state('admin.studies.study.participants', {
        url: '/participants',
        resolve: {
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
            }],
          user: function() {
            return userResolve;
          }
        },
        views: {
          'studyDetails': {
            templateUrl: '/assets/javascripts/admin/studies/studyParticipantsPane.html',
            controller: 'ParticipantsPaneCtrl'
          }
        },
        data: {
          displayName: false
        }
      })
      .state('admin.studies.study.specimens', {
        url: '/specimens',
        resolve: {
          specimenGroups: [
            '$stateParams',
            'SpecimenGroupService',
            '$log',
            function($stateParams, SpecimenGroupService, $log) {
              if ($stateParams.studyId) {
                return SpecimenGroupService.getAll($stateParams.studyId).then(function(response) {
                  return response.data;
                });
              }
              throw new Error("state parameter studyId is invalid");
            }],
          user: function() {
            return userResolve;
          }
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
      })
      .state('admin.studies.study.update', {
        url: '/update',
        resolve: {
          user: function() {
            return userResolve;
          }
        },
        views: {
          'main@': {
            templateUrl: '/assets/javascripts/admin/studies/studyForm.html',
            controller: 'StudyUpdateCtrl'
          }
        },
        data: {
          displayName: 'Update'
        }
      })
      .state('admin.studies.error', {
        url: '/error',
        views: {
          'main@': {
            template: '<div><h1>Study does not exist</h1></div>'
          }
        },
        resolve: {
          user: function() {
            return userResolve;
          }
        }
      });
  }]);
  return mod;
});
