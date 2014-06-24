/**
 * Configure routes of user module.
 */
define(['angular'], function(angular) {
  'use strict';

  var mod = angular.module('admin.studies.annotTypes.states', [
    'ui.router',
    'admin.studies.annotTypes.controllers'
  ]);
  mod.config(function($stateProvider, userResolve) {
    $stateProvider
      .state('admin.studies.participantAnnotType', {
        url: '/{studyId}/partannottype',
        template: '<ui-view></ui-view>',
        data: {
          displayName: '{{study.name}}'
        },
        resolve: {
          study: function($stateParams, studyService) {
            if ($stateParams.studyId)  {
              return studyService.query($stateParams.studyId).then(function(response) {
                return response.data;
              });
            }
            throw new Error("state parameter id is invalid");
          },
          userResolve: function($stateParams, studyService) {
            return userResolve;
          }
        }
      })
      .state('admin.studies.participantAnnotType.add', {
        url: '/add',
        templateUrl: '/assets/javascripts/admin/studies/annotTypes/annotTypeForm.html',
        controller: 'StudyAnnotationTypeEditCtrl',
        data: {
          displayName: 'Participant Annotation Type'
        },
        resolve: userResolve
      })
      .state('admin.studies.participantAnnotType.update', {
        url: '/update/{annotTypeId}',
        templateUrl: '/assets/javascripts/admin/studies/annotTypes/annotTypeForm.html',
        controller: 'StudyAnnotationTypeEditCtrl',
        data: {
          displayName: 'Participant Annotation Type'
        },
        resolve: {
          annotType: function($stateParams, studyService, study) {
            if ($stateParams.annotTypeId) {
              return studyService.participantAnnotType(study.id, $stateParams.annotTypeId)
                .then(function(response) {
                  return response.data;
                });
            }
            throw new Error("state parameter annotTypeId is invalid");
          },
          userResolve: function($stateParams, studyService) {
            return userResolve;
          }
        }
      })
      .state('admin.studies.participantAnnotType.remove', {
        url: '/remove/{annotTypeId}',
        template: 'remove', //'/assets/javascripts/admin/studies/annotationTypeRemovescaForm.html',
        controller: 'StudyAnnotationTypeRemoveCtrl',
        data: {
          displayName: 'Studies'
        },
        resolve: {
          annotType: function($stateParams, studyService, study) {
            if ($stateParams.annotTypeId) {
              return studyService.participantAnnotType(study.id, $stateParams.annotTypeId)
                .then(function(response) {
                  return response.data;
                });
            }
            throw new Error("annotation type id is null");
          },
          userResolve: function($stateParams, studyService) {
            return userResolve;
          }
        }
      });
  });
  return mod;
});
