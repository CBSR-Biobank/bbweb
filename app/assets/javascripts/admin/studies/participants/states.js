/**
 * Configure routes of user module.
 */
define(['../../module'], function(module) {
  'use strict';

  module.config(config);

  config.$inject = [
    '$urlRouterProvider', '$stateProvider', 'userResolve'
  ];

  function config($urlRouterProvider, $stateProvider, userResolve) {
    $urlRouterProvider.otherwise('/');

    /**
     * Prticipant Annotation Type Add
     */
    $stateProvider.state('admin.studies.study.participants.annotTypeAdd', {
      url: '/annottype/add',
      resolve: {
        user: userResolve.user,
        annotType: ['study', function(study) {
          return {
            studyId: study.id,
            name: '',
            description: null,
            required: false,
            valueType: '',
            options: []
          };
        }],
        addOrUpdateFn: ['participantAnnotTypesService', function(participantAnnotTypesService) {
          return participantAnnotTypesService.addOrUpdate;
        }],
        valueTypes: ['studiesService', function(studiesService) {
          return studiesService.valueTypes();
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
    $stateProvider.state('admin.studies.study.participants.annotTypeUpdate', {
      url: '/annottype/update/{annotTypeId}',
      resolve: {
        user: userResolve.user,
        annotType: [
          '$stateParams', 'participantAnnotTypesService', 'study',
          function($stateParams, participantAnnotTypesService, study) {
            if ($stateParams.annotTypeId) {
              return participantAnnotTypesService.get(study.id, $stateParams.annotTypeId);
            }
            throw new Error('state parameter annotTypeId is invalid');
          }
        ],
        childReturnState: function() {
          return 'admin.studies.study.participants';
        },
        addOrUpdateFn: ['participantAnnotTypesService', function(participantAnnotTypesService) {
          return participantAnnotTypesService.addOrUpdate;
        }],
        valueTypes: ['studiesService', function(studiesService) {
          return studiesService.valueTypes();
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
  }

});
