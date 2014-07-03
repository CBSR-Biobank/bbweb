/**
 * Configure routes of specimen groups module.
 */
define(['angular'], function(angular) {
  'use strict';

  var mod = angular.module('admin.studies.specimenGroups.states', [
    'ui.router', 'admin.studies.controllers']);

  mod.config(['$stateProvider', 'userResolve', function($stateProvider, userResolve) {
    $stateProvider
      .state('admin.studies.study.specimenGroupAdd', {
        url: '/spcgroup/add',
        views: {
          'main@': {
            templateUrl: '/assets/javascripts/admin/studies/specimenGroups/specimenGroupForm.html',
            controller: 'SpecimenGroupAddCtrl'
          }
        },
        resolve: {
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
          }],
          user: function() {
            return userResolve;
          }
        },
        data: {
          displayName: 'Specimen Group'
        }
      })
      .state('admin.studies.study.specimenGroupUpdate', {
        url: '/spcgroup/update/{specimenGroupId}',
        resolve: {
          specimenGroup: [
            '$stateParams', 'SpecimenGroupService', 'study',
            function($stateParams, SpecimenGroupService, study) {
              if ($stateParams.specimenGroupId) {
                return SpecimenGroupService.get(study.id, $stateParams.specimenGroupId)
                  .then(function(response) {
                    return response.data;
                  });
              }
              throw new Error('state parameter specimenGroupId is invalid');
            }],
          user: function() {
            return userResolve;
          }
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
