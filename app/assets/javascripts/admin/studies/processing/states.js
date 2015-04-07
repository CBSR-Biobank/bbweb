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
        processingType: [
          '$stateParams', 'ProcessingType',
          function($stateParams, ProcessingType) {
            var pt = new ProcessingType();
            pt.studyId = $stateParams.studyId;
            return pt;
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
          '$stateParams', 'ProcessingType',
          function($stateParams, ProcessingType) {
            return ProcessingType.get($stateParams.studyId, $stateParams.processingTypeId);
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
    $stateProvider.state('home.admin.studies.study.processing.spcLinkAnnotationTypeAdd', {
      url: '/annottype/add',
      resolve: {
        user: authorizationProvider.requireAuthenticatedUser,
        annotationType: ['SpecimenLinkAnnotationType', function(SpecimenLinkAnnotationType) {
          return new SpecimenLinkAnnotationType();
        }]
      },
      views: {
        'main@': {
          templateUrl: '/assets/javascripts/admin/studies/annotationTypes/annotationTypeForm.html',
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
    $stateProvider.state('home.admin.studies.study.processing.spcLinkAnnotationTypeUpdate', {
      url: '/annottype/update/{annotationTypeId}',
      resolve: {
        user: authorizationProvider.requireAuthenticatedUser,
        annotationType: [
          '$stateParams', 'SpecimenLinkAnnotationType',
          function($stateParams, SpecimenLinkAnnotationType) {
            return SpecimenLinkAnnotationType.get($stateParams.studyId, $stateParams.annotationTypeId);
          }
        ]
      },
      views: {
        'main@': {
          templateUrl: '/assets/javascripts/admin/studies/annotationTypes/annotationTypeForm.html',
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
        spcLinkType: [
          'SpecimenLinkType',
          function(SpecimenLinkType) {
            return new SpecimenLinkType();
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
    $stateProvider.state('home.admin.studies.study.processing.spcLinkTypeUpdate', {
      url: '/sltype/update/{procTypeId}/{spcLinkTypeId}',
      resolve: {
        user: authorizationProvider.requireAuthenticatedUser,
        spcLinkType: [
          '$stateParams', 'SpecimenLinkType',
          function($stateParams, SpecimenLinkType) {
            return SpecimenLinkType.get($stateParams.procTypeId, $stateParams.spcLinkTypeId);
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
