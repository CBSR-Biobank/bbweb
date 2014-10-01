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
                  //CeventAnnotTypeService
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
          'CeventAnnotTypeService', 'study',
          function(CeventAnnotTypeService, study) {
            return CeventAnnotTypeService.getAll(study.id);
          }
        ],
        specimenGroups: [
          'SpecimenGroupService', 'study',
          function(SpecimenGroupService, study) {
            return SpecimenGroupService.getAll(study.id);
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
          '$stateParams', 'CeventTypeService', 'study',
          function($stateParams, CeventTypeService, study) {
            if ($stateParams.ceventTypeId) {
              return CeventTypeService.get(study.id, $stateParams.ceventTypeId);
            }
            throw new Error('state parameter ceventTypeId is invalid');
          }
        ],
        annotTypes: [
          'CeventAnnotTypeService', 'study',
          function(CeventAnnotTypeService, study) {
            return CeventAnnotTypeService.getAll(study.id);
          }
        ],
        specimenGroups: [
          'SpecimenGroupService', 'study',
          function(SpecimenGroupService, study) {
            return SpecimenGroupService.getAll(study.id);
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
        addOrUpdateFn: ['CeventAnnotTypeService', function(CeventAnnotTypeService) {
          return CeventAnnotTypeService.addOrUpdate;
        }],
        valueTypes: ['StudyAnnotTypeService', function(StudyAnnotTypeService) {
          return StudyAnnotTypeService.valueTypes();
        }]
      },
      views: {
        'main@': {
          templateUrl: '/assets/javascripts/admin/studies/annotationTypes/annotTypeForm.html',
          controller: 'AnnotationTypeEditCtrl as vm'
        }
      },
      data: {
        displayName: 'Collection Event Annotation Type',
        returnState: 'admin.studies.study.collection'
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
            throw new Error('state parameter annotTypeId is invalid');
          }
        ],
        addOrUpdateFn: ['CeventAnnotTypeService', function(CeventAnnotTypeService) {
          return CeventAnnotTypeService.addOrUpdate;
        }],
        valueTypes: ['StudyAnnotTypeService', function(StudyAnnotTypeService) {
          return StudyAnnotTypeService.valueTypes();
        }]
      },
      views: {
        'main@': {
          templateUrl: '/assets/javascripts/admin/studies/annotationTypes/annotTypeForm.html',
          controller: 'AnnotationTypeEditCtrl as vm'
        }
      },
      data: {
        displayName: 'Collection Event Annotation Type',
        returnState: 'admin.studies.study.collection'
      }
    });

  }

});
