/**
 * Configure routes of user module.
 */
define(['angular'], function(angular) {
  'use strict';

  var mod = angular.module('study.states', ['ui.router', 'study.controllers', 'study.services']);
  mod.config(function($stateProvider, userResolve) {
    $stateProvider
      .state('admin.studies', {
        abstract: true,
        url: '/studies',
        templateUrl: '/assets/javascripts/admin/studies/studies.html',
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
      .state('admin.studies.view', {
        url: '/{id}',
        templateUrl: '/assets/javascripts/admin/studies/study.html',
        controller: 'StudyCtrl',
        data: {
          displayName: '{{study.name | uppercase}}'
        },
        resolve: {
          study: function($stateParams, studyService) {
            return studyService.query($stateParams.id).then(function(response) {
              return response.data;
            });
          },
          userResolve: function($stateParams, studyService) {
            return userResolve;
          }
        }
      })
      .state('admin.study.edit', {
        url: '/edit/:id?',
        templateUrl: '/assets/javascripts/admin/studies/studyForm.html',
        controller: 'StudyEditCtrl',
        resolve: userResolve
      })
      .state('admin.study.error', {
        url: '/error',
        template: '<div><h1>Study does not exist</h1></div>',
        resolve: userResolve
      })
      .state('admin.studies.partannot.edit', {
        url: '/partannot/edit/:id?',
        template: 'edit', //'/assets/javascripts/admin/studies/annotationTypeForm.html',
        controller: 'StudyAnnotationTypeEditCtrl',
        resolve: userResolve
      })
      .state('admin.studies.partannot.remove', {
        url: '/partannot/remove/:id',
        template: 'remove', //'/assets/javascripts/admin/studies/annotationTypeRemovescaForm.html',
        controller: 'StudyAnnotationTypeRemoveCtrl',
        resolve: userResolve
      });
  });
  return mod;
});
