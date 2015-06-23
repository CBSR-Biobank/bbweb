/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
/* global define */

/**
 * collection routes.
 */
define([], function() {
  'use strict';

  config.$inject = ['$urlRouterProvider', '$stateProvider', 'authorizationProvider'];

  function config($urlRouterProvider, $stateProvider, authorizationProvider) {

    resolveStudyCounts.$inject = ['studiesService'];
    function resolveStudyCounts(studiesService) {
      return studiesService.getStudyCounts();
    }

    resolveStudy.$inject = ['$stateParams', 'Study'];
    function resolveStudy($stateParams, Study) {
      return Study.get($stateParams.studyId);
    }

    resolveParticipant.$inject = ['$stateParams', 'Participant', 'study', 'annotationTypes'];
    function resolveParticipant($stateParams, Participant, study, annotationTypes) {
      return Participant.get($stateParams.studyId, $stateParams.participantId).then(
        function (p) {
          p.setStudy(study);
          p.setAnnotationTypes(annotationTypes);
          return p;
        });
    }

    resolveParticipantByUniqueId.$inject = ['$stateParams', 'Participant'];
    function resolveParticipantByUniqueId($stateParams, Participant) {
      return Participant.getByUniqueId($stateParams.studyId, $stateParams.uniqueId);
    }

    resolveAnnotationTypes.$inject = ['$stateParams', 'ParticipantAnnotationType'];
    function resolveAnnotationTypes($stateParams, ParticipantAnnotationType) {
      if ($stateParams.studyId) {
        return ParticipantAnnotationType.list($stateParams.studyId);
      }
      throw new Error('state parameter studyId is invalid');
    }

    $urlRouterProvider.otherwise('/');

    $stateProvider.state('home.collection', {
      url: '^/collection',
      resolve: {
        user: authorizationProvider.requireAuthenticatedUser,
        studyCounts: resolveStudyCounts
      },
      views: {
        'main@': {
          templateUrl: '/assets/javascripts/collection/collection.html',
          controller: 'CollectionCtrl as vm'
        }
      },
      data: {
        displayName: 'Collection'
      }
    });

    $stateProvider.state('home.collection.study', {
      url: '/{studyId}',
      resolve: {
        user: authorizationProvider.requireAuthenticatedUser,
        study: resolveStudy
      },
      views: {
        'main@': {
          templateUrl: '/assets/javascripts/collection/studyView.html',
          controller: 'CollectionStudyCtrl as vm'
        }
      },
      data: {
        displayName: '{{study.name}}'
      }
    });

    $stateProvider.state('home.collection.study.addParticipant', {
      url: '/add/{uniqueId}',
      resolve: {
        user: authorizationProvider.requireAuthenticatedUser,
        annotationTypes: resolveAnnotationTypes,
        participant: [
          '$stateParams',
          'Participant',
          'study',
          'annotationTypes',
          function ($stateParams, Participant, study, annotationTypes) {
            return new Participant({uniqueId: $stateParams.uniqueId}, study, annotationTypes) ;
          }
        ]
      },
      views: {
        'main@': {
          templateUrl: '/assets/javascripts/collection/participantForm.html',
          controller: 'ParticipantEditCtrl as vm'
        }
      },
      data: {
        displayName: 'Add participant'
      }
    });

    $stateProvider.state('home.collection.study.participant', {
      url: '/{participantId}',
      resolve: {
        user: authorizationProvider.requireAuthenticatedUser,
        annotationTypes: resolveAnnotationTypes,
        participant: resolveParticipant
      },
      views: {
        'main@': {
          templateUrl: '/assets/javascripts/collection/participant.html',
          controller: 'ParticipantCtrl as vm'
        }
      },
      data: {
        displayName: 'Participant {{participant.uniqueId}}'
      }
    });

    $stateProvider.state('home.collection.study.participant.update', {
      url: '/update',
      resolve: {
        user: authorizationProvider.requireAuthenticatedUser
      },
      views: {
        'main@': {
          templateUrl: '/assets/javascripts/collection/participantForm.html',
          controller: 'ParticipantEditCtrl as vm'
        }
      },
      data: {
        displayName: 'Update participant'
      }
    });

  }

  return config;
});
