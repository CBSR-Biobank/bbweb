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

    resolveStudy.$inject = ['$stateParams', 'studiesService'];
    function resolveStudy($stateParams, studiesService) {
      if ($stateParams.studyId) {
        return studiesService.get($stateParams.studyId);
      }
      throw new Error('state parameter studyId is invalid');
    }

    resolveParticipant.$inject = ['$stateParams', 'participantsService'];
    function resolveParticipant($stateParams, participantsService) {
      if ($stateParams.studyId) {
        if ($stateParams.participantId) {
          return participantsService.get($stateParams.studyId, $stateParams.participantId);
        }
        throw new Error('state parameter participantId is invalid');
      }
      throw new Error('state parameter studyId is invalid');
    }

    resolveParticipantByUniqueId.$inject = ['$stateParams', 'participantsService'];
    function resolveParticipantByUniqueId($stateParams, participantsService) {
      if ($stateParams.studyId) {
        if ($stateParams.uniqueId) {
          return participantsService.getByUniqueId($stateParams.studyId, $stateParams.uniqueId);
        }
        throw new Error('state parameter uniqueId is invalid');
      }
      throw new Error('state parameter studyId is invalid');
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
        participant: function () { return {}; }
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
        displayName: '{{participant.uniqueId}}'
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
