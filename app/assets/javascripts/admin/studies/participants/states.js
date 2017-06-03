/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(['lodash'], function (_) {
  'use strict';

  config.$inject = [
    '$urlRouterProvider',
    '$stateProvider'
  ];

  function config($urlRouterProvider, $stateProvider) {
    $urlRouterProvider.otherwise('/');

    /**
     * Prticipant Annotation Type Add
     */
    $stateProvider.state('home.admin.studies.study.participants.annotationTypeAdd', {
      url: '/annottype/add',
      views: {
        'main@': {
          template: [
            '<participant-annotation-type-add',
            '  study="vm.study"',
            '</participant-annotation-type-add>'
          ].join(''),
          controller: [
            'study',
            function (study) {
              this.study = study;
            }
          ],
          controllerAs: 'vm'
        }
      },
      data: {
        displayName: 'Participant Annotation Type'
      }
    });

    $stateProvider.state('home.admin.studies.study.participants.annotationTypeView', {
      url: '/annottype/view/{annotationTypeId}',
      resolve: {
        annotationType: [
          'study',
          '$stateParams',
          function (study, $stateParams) {
            var annotationType = _.find(study.annotationTypes, { id: $stateParams.annotationTypeId });
            if (_.isUndefined(annotationType)) {
              throw new Error('could not find annotation type: ' + $stateParams.annotationTypeId);
            }
            return annotationType;
          }
        ]
      },
      views: {
        'main@': {
          template: [
            '<participant-annotation-type-view',
            '  study="vm.study"',
            '  annotation-type="vm.annotationType"',
            '</participant-annotation-type-view>'
          ].join(''),
          controller: [
            'study',
            'annotationType',
            function (study, annotationType) {
              var vm = this;
              vm.study = study;
              vm.annotationType = annotationType;
            }
          ],
          controllerAs: 'vm'
        }
      },
      data: {
        displayName: 'Participant Annotation: {{annotationType.name}}'
      }
    });
  }

  return config;
});
