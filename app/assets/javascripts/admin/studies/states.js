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

    $urlRouterProvider.otherwise('/');

    /**
     * Studies - view all studies in panels
     *
     */
    $stateProvider.state('admin.studies', {
      url: '/studies',
      resolve: {
        user: userResolve.user,
        studies: ['StudyService', function(StudyService) {
          return StudyService.list();
        }]
      },
      views: {
        'main@': {
          templateUrl: '/assets/javascripts/admin/studies/studiesPanels.html',
          controller: 'StudiesCtrl as vm'
        }
      },
      data: {
        displayName: 'Studies'
      }
    });

    /**
     * Studies - view all studies in a table
     */
    $stateProvider.state('admin.studies.table', {
      url: '',
      resolve: {
        user: userResolve.user
      },
      views: {
        'main@': {
          templateUrl: '/assets/javascripts/admin/studies/studiesTable.html',
          controller: 'StudiesTableCtrl as vm'
        }
      },
      data: {
        displayName: false
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
        },
        returnState: function() {
          return {
            name: 'admin.studies',
            params:  {},
            options: {}
          };
        }
      },
      views: {
        'main@': {
          templateUrl: '/assets/javascripts/admin/studies/studyForm.html',
          controller: 'StudyEditCtrl'
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
        user: userResolve.user,
        study: ['$stateParams', 'StudyService', function($stateParams, StudyService) {
          if ($stateParams.studyId) {
            return StudyService.query($stateParams.studyId);
          }
          throw new Error('state parameter studyId is invalid');
        }]
      },
      views: {
        'main@': {
          templateUrl: '/assets/javascripts/admin/studies/studyView.html',
          controller: [
            '$window', '$scope', '$state', '$timeout', 'study',
            function($window, $scope, $state, $timeout, study) {
              $scope.study = study;

              // initialize the panels to open state
              //
              // this way when the user selects a new study, the panels always default to open
              $window.localStorage.setItem('study.panel.collectionEventTypes',        true);
              $window.localStorage.setItem('study.panel.participantAnnotationTypes',  true);
              $window.localStorage.setItem('study.panel.participantAnnottionTypes',   true);
              $window.localStorage.setItem('study.panel.processingTypes',             true);
              $window.localStorage.setItem('study.panel.specimenGroups',              true);
              $window.localStorage.setItem('study.panel.specimenLinkAnnotationTypes', true);
              $window.localStorage.setItem('study.panel.specimenLinkTypes',           true);

              $scope.tabSummaryActive      = false;
              $scope.tabParticipantsActive = false;
              $scope.tabSpecimensActive    = false;
              $scope.tabCollectionActive   = false;
              $scope.tabProcessingActive   = false;

              $timeout(function() {
                $scope.tabSummaryActive      = ($state.current.name === 'admin.studies.study.summary');
                $scope.tabParticipantsActive = ($state.current.name === 'admin.studies.study.participants');
                $scope.tabSpecimensActive    = ($state.current.name === 'admin.studies.study.specimens');
                $scope.tabCollectionActive   = ($state.current.name === 'admin.studies.study.collection');
                $scope.tabProcessingActive   = ($state.current.name === 'admin.studies.study.processing');
              }, 0);
            }
          ]
        }
      },
      data: {
        breadcrumProxy: 'admin.studies.study.summary'
      }
    });

    /**
     * Study summary information update
     */
    $stateProvider.state('admin.studies.study.summary.update', {
      url: '/update',
      resolve: {
        user: userResolve.user,
        returnState: ['$stateParams', function($stateParams) {
          return {
            name: 'admin.studies.study.summary',
            params:  { studyId: $stateParams.studyId },
            options: { reload: true }
          };
        }]
      },
      views: {
        'main@': {
          templateUrl: '/assets/javascripts/admin/studies/studyForm.html',
          controller: 'StudyEditCtrl'
        }
      },
      data: {
        displayName: 'Update'
      }
    });

    /**
     * Study view summary information
     */
    $stateProvider.state('admin.studies.study.summary', {
      url: '/summary',
      resolve: {
        user: userResolve.user
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
     * Study view participatns information
     */
    $stateProvider.state('admin.studies.study.participants', {
      url: '/participants',
      resolve: {
        user: userResolve.user,
        annotTypes: [
          'ParticipantAnnotTypeService', 'study',
          function(ParticipantAnnotTypeService, study) {
            return ParticipantAnnotTypeService.getAll(study.id);
          }
        ]
      },
      views: {
        'studyDetails': {
          template: '<accordion close-others="false">' +
            '<participants-annot-types-panel annot-types="annotTypes"></participants-annot-types-panel>' +
            '</accordion>',
          controller: ['$scope', 'annotTypes', function($scope, annotTypes) {
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
        specimenGroups: [
          'SpecimenGroupService', 'study',
          function(SpecimenGroupService, study) {
            return SpecimenGroupService.getAll(study.id);
          }
        ]
      },
      views: {
        'studyDetails': {
          template: '<accordion close-others="false">' +
            '<specimen-groups-panel specimen-groups="specimenGroups"></specimen-groups-panel>' +
            '</accordion>',
          controller: ['$scope', 'specimenGroups', function($scope, specimenGroups) {
            $scope.specimenGroups = specimenGroups;
          }]
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
        // FIXME: replace these with a single REST call like already done for processing state
        ceventTypes: [
          'CeventTypeService', 'study',
          function( CeventTypeService, study) {
            return CeventTypeService.getAll(study.id);
          }
        ],
        annotTypes: [
          'CeventAnnotTypeService', 'study',
          function(CeventAnnotTypeService, study) {
            return CeventAnnotTypeService.getAll(study.id);
          }
        ],
        specimenGroups: [
          'SpecimenGroupService', 'study',
          function(SpecimenGroupService, study) {
            return SpecimenGroupService.getAll(study.id);
          }
        ]
      },
      views: {
        'studyDetails': {
          templateUrl: '/assets/javascripts/admin/studies/studyCollectionTab.html',
          controller: [
            '$scope', 'ceventTypes', 'annotTypes', 'specimenGroups',
            function($scope, ceventTypes, annotTypes, specimenGroups) {
              $scope.ceventTypes = ceventTypes;
              $scope.annotTypes  = annotTypes;
              $scope.specimenGroups = specimenGroups;
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
        processingDto: [
          'StudyService', 'study',
          function (StudyService, study) {
            return StudyService.processingDto(study.id);
          }
        ]
      },
      views: {
        'studyDetails': {
          templateUrl: '/assets/javascripts/admin/studies/studyProcessingTab.html',
          controller: [
            '$scope', 'processingDto',
            function($scope, processingDto) {
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
