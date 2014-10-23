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
        studies: ['studiesService', function(studiesService) {
          return studiesService.getAll();
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
        user: userResolve.user,
        study: ['$stateParams', 'studiesService', function($stateParams, studiesService) {
          if ($stateParams.studyId) {
            return studiesService.get($stateParams.studyId);
          }
          throw new Error('state parameter studyId is invalid');
        }]
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
     * Study summary information update
     */
    $stateProvider.state('admin.studies.study.summary.update', {
      url: '/update',
      resolve: {
        user: userResolve.user
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
          'participantAnnotTypesService', 'study',
          function(participantAnnotTypesService, study) {
            return participantAnnotTypesService.getAll(study.id);
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
          template: '<accordion close-others="false">' +
            '<specimen-groups-panel ' +
            '  specimen-groups="specimenGroups" ' +
            '  specimen-group-ids-in-use="specimenGroupIdsInUse"></specimen-groups-panel>' +
            '</accordion>',
          controller: [
            '$scope', 'specimenGroups', 'specimenGroupIdsInUse',
            function($scope, specimenGroups, specimenGroupIdsInUse) {
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
        // FIXME: replace these with a single REST call like already done for processing state
        ceventTypes: [
          'ceventTypesService', 'study',
          function( ceventTypesService, study) {
            return ceventTypesService.getAll(study.id);
          }
        ],
        annotTypes: [
          'ceventAnnotTypesService', 'study',
          function(ceventAnnotTypesService, study) {
            return ceventAnnotTypesService.getAll(study.id);
          }
        ],
        specimenGroups: [
          'specimenGroupsService', 'study',
          function(specimenGroupsService, study) {
            return specimenGroupsService.getAll(study.id);
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
