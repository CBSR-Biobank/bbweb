/**
 * Configure states of specimen groups module.
 */
define(['../../module'], function(module) {
  'use strict';

  module.config(config);

  config.$inject = [
    '$urlRouterProvider', '$stateProvider', 'authorizationProvider'
  ];

  function config($urlRouterProvider, $stateProvider, authorizationProvider ) {
    // FIXME does this need to be in each state definition file?
    $urlRouterProvider.otherwise('/');

    /**
     * Used to add a specimen group.
     */
    $stateProvider.state('home.admin.studies.study.specimens.groupAdd', {
      url: '/spcgroup/add',
      resolve: {
        user: authorizationProvider.requireAuthenticatedUser,
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
        valueTypes: ['specimenGroupsService', function(specimenGroupsService) {
          return specimenGroupsService.specimenGroupValueTypes();
        }]
      },
      views: {
        'main@': {
          templateUrl: '/assets/javascripts/admin/studies/specimenGroups/specimenGroupForm.html',
          controller: 'SpecimenGroupEditCtrl as vm'
        }
      },
      data: {
        displayName: 'Specimen Group'
      }
    });

    /**
     * Used to update a specimen group.
     */
    $stateProvider.state('home.admin.studies.study.specimens.groupUpdate', {
      url: '/spcgroup/update/{specimenGroupId}',
      resolve: {
        user: authorizationProvider.requireAuthenticatedUser,
        specimenGroup: [
          '$stateParams', 'specimenGroupsService', 'study',
          function($stateParams, specimenGroupsService, study) {
            if ($stateParams.specimenGroupId) {
              return specimenGroupsService.get(study.id, $stateParams.specimenGroupId);
            }
            throw new Error('state parameter specimenGroupId is invalid');
          }
        ],
        valueTypes: ['specimenGroupsService', function(specimenGroupsService) {
          return specimenGroupsService.specimenGroupValueTypes();
        }]
      },
      views: {
        'main@': {
          templateUrl: '/assets/javascripts/admin/studies/specimenGroups/specimenGroupForm.html',
          controller: 'SpecimenGroupEditCtrl as vm'
        }
      },
      data: {
        displayName: 'Specimen Group'
      }
    });
  }
});
