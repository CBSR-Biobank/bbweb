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
        addOrUpdateFn: ['ParticipantAnnotTypeService', function(ParticipantAnnotTypeService) {
          return ParticipantAnnotTypeService.addOrUpdate;
        }],
        valueTypes: ['StudyAnnotTypeService', function(StudyAnnotTypeService) {
          return StudyAnnotTypeService.valueTypes();
        }]
      },
      views: {
        'main@': {
          templateUrl: '/assets/javascripts/admin/studies/annotationTypes/annotTypeForm.html',
          controller: 'AnnotationTypeEditCtrl as vm'
        }
      },
      data: {
        displayName: 'Participant Annotation Type',
        returnState: 'admin.studies.study.participants' // need to use state date since child state resolves inherit from parent states
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
          '$stateParams', 'ParticipantAnnotTypeService', 'study',
          function($stateParams, ParticipantAnnotTypeService, study) {
            if ($stateParams.annotTypeId) {
              return ParticipantAnnotTypeService.get(study.id, $stateParams.annotTypeId);
            }
            throw new Error('state parameter annotTypeId is invalid');
          }
        ],
        childReturnState: function() {
          return 'admin.studies.study.participants';
        },
        addOrUpdateFn: ['ParticipantAnnotTypeService', function(ParticipantAnnotTypeService) {
          return ParticipantAnnotTypeService.addOrUpdate;
        }],
        valueTypes: ['StudyAnnotTypeService', function(StudyAnnotTypeService) {
          return StudyAnnotTypeService.valueTypes();
        }]
      },
      views: {
        'main@': {
          templateUrl: '/assets/javascripts/admin/studies/annotationTypes/annotTypeForm.html',
          controller: 'AnnotationTypeEditCtrl as vm'
        }
      },
      data: {
        displayName: 'Participant Annotation Type',
        returnState: 'admin.studies.study.participants' // need to use state date since child state resolves inherit from parent states
      }
    });
  }

});
