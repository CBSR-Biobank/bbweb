/**
 * Configure routes of user module.
 */
define(['angular'], function(angular) {
  'use strict';

  var mod = angular.module('admin.studies.participants.states', [
    'ui.router',
    'admin.studies.participants.controllers'
  ]);

  mod.config([
    '$urlRouterProvider', '$stateProvider', 'userResolve',
    function($urlRouterProvider, $stateProvider, userResolve ) {

    $urlRouterProvider.otherwise('/');

    /**
     * Prticipant Annotation Type Add
     */
    $stateProvider.state('admin.studies.study.participants.annotTypeAdd', {
      url: '/participant/annottype/add',
      resolve: {
        user: userResolve.user,
        annotType: ['study', function(study) {
          return {
            studyId: study.id,
            name: "",
            description: null,
            required: false,
            valueType: "",
            options: []
          };
        }]
      },
      views: {
        'main@': {
          templateUrl: '/assets/javascripts/admin/studies/annotTypeForm.html',
          controller: 'participantAnnotationTypeAddCtrl'
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
      url: '/participant/annottype/update/{annotTypeId}',
      resolve: {
        user: userResolve.user,
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
          }]
      },
      views: {
        'main@': {
          templateUrl: '/assets/javascripts/admin/studies/annotTypeForm.html',
          controller: 'participantAnnotationTypeUpdateCtrl'
        }
      },
      data: {
        displayName: 'Participant Annotation Type'
      }
    });

  }]);
  return mod;
});
