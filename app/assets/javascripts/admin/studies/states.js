/**
 * Configure routes of studies module.
 *
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

  function config($urlRouterProvider, $stateProvider, authorizationProvider ) {

    resolveStudy.$inject = ['$stateParams', 'Study'];
    function resolveStudy($stateParams, Study) {
      if ($stateParams.studyId) {
        return Study.get($stateParams.studyId);
      }
      throw new Error('state parameter studyId is invalid');
    }

    $urlRouterProvider.otherwise('/');

    /**
     * Studies - view all studies
     *
     */
    $stateProvider.state('home.admin.studies', {
      url: '/studies',
      resolve: {
        user: authorizationProvider.requireAuthenticatedUser
      },
      views: {
        'main@': {
          template: '<studies-list></studies-list>'
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
     * Study view participatns information
     */
    $stateProvider.state('home.admin.studies.study.participants', {
      url: '/participants',
      resolve: {
        user: authorizationProvider.requireAuthenticatedUser
      },
      views: {
        'studyDetails': {
          templateUrl: '/assets/javascripts/admin/studies/studyParticipantsTab.html',
          controller: [
            'study',
            function(study) {
              var vm = this;
              vm.study = study;
              // FIXME this is set to empty array for now, but will have to call the correct method in the future
              vm.annotationTypeIdsInUse = [];
              vm.onAnnotTypeRemove = onAnnotTypeRemove;

              function onAnnotTypeRemove(annotationType) {
                return vm.study.removeAnnotationType(annotationType);
              }
            }
          ],
          controllerAs: 'vm'
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
          '$stateParams',
          'SpecimenGroup',
          function($stateParams, SpecimenGroup) {
            return SpecimenGroup.list($stateParams.studyId);
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
        user: authorizationProvider.requireAuthenticatedUser
      },
      views: {
        'studyDetails': {
          template: [
            '<study-collection',
            '  study="vm.study"',
            '</study-collection>',
          ].join(''),
          controller: [
            'study',
            function(study) {
              this.study = study;
            }
          ],
          controllerAs: 'vm'
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
          '$stateParams',
          'ProcessingDto',
          function ($stateParams, ProcessingDto) {
            return ProcessingDto.get($stateParams.studyId);
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

  return config;
});
