/**
 * Configure routes of specimen groups module.
 */
define(['angular'], function(angular) {
  'use strict';

  var mod = angular.module('admin.studies.specimenGroups.states', [
    'ui.router', 'admin.studies.controllers']);

  mod.config([
    '$urlRouterProvider', '$stateProvider', 'userResolve',
    function($urlRouterProvider, $stateProvider, userResolve ) {

    $urlRouterProvider.otherwise('/');

    $stateProvider
      .state('admin.studies.study.specimens.groupAdd', {
        url: '/spcgroup/add',
        resolve: {
          user: userResolve.user,
          specimenGroup: ['study', function(study) {
            return {
              studyId: study.id,
              name: '',
              description: null,
              units: '',
              anatomicalSourceType: '',
              preservationType: '',
              preservationTemperatureType: '',
              specimenType: ''
            };
          }]
        },
        views: {
          'main@': {
            templateUrl: '/assets/javascripts/admin/studies/specimenGroups/specimenGroupForm.html',
            controller: 'SpecimenGroupAddCtrl'
          }
        },
        data: {
          displayName: 'Specimen Group'
        }
      })
      .state('admin.studies.study.specimens.groupUpdate', {
        url: '/spcgroup/update/{specimenGroupId}',
        resolve: {
          user: userResolve.user,
          specimenGroup: [
            '$stateParams', 'SpecimenGroupService', 'study',
            function($stateParams, SpecimenGroupService, study) {
              if ($stateParams.specimenGroupId) {
                return SpecimenGroupService.get(study.id, $stateParams.specimenGroupId);
              }
              throw new Error('state parameter specimenGroupId is invalid');
            }]
        },
        views: {
          'main@': {
            templateUrl: '/assets/javascripts/admin/studies/specimenGroups/specimenGroupForm.html',
            controller: 'SpecimenGroupUpdateCtrl'
          }
        },
        data: {
          displayName: 'Specimen Group'
        }
      });
  }]);
  return mod;
});
