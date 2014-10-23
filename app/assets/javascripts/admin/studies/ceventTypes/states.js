/**
 * Configure routes of specimen groups module.
 */
define(['../../module'], function(module) {
  'use strict';

  module.config(config);

  config.$inject = [
    '$urlRouterProvider',
    '$stateProvider',
    'userResolve'
  ];

  function config($urlRouterProvider,
                  $stateProvider,
                  userResolve
                  //ceventAnnotTypesService
                 ) {

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
        }],
        annotTypes: [
          'ceventAnnotTypesService', 'study',
          function(ceventAnnotTypesService, study) {
            return ceventAnnotTypesService.getAll(study.id);
          }
        ],
        specimenGroups: [
          'specimenGroupsService', 'study',
          function(specimenGroupsService, study) {
            return specimenGroupsService.getAll(study.id);
          }
        ]
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
    $stateProvider.state('admin.studies.study.collection.ceventTypeUpdate', {
      url: '/cetypes/update/{ceventTypeId}',
      resolve: {
        user: userResolve.user,
        ceventType: [
          '$stateParams', 'ceventTypesService', 'study',
          function($stateParams, ceventTypesService, study) {
            if ($stateParams.ceventTypeId) {
              return ceventTypesService.get(study.id, $stateParams.ceventTypeId);
            }
            throw new Error('state parameter ceventTypeId is invalid');
          }
        ],
        annotTypes: [
          'ceventAnnotTypesService', 'study',
          function(ceventAnnotTypesService, study) {
            return ceventAnnotTypesService.getAll(study.id);
          }
        ],
        specimenGroups: [
          'specimenGroupsService', 'study',
          function(specimenGroupsService, study) {
            return specimenGroupsService.getAll(study.id);
          }
        ]
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
    $stateProvider.state('admin.studies.study.collection.ceventAnnotTypeAdd', {
      url: '/cevent/annottype/add',
      resolve: {
        user: userResolve.user,
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
        valueTypes: ['studyAnnotTypesService', function(studyAnnotTypesService) {
          return studyAnnotTypesService.valueTypes();
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
    $stateProvider.state('admin.studies.study.collection.ceventAnnotTypeUpdate', {
      url: '/cevent/annottype/update/{annotTypeId}',
      resolve: {
        user: userResolve.user,
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
        valueTypes: ['studyAnnotTypesService', function(studyAnnotTypesService) {
          return studyAnnotTypesService.valueTypes();
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
