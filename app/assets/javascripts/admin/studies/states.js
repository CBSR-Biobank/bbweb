/**
 * Configure routes of studies module.
 */
define(['angular'], function(angular) {
  'use strict';

  var mod = angular.module('admin.studies.states', [
    'ui.router', 'admin.studies.controllers', 'studies.services']);

  mod.config(['$stateProvider', 'userResolve', function($stateProvider, userResolve ) {

    /**
     * Studies - view all studies in panels
     */
    $stateProvider.state('admin.studies', {
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
    });

    /**
     * Studies - view all studies in a table
     */
    $stateProvider.state('admin.studies.table', {
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
    });

    /**
     * Study add
     */
    $stateProvider.state('admin.studies.add', {
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
    });

    /**
     * Study view
     */
    $stateProvider.state('admin.studies.study', {
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
    });

    /**
     * Study view summary information
     */
    $stateProvider.state('admin.studies.study.summary', {
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
    });

    /**
     * Study view participatns information
     */
    $stateProvider.state('admin.studies.study.participants', {
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
    });

    /**
     * Study view specimen information
     */
    $stateProvider.state('admin.studies.study.specimens', {
      url: '/specimens',
      resolve: {
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
    });

    /**
     * Study view collection information
     */
    $stateProvider.state('admin.studies.study.collection', {
      url: '/collection',
      resolve: {
        ceventTypes: [
          '$stateParams',
          'CollectionEventTypeService',
          function($stateParams, CollectionEventTypeService) {
            if ($stateParams.studyId) {
              return CollectionEventTypeService.getAll($stateParams.studyId).then(function(response) {
                return response.data;
              });
            }
            throw new Error("state parameter studyId is invalid");
          }],
        annotTypes: [
          '$stateParams',
          'CeventAnnotationTypeService',
          function($stateParams, CeventAnnotationTypeService) {
            if ($stateParams.studyId) {
              return CeventAnnotationTypeService.getAll($stateParams.studyId).then(function(response) {
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
          }],
        user: function() {
          return userResolve;
        }
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
