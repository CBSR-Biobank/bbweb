/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(function () {
  'use strict';

  config.$inject = [
    '$urlRouterProvider',
    '$stateProvider'
  ];

  function config($urlRouterProvider, $stateProvider) {

    $urlRouterProvider.otherwise('/');

    /**
     * Processing Type Add
     */
    $stateProvider.state('home.admin.studies.study.processing.processingTypeAdd', {
      url: '/proctypes/add',
      resolve: {
        processingType: [
          '$transition$', 'ProcessingType',
          function($transition$, ProcessingType) {
            var pt = new ProcessingType();
            pt.studyId = $transition$.params().studyId;
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
        processingType: [
          '$transition$', 'ProcessingType',
          function($transition$, ProcessingType) {
            return ProcessingType.get($transition$.params().studyId, $transition$.params().processingTypeId);
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
        annotationType: [
          '$transition$', 'SpecimenLinkAnnotationType',
          function($transition$, SpecimenLinkAnnotationType) {
            return SpecimenLinkAnnotationType.get($transition$.params().studyId, $transition$.params().annotationTypeId);
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
        spcLinkType: [
          '$transition$', 'SpecimenLinkType',
          function($transition$, SpecimenLinkType) {
            return SpecimenLinkType.get($transition$.params().procTypeId, $transition$.params().spcLinkTypeId);
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
