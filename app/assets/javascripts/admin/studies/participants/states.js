/**
 * Configure routes of user module.
 */
define(['angular'], function(angular) {
  'use strict';

  var mod = angular.module('admin.studies.participants.states', [
    'ui.router'
  ]);

  mod.config([
    '$urlRouterProvider', '$stateProvider', 'userResolve',
    function($urlRouterProvider, $stateProvider, userResolve ) {

      $urlRouterProvider.otherwise('/');

      /**
       * Prticipant Annotation Type Add
       */
      $stateProvider.state('admin.studies.study.participants.annotTypeAdd', {
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
          returnState: function() {
            return 'admin.studies.study.participants';
          },
          addOrUpdateFn: ['ParticipantAnnotTypeService', function(ParticipantAnnotTypeService) {
            return ParticipantAnnotTypeService.addOrUpdate;
          }]
        },
        views: {
          'main@': {
            templateUrl: '/assets/javascripts/admin/studies/annotationTypes/annotTypeForm.html',
            controller: 'StudyAnnotationTypeEditCtrl'
          }
        },
        data: {
          displayName: 'Participant Annotation Type'
        }
      });

      /**
       * Prticipant Annotation Type Update
       */
      $stateProvider.state('admin.studies.study.participants.annotTypeUpdate', {
        url: '/annottype/update/{annotTypeId}',
        resolve: {
          user: userResolve.user,
          annotType: [
            '$stateParams', 'ParticipantAnnotTypeService', 'study',
            function($stateParams, ParticipantAnnotTypeService, study) {
              if ($stateParams.annotTypeId) {
                return ParticipantAnnotTypeService.get(study.id, $stateParams.annotTypeId);
              }
              throw new Error('state parameter annotTypeId is invalid');
            }
          ],
          returnState: function() {
            return 'admin.studies.study.participants';
          },
          addOrUpdateFn: ['ParticipantAnnotTypeService', function(ParticipantAnnotTypeService) {
            return ParticipantAnnotTypeService.addOrUpdate;
          }]
        },
        views: {
          'main@': {
            templateUrl: '/assets/javascripts/admin/studies/annotationTypes/annotTypeForm.html',
            controller: 'StudyAnnotationTypeEditCtrl'
          }
        },
        data: {
          displayName: 'Participant Annotation Type'
        }
      });
    }
  ]);

  return mod;
});
