/**
 * Configure routes of specimen groups module.
 */
define(['angular'], function(angular) {
  'use strict';

  var mod = angular.module('admin.studies.ceventTypes.states', [
    'ui.router', 'admin.studies.controllers']);

  mod.config([
    '$urlRouterProvider', '$stateProvider', 'userResolve',
    function($urlRouterProvider, $stateProvider, userResolve ) {

      $urlRouterProvider.otherwise('/');

      /**
       * Collection Event Type Add
       */
      $stateProvider.state('admin.studies.study.collection.ceventTypeAdd', {
        url: '/cetypes/add',
        resolve: {
          user: userResolve.user,
          ceventType: ['study', function(study) {
            return {
              studyId: study.id,
              name: '',
              description: null,
              recurring: false,
              specimenGroupData: [],
              annotationTypeData: []
            };
          }]
        },
        views: {
          'main@': {
            templateUrl: '/assets/javascripts/admin/studies/ceventTypes/ceventTypeForm.html',
            controller: 'CeventTypeAddCtrl'
          }
        },
        data: {
          displayName: 'Collection Event Type'
        }
      });

      /**
       * Collection Event Type Update
       */
      $stateProvider.state('admin.studies.study.collection.ceventTypeUpdate', {
        url: '/cetypes/update/{ceventTypeId}',
        resolve: {
          user: userResolve.user,
          ceventType: [
            '$stateParams', 'CeventTypeService', 'study',
            function($stateParams, CeventTypeService, study) {
              if ($stateParams.ceventTypeId) {
                return CeventTypeService.get(study.id, $stateParams.ceventTypeId);
              }
              throw new Error('state parameter ceventTypeId is invalid');
            }]
        },
        views: {
          'main@': {
            templateUrl: '/assets/javascripts/admin/studies/ceventTypes/ceventTypeForm.html',
            controller: 'CeventTypeUpdateCtrl'
          }
        },
        data: {
          displayName: 'Collection Event Type'
        }
      });

      /**
       * Collection Event Annotation Type Add
       */
      $stateProvider.state('admin.studies.study.collection.ceventAnnotTypeAdd', {
        url: '/cevent/annottype/add',
        resolve: {
          user: userResolve.user,
          annotType: ['study', function(study) {
            return {
              studyId: study.id,
              name: "",
              description: null,
              valueType: "",
              options: []
            };
          }]
        },
        views: {
          'main@': {
            templateUrl: '/assets/javascripts/admin/studies/annotTypeForm.html',
            controller: 'ceventAnnotationTypeAddCtrl'
          }
        },
        data: {
          displayName: 'Collection Event Annotation Type'
        }
      });

      /**
       * Collection Event Annotation Type Update
       */
      $stateProvider.state('admin.studies.study.collection.ceventAnnotTypeUpdate', {
        url: '/cevent/annottype/update/{annotTypeId}',
        resolve: {
          user: userResolve.user,
          annotType: [
            '$stateParams', 'CeventAnnotTypeService', 'study',
            function($stateParams, CeventAnnotTypeService, study) {
              if ($stateParams.annotTypeId) {
                return CeventAnnotTypeService.get(study.id, $stateParams.annotTypeId);
              }
              throw new Error("state parameter annotTypeId is invalid");
            }]
        },
        views: {
          'main@': {
            templateUrl: '/assets/javascripts/admin/studies/annotTypeForm.html',
            controller: 'ceventAnnotationTypeUpdateCtrl'
          }
        },
        data: {
          displayName: 'Collection Event Annotation Type'
        }
      });

    }]);
  return mod;
});
