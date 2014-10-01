/**
 * Configure routes of user module.
 */
define(['../../module'], function(module) {
  'use strict';

  module.config(config);

  config.$inject = [
    '$urlRouterProvider', '$stateProvider', 'userResolve'
  ];

  function config($urlRouterProvider, $stateProvider, userResolve ) {

    $urlRouterProvider.otherwise('/');

    /**
     * Processing Type Add
     */
    $stateProvider.state('admin.studies.study.processing.processingTypeAdd', {
      url: '/proctypes/add',
      resolve: {
        user: userResolve.user,
        processingType: ['study', function(study) {
          return {
            studyId: study.id,
            name: '',
            description: null,
            enabled: false
          };
        }]
      },
      views: {
        'main@': {
          templateUrl: '/assets/javascripts/admin/studies/processing/processingTypeForm.html',
          controller: 'ProcessingTypeEditCtrl as vm'
        }
      },
      data: {
        displayName: 'Processing Type'
      }
    });

    /**
     * Processing Type Update
     */
    $stateProvider.state('admin.studies.study.processing.processingTypeUpdate', {
      url: '/proctypes/update/{processingTypeId}',
      resolve: {
        user: userResolve.user,
        processingType: [
          '$stateParams', 'ProcessingTypeService', 'study',
          function($stateParams, ProcessingTypeService, study) {
            if ($stateParams.processingTypeId) {
              return ProcessingTypeService.get(study.id, $stateParams.processingTypeId);
            }
            throw new Error('state parameter processingTypeId is invalid');
          }
        ]
      },
      views: {
        'main@': {
          templateUrl: '/assets/javascripts/admin/studies/processing/processingTypeForm.html',
          controller: 'ProcessingTypeEditCtrl as vm'
        }
      },
      data: {
        displayName: 'Processing Type'
      }
    });

    /**
     * Specimen Link Annotation Type Add
     */
    $stateProvider.state('admin.studies.study.processing.spcLinkAnnotTypeAdd', {
      url: '/annottype/add',
      resolve: {
        user: userResolve.user,
        annotType: ['study', function(study) {
          return {
            studyId: study.id,
            name: '',
            description: null,
            required: false,
            valueType: '',
            options: []
          };
        }],
        addOrUpdateFn: ['SpcLinkAnnotTypeService', function(SpcLinkAnnotTypeService) {
          return SpcLinkAnnotTypeService.addOrUpdate;
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
        displayName: 'Specimen Link Annotation Type',
        returnState: 'admin.studies.study.processing' // need to use state date since child state resolves inherit from parent states
      }
    });

    /**
     * Prticipant Annotation Type Update
     */
    $stateProvider.state('admin.studies.study.processing.spcLinkAnnotTypeUpdate', {
      url: '/annottype/update/{annotTypeId}',
      resolve: {
        user: userResolve.user,
        annotType: [
          '$stateParams', 'SpcLinkAnnotTypeService', 'study',
          function($stateParams, SpcLinkAnnotTypeService, study) {
            if ($stateParams.annotTypeId) {
              return SpcLinkAnnotTypeService.get(study.id, $stateParams.annotTypeId);
            }
            throw new Error('state parameter annotTypeId is invalid');
          }
        ],
        addOrUpdateFn: ['SpcLinkAnnotTypeService', function(SpcLinkAnnotTypeService) {
          return SpcLinkAnnotTypeService.addOrUpdate;
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
        displayName: 'Specimen Link Annotation Type',
        returnState: 'admin.studies.study.processing' // need to use state date since child state resolves inherit from parent states
      }
    });

    /**
     * Specimen Link Type Add
     */
    $stateProvider.state('admin.studies.study.processing.spcLinkTypeAdd', {
      url: '/sltype/add',
      resolve: {
        user: userResolve.user,
        spcLinkType: function() {
          return {
            processingTypeId:      null,
            inputGroupId:          null,
            outputGroupId:         null,
            inputContainerTypeId:  null,
            outputContainerTypeId: null,
            annotationTypeData:    []
          };
        },
        dtoProcessing: [
          'StudyService', 'study',
          function( StudyService, study) {
            return StudyService.processingDto(study.id);
          }
        ]
      },
      views: {
        'main@': {
          templateUrl: '/assets/javascripts/admin/studies/processing/spcLinkTypeForm.html',
          controller: 'SpcLinkTypeEditCtrl as vm'
        }
      },
      data: {
        displayName: 'Specimen Link Type'
      }
    });

    /**
     * Specimen Link Type Update
     */
    $stateProvider.state('admin.studies.study.processing.spcLinkTypeUpdate', {
      url: '/sltype/update/{procTypeId}/{spcLinkTypeId}',
      resolve: {
        user: userResolve.user,
        spcLinkType: [
          '$stateParams', 'SpcLinkTypeService', 'study',
          function($stateParams, SpcLinkTypeService) {
            return SpcLinkTypeService.get($stateParams.procTypeId, $stateParams.spcLinkTypeId);
          }
        ],
        dtoProcessing: [
          'StudyService', 'study',
          function( StudyService, study) {
            return StudyService.processingDto(study.id);
          }
        ]
      },
      views: {
        'main@': {
          templateUrl: '/assets/javascripts/admin/studies/processing/spcLinkTypeForm.html',
          controller: 'SpcLinkTypeEditCtrl as vm'
        }
      },
      data: {
        displayName: 'Specimen Link Type'
      }
    });

  }

});
