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
      .state('admin.studies.study.participantAnnotTypeAdd', {
        url: '/participant/annottype/add',
        views: {
          'main@': {
            templateUrl: '/assets/javascripts/admin/studies/annotTypes/annotTypeForm.html',
            controller: 'StudyAnnotationTypeEditCtrl'
          }
        },
        data: {
          displayName: 'Participant Annotation Type'
        },
        resolve: {
          annotType: function() {
            return null;
          },
          user: function() {
            return userResolve;
          }
        }
      })
      .state('admin.studies.study.participantAnnotTypeUpdate', {
        url: '/participant/annottype/update/{annotTypeId}',
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
          user: function() {
            return userResolve;
          }
        },
        views: {
          'main@': {
            templateUrl: '/assets/javascripts/admin/studies/annotTypes/annotTypeForm.html',
            controller: 'StudyAnnotationTypeEditCtrl'
          }
        },
        data: {
          displayName: 'Participant Annotation Type'
        }
      })
      .state('admin.studies.study.participantAnnotTypeRemove', {
        url: '/participant/annottype/remove/{annotTypeId}',
        views: {
          'main@': {
            template: 'remove', //'/assets/javascripts/admin/studies/annotationTypeRemovescaForm.html',
            controller: 'StudyAnnotationTypeRemoveCtrl'
          }
        },
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
