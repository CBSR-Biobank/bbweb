/**
 * Configure routes of user module.
 */
define([], function() {
  'use strict';

  config.$inject = [
    '$urlRouterProvider',
    '$stateProvider',
    'authorizationProvider'
  ];

  function config($urlRouterProvider, $stateProvider, authorizationProvider ) {

    $urlRouterProvider.otherwise('/');

    /**
     * Processing Type Add
     */
    $stateProvider.state('home.admin.studies.study.processing.processingTypeAdd', {
      url: '/proctypes/add',
      resolve: {
        user: authorizationProvider.requireAuthenticatedUser,
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
    $stateProvider.state('home.admin.studies.study.processing.processingTypeUpdate', {
      url: '/proctypes/update/{processingTypeId}',
      resolve: {
        user: authorizationProvider.requireAuthenticatedUser,
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
    $stateProvider.state('home.admin.studies.study.processing.spcLinkAnnotTypeAdd', {
      url: '/annottype/add',
      resolve: {
        user: authorizationProvider.requireAuthenticatedUser,
        annotType: ['SpecimenLinkAnnotationType', function(SpecimenLinkAnnotationType) {
          return new SpecimenLinkAnnotationType();
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
    $stateProvider.state('home.admin.studies.study.processing.spcLinkAnnotTypeUpdate', {
      url: '/annottype/update/{annotTypeId}',
      resolve: {
        user: authorizationProvider.requireAuthenticatedUser,
        annotType: [
          '$stateParams', 'SpecimenLinkAnnotationType',
          function($stateParams, SpecimenLinkAnnotationType) {
            return SpecimenLinkAnnotationType.get($stateParams.studyId, $stateParams.annotTypeId);
          }
        ]
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
    $stateProvider.state('home.admin.studies.study.processing.spcLinkTypeAdd', {
      url: '/sltype/add',
      resolve: {
        user: authorizationProvider.requireAuthenticatedUser,
        spcLinkType: function() {
          return {
            processingTypeId:      null,
            inputGroupId:          null,
            outputGroupId:         null,
            inputContainerTypeId:  null,
            outputContainerTypeId: null,
            annotationTypeData:    []
          };
        }
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
    $stateProvider.state('home.admin.studies.study.processing.spcLinkTypeUpdate', {
      url: '/sltype/update/{procTypeId}/{spcLinkTypeId}',
      resolve: {
        user: authorizationProvider.requireAuthenticatedUser,
        spcLinkType: [
          '$stateParams', 'spcLinkTypesService', 'study',
          function($stateParams, spcLinkTypesService) {
            return spcLinkTypesService.get($stateParams.procTypeId, $stateParams.spcLinkTypeId);
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

  return config;
});
