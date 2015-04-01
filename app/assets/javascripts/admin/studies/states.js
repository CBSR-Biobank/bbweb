/**
 * Configure routes of studies module.
 */
define(['angular', 'underscore'], function(angular, _) {
  'use strict';

  config.$inject = [
    '$urlRouterProvider',
    '$stateProvider',
    'authorizationProvider'
  ];

  function config($urlRouterProvider, $stateProvider, authorizationProvider ) {

    resolveStudy.$inject = ['$stateParams', 'Study'];
    function resolveStudy($stateParams, Study) {
      if ($stateParams.studyId) {
        return Study.get($stateParams.studyId);
      }
      throw new Error('state parameter studyId is invalid');
    }

    resolveStudyCounts.$inject = ['StudyCounts'];
    function resolveStudyCounts(StudyCounts) {
      return StudyCounts.get();
    }

    $urlRouterProvider.otherwise('/');

    /**
     * Studies - view all studies
     *
     */
    $stateProvider.state('home.admin.studies', {
      url: '/studies',
      resolve: {
        user: authorizationProvider.requireAuthenticatedUser,
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
    $stateProvider.state('home.admin.studies.add', {
      url: '/add',
      resolve: {
        user: authorizationProvider.requireAuthenticatedUser,
        study: ['Study', function(Study) {
          return new Study();
        }]
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
    $stateProvider.state('home.admin.studies.study', {
      abstract: true,
      url: '/{studyId}',
      resolve: {
        user: authorizationProvider.requireAuthenticatedUser,
        study: resolveStudy
      },
      views: {
        'main@': {
          templateUrl: '/assets/javascripts/admin/studies/studyView.html',
          controller: 'StudyCtrl as vm'
        }
      },
      data: {
        breadcrumProxy: 'home.admin.studies.study.summary'
      }
    });

    /**
     * Study view summary information
     */
    $stateProvider.state('home.admin.studies.study.summary', {
      url: '/summary',
      resolve: {
        user: authorizationProvider.requireAuthenticatedUser
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
    $stateProvider.state('home.admin.studies.study.summary.update', {
      url: '/update',
      resolve: {
        user: authorizationProvider.requireAuthenticatedUser
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
    $stateProvider.state('home.admin.studies.study.participants', {
      url: '/participants',
      resolve: {
        user: authorizationProvider.requireAuthenticatedUser,
        annotationTypes: [
          '$stateParams', 'ParticipantAnnotationType',
          function($stateParams, ParticipantAnnotationType) {
            return ParticipantAnnotationType.list($stateParams.studyId);
          }
        ]
      },
      views: {
        'studyDetails': {
          templateUrl: '/assets/javascripts/admin/studies/studyParticipantsTab.html',
          controller: ['$scope', 'study', 'annotationTypes', function($scope, study, annotationTypes) {
            $scope.study = study;
            $scope.annotationTypes = annotationTypes;
            // FIXME this is set to empty array for now, but will have to call the correct method in the future
            $scope.annotationTypeIdsInUse = [];
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
    $stateProvider.state('home.admin.studies.study.specimens', {
      url: '/specimens',
      resolve: {
        user: authorizationProvider.requireAuthenticatedUser,
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
    $stateProvider.state('home.admin.studies.study.collection', {
      url: '/collection',
      resolve: {
        user: authorizationProvider.requireAuthenticatedUser,
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
            '$scope',
            'study',
            'collectionDto',
            'CollectionEventType',
            'CollectionEventAnnotationType',
            function($scope,
                     study,
                     collectionDto,
                     CollectionEventType,
                     CollectionEventAnnotationType) {
              $scope.study = study;
              $scope.annotationTypes  = _.map(collectionDto.collectionEventAnnotationTypes,
                                         function (annotationType) {
                                           return new CollectionEventAnnotationType(annotationType);
                                         });
              // FIXME: convert collectionDto.specimenGroups to SpecimenGroup objects
              $scope.ceventTypes = _.map(collectionDto.collectionEventTypes,
                                         function (obj) {
                                           return new CollectionEventType(obj, {
                                             studySpecimenGroups: collectionDto.specimenGroups,
                                             studyAnnotationTypes: $scope.annotationTypes
                                           });
                                         });
              $scope.annotationTypeIdsInUse = collectionDto.collectionEventAnnotationTypesInUse;
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
    $stateProvider.state('home.admin.studies.study.processing', {
      url: '/processing',
      resolve: {
        user: authorizationProvider.requireAuthenticatedUser,
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
            '$scope', 'study', 'processingDto', 'SpecimenLinkAnnotationType',
            function($scope, study, processingDto, SpecimenLinkAnnotationType) {
              $scope.study = study;
              $scope.processingDto = processingDto;
              $scope.processingDto.specimenLinkAnnotationTypes = _.map(
                $scope.processingDto.specimenLinkAnnotationTypes,
                function (at) {
                  return new SpecimenLinkAnnotationType(at);
                });
            }
          ]
        }
      },
      data: {
        displayName: '{{study.name}}'
      }
    });
  }

  return config;
});
