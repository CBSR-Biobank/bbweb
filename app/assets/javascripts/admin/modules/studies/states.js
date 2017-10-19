/**
 * Configure routes of studies module.
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(function () {
  'use strict';

  config.$inject = [ '$stateProvider' ];

  function config($stateProvider) {
    $stateProvider
      .state('home.admin.studies', {
        url: '/studies',
        views: {
          'main@': 'studiesAdmin'
        }
      })
      .state('home.admin.studies.add', {
        url: '/add',
        resolve: {
          study: ['Study', function(Study) {
            return new Study();
          }]
        },
        views: {
          'main@': 'studyAdd'
        }
      })
      .state('home.admin.studies.study', {
        abstract: true,
        url: '/{studyId}',
        resolve: {
          study: resolveStudy
        },
        views: {
          'main@': 'studyView'
        }
      })
      .state('home.admin.studies.study.summary', {
        url: '/summary',
        views: {
          'studyDetails': 'studySummary'
        }
      })
      .state('home.admin.studies.study.participants', {
        url: '/participants',
        views: {
          'studyDetails': 'studyParticipantsTab'
        }
      })
      .state('home.admin.studies.study.collection', {
        url: '/collection',
        views: {
          'studyDetails': 'studyCollection'
        }
      })
      .state('home.admin.studies.study.processing', {
        url: '/processing',
        views: {
          'studyDetails': 'studyProcessingTab'
        }
      });

    resolveStudy.$inject = ['$transition$', 'Study'];
    function resolveStudy($transition$, Study) {
      if ($transition$.params().studyId) {
        return Study.get($transition$.params().studyId);
      }
      throw new Error('state parameter studyId is invalid');
    }
  }

  return config;
});
