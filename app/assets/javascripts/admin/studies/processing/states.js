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
          '$stateParams', 'processingTypesService', 'study',
          function($stateParams, processingTypesService, study) {
            if ($stateParams.processingTypeId) {
              return processingTypesService.get(study.id, $stateParams.processingTypeId);
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
            valueType: '',
            options: []
          };
        }],
        addOrUpdateFn: ['spcLinkAnnotTypesService', function(spcLinkAnnotTypesService) {
          return spcLinkAnnotTypesService.addOrUpdate;
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
        displayName: 'Specimen Link Annotation Type'
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
          '$stateParams', 'spcLinkAnnotTypesService', 'study',
          function($stateParams, spcLinkAnnotTypesService, study) {
            if ($stateParams.annotTypeId) {
              return spcLinkAnnotTypesService.get(study.id, $stateParams.annotTypeId);
            }
            throw new Error('state parameter annotTypeId is invalid');
          }
        ],
        addOrUpdateFn: ['spcLinkAnnotTypesService', function(spcLinkAnnotTypesService) {
          return spcLinkAnnotTypesService.addOrUpdate;
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
        displayName: 'Specimen Link Annotation Type'
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
          'studiesService', 'study',
          function( studiesService, study) {
            return studiesService.processingDto(study.id);
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
          '$stateParams', 'spcLinkTypesService', 'study',
          function($stateParams, spcLinkTypesService) {
            return spcLinkTypesService.get($stateParams.procTypeId, $stateParams.spcLinkTypeId);
          }
        ],
        dtoProcessing: [
          'studiesService', 'study',
          function( studiesService, study) {
            return studiesService.processingDto(study.id);
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
