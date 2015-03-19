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

  function config($urlRouterProvider, $stateProvider, authorizationProvider) {
    $urlRouterProvider.otherwise('/');

    /**
     * Prticipant Annotation Type Add
     */
    $stateProvider.state('home.admin.studies.study.participants.annotTypeAdd', {
      url: '/annottype/add',
      resolve: {
        user: authorizationProvider.requireAuthenticatedUser,
        annotType: ['ParticipantAnnotationType', function(ParticipantAnnotationType) {
          return new ParticipantAnnotationType();
        }]
      },
      views: {
        'main@': {
          templateUrl: '/assets/javascripts/admin/studies/annotationTypes/annotTypeForm.html',
          controller: 'AnnotationTypeEditCtrl as vm'
        }
      },
      data: {
        displayName: 'Participant Annotation Type'
      }
    });

    /**
     * Prticipant Annotation Type Update
     */
    $stateProvider.state('home.admin.studies.study.participants.annotTypeUpdate', {
      url: '/annottype/update/{annotTypeId}',
      resolve: {
        user: authorizationProvider.requireAuthenticatedUser,
        annotType: [
          '$stateParams',
          'ParticipantAnnotationType',
          function($stateParams, ParticipantAnnotationType) {
            return ParticipantAnnotationType.get($stateParams.studyId,
                                                 $stateParams.annotTypeId);
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
        displayName: 'Participant Annotation Type'
      }
    });
  }

  return config;
});
