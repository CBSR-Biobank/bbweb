/**
 * Configure routes of user module.
 */
define(['angular'], function(angular) {
  'use strict';

  var mod = angular.module('admin.studies.annotTypes.states', [
    'ui.router',
    'admin.studies.annotTypes.controllers'
  ]);

  mod.config(['$stateProvider', 'userResolve', function($stateProvider, userResolve) {
    $stateProvider
      .state('admin.studies.study.participantAnnotTypeAdd', {
        url: '/participant/annottype/add',
        views: {
          'main@': {
            templateUrl: '/assets/javascripts/admin/studies/annotTypes/annotTypeForm.html',
            controller: 'StudyAnnotationTypeAddCtrl'
          }
        },
        resolve: {
          annotType: ['study', function(study) {
            return {
              studyId: study.id,
              name: "",
              description: null,
              required: false,
              valueType: "",
              options: []
            };
          }],
          user: function() {
            return userResolve;
          }
        },
        data: {
          displayName: 'Participant Annotation Type'
        }
      })
      .state('admin.studies.study.participantAnnotTypeUpdate', {
        url: '/participant/annottype/update/{annotTypeId}',
        resolve: {
          annotType: [
            '$stateParams', 'ParticipantAnnotTypeService', 'study',
            function($stateParams, ParticipantAnnotTypeService, study) {
              if ($stateParams.annotTypeId) {
                return ParticipantAnnotTypeService.get(study.id, $stateParams.annotTypeId)
                  .then(function(response) {
                    return response.data;
                  });
              }
              throw new Error("state parameter annotTypeId is invalid");
            }],
          user: function() {
            return userResolve;
          }
        },
        views: {
          'main@': {
            templateUrl: '/assets/javascripts/admin/studies/annotTypes/annotTypeForm.html',
            controller: 'StudyAnnotationTypeUpdateCtrl'
          }
        },
        data: {
          displayName: 'Participant Annotation Type'
        }
      });
  }]);
  return mod;
});
