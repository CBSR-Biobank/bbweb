/**
 * Configure routes of user module.
 */
define(['angular'], function(angular) {
  'use strict';

  var mod = angular.module('admin.studies.states', ['ui.router', 'admin.studies.controllers', 'study.services']);
  mod.config(function($stateProvider, userResolve) {
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
            return null;
          },
          user: function() {
            return userResolve;
          }
        },
        views: {
          'main@': {
            templateUrl: '/assets/javascripts/admin/studies/studyForm.html',
            controller: 'StudyEditCtrl'
          }
        },
        data: {
          displayName: 'Add Study'
        }
      })
      .state('admin.studies.study', {
        url: '/{studyId}',
        resolve: {
          study: function($stateParams, studyService) {
            if ($stateParams.studyId)  {
              return studyService.query($stateParams.studyId).then(function(response) {
                return response.data;
              });
            }
            throw new Error("state parameter id is invalid");
          },
          user: function() {
            return userResolve;
          }
        },
        views: {
          'main@': {
            templateUrl: '/assets/javascripts/admin/studies/studyView.html',
            controller: 'StudyViewCtrl'
          }
        },
        data: {
          displayName: "{{study.name}}"
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
            controller: 'StudyEditCtrl'
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
  });
  return mod;
});
