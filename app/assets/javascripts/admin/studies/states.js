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
        template: '<ui-view></ui-view>',
        resolve: userResolve,
        data: {
          breadcrumbProxy: 'admin.studies.panels'
        }
      })
      .state('admin.studies.panels', {
        url: '/',
        templateUrl: '/assets/javascripts/admin/studies/studiesPanels.html',
        resolve: userResolve,
        controller: 'StudiesCtrl',
        data: {
          displayName: 'Studies'
        }
      })
      .state('admin.studies.table', {
        url: '/',
        templateUrl: '/assets/javascripts/admin/studies/studiesTable.html',
        resolve: userResolve,
        controller: 'StudiesTableCtrl',
        data: {
          displayName: false
        }
      })
      .state('admin.studies.add', {
        url: '/edit',
        templateUrl: '/assets/javascripts/admin/studies/studyForm.html',
        controller: 'StudyEditCtrl',
        data: {
          displayName: 'Add Study'
        },
        resolve: userResolve
      })
      .state('admin.studies.study', {
        abstract: true,
        url: '/{studyId}',
        template: '<ui-view></ui-view>',
        data: {
          breadcrumbProxy: 'admin.studies.study.view'
        },
        resolve: {
          study: function($stateParams, studyService) {
            if ($stateParams.studyId)  {
              return studyService.query($stateParams.studyId).then(function(response) {
                return response.data;
              });
            }
            throw new Error("state parameter id is invalid");
          },
          userResolve: function($stateParams, studyService) {
            return userResolve;
          }
        }
      })
      .state('admin.studies.study.view', {
        url: '/view',
        templateUrl: '/assets/javascripts/admin/studies/studyView.html',
        controller: 'StudyViewCtrl',
        data: {
          displayName: '{{study.name}}'
        },
        resolve: userResolve
      })
      .state('admin.studies.study.update', {
        url: '/update',
        templateUrl: '/assets/javascripts/admin/studies/studyForm.html',
        controller: 'StudyEditCtrl',
        data: {
          displayName: '{{study.name}}'
        },
        resolve: userResolve
      })
      .state('admin.studies.error', {
        url: '/error',
        template: '<div><h1>Study does not exist</h1></div>',
        resolve: userResolve
      });
  });
  return mod;
});
