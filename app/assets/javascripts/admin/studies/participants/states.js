/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(function () {
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
    $stateProvider.state('home.admin.studies.study.participants.annotationTypeAdd', {
      url: '/annottype/add',
      resolve: {
        user: authorizationProvider.requireAuthenticatedUser,
        annotationType: ['ParticipantAnnotationType', function(ParticipantAnnotationType) {
          return new ParticipantAnnotationType();
        }]
      },
      views: {
        'main@': {
          templateUrl: '/assets/javascripts/admin/studies/annotationTypes/annotationTypeForm.html',
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
    $stateProvider.state('home.admin.studies.study.participants.annotationTypeUpdate', {
      url: '/annottype/update/{annotationTypeId}',
      resolve: {
        user: authorizationProvider.requireAuthenticatedUser,
        annotationType: [
          '$stateParams',
          'ParticipantAnnotationType',
          function($stateParams, ParticipantAnnotationType) {
            return ParticipantAnnotationType.get($stateParams.studyId,
                                                 $stateParams.annotationTypeId);
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
        displayName: 'Participant Annotation Type'
      }
    });
  }

  return config;
});
