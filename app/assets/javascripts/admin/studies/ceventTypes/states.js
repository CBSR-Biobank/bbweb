/**
 * Configure routes of specimen groups module.
 */
define(['../../module'], function(module) {
  'use strict';

  module.config(config);

  config.$inject = [
    '$urlRouterProvider',
    '$stateProvider',
    'authorizationProvider'
  ];

  function config($urlRouterProvider,
                  $stateProvider,
                  authorizationProvider
                 ) {

    resolveSpecimenGroups.$inject = ['specimenGroupsService', 'study'];
    function resolveSpecimenGroups(specimenGroupsService, study) {
      return specimenGroupsService.getAll(study.id);
    }

    resolveAnnotTypes.$inject = ['ceventAnnotTypesService', 'study'];
    function resolveAnnotTypes(ceventAnnotTypesService, study) {
      return ceventAnnotTypesService.getAll(study.id);
    }

    $urlRouterProvider.otherwise('/');

    /**
     * Collection Event Type Add
     */
    $stateProvider.state('home.admin.studies.study.collection.ceventTypeAdd', {
      url: '/cetypes/add',
      resolve: {
        user: authorizationProvider.requireAuthenticatedUser,
        ceventType: ['study', function(study) {
          return {
            studyId: study.id,
            name: '',
            description: null,
            recurring: false,
            specimenGroupData: [],
            annotationTypeData: []
          };
        }],
        annotTypes: resolveAnnotTypes,
        specimenGroups: resolveSpecimenGroups
      },
      views: {
        'main@': {
          templateUrl: '/assets/javascripts/admin/studies/ceventTypes/ceventTypeForm.html',
          controller: 'CeventTypeEditCtrl as vm'
        }
      },
      data: {
        displayName: 'Collection Event Type'
      }
    });

    /**
     * Collection Event Type Update
     */
    $stateProvider.state('home.admin.studies.study.collection.ceventTypeUpdate', {
      url: '/cetypes/update/{ceventTypeId}',
      resolve: {
        user: authorizationProvider.requireAuthenticatedUser,
        ceventType: [
          '$stateParams', 'ceventTypesService', 'study',
          function($stateParams, ceventTypesService, study) {
            if ($stateParams.ceventTypeId) {
              return ceventTypesService.get(study.id, $stateParams.ceventTypeId);
            }
            throw new Error('state parameter ceventTypeId is invalid');
          }
        ],
        annotTypes: resolveAnnotTypes,
        specimenGroups: resolveSpecimenGroups
      },
      views: {
        'main@': {
          templateUrl: '/assets/javascripts/admin/studies/ceventTypes/ceventTypeForm.html',
          controller: 'CeventTypeEditCtrl as vm'
        }
      },
      data: {
        displayName: 'Collection Event Type'
      }
    });

    /**
     * Collection Event Annotation Type Add
     */
    $stateProvider.state('home.admin.studies.study.collection.ceventAnnotTypeAdd', {
      url: '/cevent/annottype/add',
      resolve: {
        user: authorizationProvider.requireAuthenticatedUser,
        annotType: ['study', function(study) {
          return {
            studyId: study.id,
            name: '',
            description: null,
            valueType: '',
            options: []
          };
        }],
        addOrUpdateFn: ['ceventAnnotTypesService', function(ceventAnnotTypesService) {
          return ceventAnnotTypesService.addOrUpdate;
        }],
        valueTypes: ['studiesService', function(studiesService) {
          return studiesService.valueTypes();
        }]
      },
      views: {
        'main@': {
          templateUrl: '/assets/javascripts/admin/studies/annotationTypes/annotTypeForm.html',
          controller: 'AnnotationTypeEditCtrl as vm'
        }
      },
      data: {
        displayName: 'Collection Event Annotation Type'
      }
    });

    /**
     * Collection Event Annotation Type Update
     */
    $stateProvider.state('home.admin.studies.study.collection.ceventAnnotTypeUpdate', {
      url: '/cevent/annottype/update/{annotTypeId}',
      resolve: {
        user: authorizationProvider.requireAuthenticatedUser,
        annotType: [
          '$stateParams', 'ceventAnnotTypesService', 'study',
          function($stateParams, ceventAnnotTypesService, study) {
            if ($stateParams.annotTypeId) {
              return ceventAnnotTypesService.get(study.id, $stateParams.annotTypeId);
            }
            throw new Error('state parameter annotTypeId is invalid');
          }
        ],
        addOrUpdateFn: ['ceventAnnotTypesService', function(ceventAnnotTypesService) {
          return ceventAnnotTypesService.addOrUpdate;
        }],
        valueTypes: ['studiesService', function(studiesService) {
          return studiesService.valueTypes();
        }]
      },
      views: {
        'main@': {
          templateUrl: '/assets/javascripts/admin/studies/annotationTypes/annotTypeForm.html',
          controller: 'AnnotationTypeEditCtrl as vm'
        }
      },
      data: {
        displayName: 'Collection Event Annotation Type'
      }
    });

  }

});
