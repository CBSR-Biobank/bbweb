/**
 * Configure routes of studies module.
 */
define(['../module'], function(module) {
  'use strict';

  module.config(config);

  config.$inject = [
    '$urlRouterProvider', '$stateProvider', 'userResolve'
  ];

  function config($urlRouterProvider, $stateProvider, userResolve ) {

    resolveStudy.$inject = ['$stateParams', 'studiesService'];
    function resolveStudy($stateParams, studiesService) {
      if ($stateParams.studyId) {
        return studiesService.get($stateParams.studyId);
      }
      throw new Error('state parameter studyId is invalid');
    }

    resolveStudyCounts.$inject = ['studiesService'];
    function resolveStudyCounts(studiesService) {
      return studiesService.getStudyCounts();
    }

    $urlRouterProvider.otherwise('/');

    /**
     * Studies - view all studies
     *
     */
    $stateProvider.state('admin.studies', {
      url: '/studies',
      resolve: {
        user: userResolve.user,
        studyCounts: resolveStudyCounts
      },
      views: {
        'main@': {
          templateUrl: '/assets/javascripts/admin/studies/studies.html',
          controller: 'StudiesCtrl as vm'
        }
      },
      data: {
        displayName: 'Studies'
      }
    });

    /**
     * Study add
     */
    $stateProvider.state('admin.studies.add', {
      url: '/add',
      resolve: {
        user: userResolve.user,
        study: function() {
          return { name: '', description: null };
        }
      },
      views: {
        'main@': {
          templateUrl: '/assets/javascripts/admin/studies/studyForm.html',
          controller: 'StudyEditCtrl as vm'
        }
      },
      data: {
        displayName: 'Add Study'
      }
    });

    /**
     * Study view
     */
    $stateProvider.state('admin.studies.study', {
      abstract: true,
      url: '/{studyId}',
      resolve: {
        user: userResolve.user
      },
      views: {
        'main@': {
          templateUrl: '/assets/javascripts/admin/studies/studyView.html',
          controller: 'StudyCtrl as vm'
        }
      },
      data: {
        breadcrumProxy: 'admin.studies.study.summary'
      }
    });

    /**
     * Study view summary information
     */
    $stateProvider.state('admin.studies.study.summary', {
      url: '/summary',
      resolve: {
        user: userResolve.user,
        study: resolveStudy
      },
      views: {
        'studyDetails': {
          templateUrl: '/assets/javascripts/admin/studies/studySummaryTab.html',
          controller: 'StudySummaryTabCtrl as vm'
        }
      },
      data: {
        displayName: '{{study.name}}'
      }
    });

    /**
     * Study summary information update
     */
    $stateProvider.state('admin.studies.study.summary.update', {
      url: '/update',
      resolve: {
        user: userResolve.user,
        study: resolveStudy
      },
      views: {
        'main@': {
          templateUrl: '/assets/javascripts/admin/studies/studyForm.html',
          controller: 'StudyEditCtrl as vm'
        }
      },
      data: {
        displayName: 'Update'
      }
    });

    /**
     * Study view participatns information
     */
    $stateProvider.state('admin.studies.study.participants', {
      url: '/participants',
      resolve: {
        user: userResolve.user,
        study: resolveStudy,
        annotTypes: [
          'participantAnnotTypesService', 'study',
          function(participantAnnotTypesService, study) {
            return participantAnnotTypesService.getAll(study.id);
          }
        ]
      },
      views: {
        'studyDetails': {
          templateUrl: '/assets/javascripts/admin/studies/studyParticipantsTab.html',
          controller: ['$scope', 'study', 'annotTypes', function($scope, study, annotTypes) {
            $scope.study = study;
            $scope.annotTypes = annotTypes;
          }]
        }
      },
      data: {
        displayName: '{{study.name}}'
      }
    });

    /**
     * Study view specimen information
     */
    $stateProvider.state('admin.studies.study.specimens', {
      url: '/specimens',
      resolve: {
        user: userResolve.user,
        study: resolveStudy,
        specimenGroups: [
          'specimenGroupsService', 'study',
          function(specimenGroupsService, study) {
            return specimenGroupsService.getAll(study.id);
          }
        ],
        specimenGroupIdsInUse: [
          'specimenGroupsService', 'study',
          function(specimenGroupsService, study) {
            return specimenGroupsService.specimenGroupIdsInUse(study.id);
          }
        ]
      },
      views: {
        'studyDetails': {
          templateUrl: '/assets/javascripts/admin/studies/studySpecimensTab.html',
          controller: [
            '$scope', 'study', 'specimenGroups', 'specimenGroupIdsInUse',
            function($scope, study, specimenGroups, specimenGroupIdsInUse) {
              $scope.study = study;
              $scope.specimenGroups = specimenGroups;
              $scope.specimenGroupIdsInUse = specimenGroupIdsInUse;
            }
          ]
        }
      },
      data: {
        displayName: '{{study.name}}'
      }
    });

    /**
     * Study view collection information
     */
    $stateProvider.state('admin.studies.study.collection', {
      url: '/collection',
      resolve: {
        user: userResolve.user,
        study: resolveStudy,
        collectionDto: [
          'studiesService', 'study',
          function (studiesService, study) {
            return studiesService.collectionDto(study.id);
          }
        ]
      },
      views: {
        'studyDetails': {
          templateUrl: '/assets/javascripts/admin/studies/studyCollectionTab.html',
          controller: [
            '$scope', 'study', 'collectionDto',
            function($scope, study, collectionDto) {
              $scope.study = study;
              $scope.ceventTypes = collectionDto.collectionEventTypes;
              $scope.annotTypes  = collectionDto.collectionEventAnnotationTypes;
              $scope.annotTypesInUse = collectionDto.collectionEventAnnotationTypesInUse;
              $scope.specimenGroups = collectionDto.specimenGroups;
            }
          ]
        }
      },
      data: {
        displayName: '{{study.name}}'
      }
    });

    /**
     * Study view processing tab
     */
    $stateProvider.state('admin.studies.study.processing', {
      url: '/processing',
      resolve: {
        user: userResolve.user,
        study: resolveStudy,
        processingDto: [
          'studiesService', 'study',
          function (studiesService, study) {
            return studiesService.processingDto(study.id);
          }
        ]
      },
      views: {
        'studyDetails': {
          templateUrl: '/assets/javascripts/admin/studies/studyProcessingTab.html',
          controller: [
            '$scope', 'study', 'processingDto',
            function($scope, study, processingDto) {
              $scope.study = study;
              $scope.processingDto = processingDto;
            }
          ]
        }
      },
      data: {
        displayName: '{{study.name}}'
      }
    });
  }

});
