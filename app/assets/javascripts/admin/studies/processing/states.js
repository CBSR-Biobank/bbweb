/**
 * Configure routes of user module.
 */
define(['angular'], function(angular) {
  'use strict';

  var mod = angular.module('admin.studies.processing.states', [
    'ui.router',
    'admin.studies.processing.controllers'
  ]);

  mod.config([
    '$urlRouterProvider', '$stateProvider', 'userResolve',
    function($urlRouterProvider, $stateProvider, userResolve ) {

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
            controller: 'ProcessingTypeAddCtrl'
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
            controller: 'ProcessingTypeUpdateCtrl'
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
        url: '/processing/annottype/add',
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
          }]
        },
        views: {
          'main@': {
            templateUrl: '/assets/javascripts/admin/studies/annotTypeForm.html',
            controller: 'spcLinkAnnotationTypeAddCtrl'
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
        url: '/processing/annottype/update/{annotTypeId}',
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
          ]
        },
        views: {
          'main@': {
            templateUrl: '/assets/javascripts/admin/studies/annotTypeForm.html',
            controller: 'spcLinkAnnotationTypeUpdateCtrl'
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
          }
        },
        views: {
          'main@': {
            templateUrl: '/assets/javascripts/admin/studies/processing/spcLinkTypeForm.html',
            controller: 'SpcLinkTypeAddCtrl'
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
          ]
        },
        views: {
          'main@': {
            templateUrl: '/assets/javascripts/admin/studies/processing/spcLinkTypeForm.html',
            controller: 'SpcLinkTypeUpdateCtrl'
          }
        },
        data: {
          displayName: 'Specimen Link Type'
        }
      });

    }
  ]);
  return mod;
});
