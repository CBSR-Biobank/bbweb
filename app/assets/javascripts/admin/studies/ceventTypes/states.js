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

    $stateProvider.state('home.admin.studies.study.collection.ceventType', {
      url: '/event/{ceventTypeId}',
      resolve: {
        ceventType: [
          '$transition$',
          'study',
          'CollectionEventType',
          function ($transition$, study, CollectionEventType) {
            return CollectionEventType.get(study.id, $transition$.params().ceventTypeId);
          }
        ]
      },
      views: {
        'ceventTypeDetails': {
          template: '<cevent-type-view study="vm.study" cevent-type="vm.ceventType"></cevent-type-view>',
          controller: [
            'study',
            'ceventType',
            function (study, ceventType) {
              this.study = study;
              this.ceventType = ceventType;
            }
          ],
          controllerAs: 'vm'
        }
      },
      data: {
        displayName: '{{ceventType.name}}'
      }
    });

    /**
     * Collection Event Type Add
     */
    $stateProvider.state('home.admin.studies.study.collection.ceventTypeAdd', {
      url: '/add',
      views: {
        'main@': {
          template: '<cevent-type-add study="vm.study"></cevent-type-add>',
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
        displayName: 'Collection Event'
      }
    });

    /**
     * Collection Event Annotation Type Add
     */
    $stateProvider.state('home.admin.studies.study.collection.ceventType.annotationTypeAdd', {
      url: '/annottype/add',
      views: {
        'main@': {
          template: [
            '<collection-event-annotation-type-add',
            '  study="vm.study"',
            '  collection-event-type="vm.ceventType">',
            '</collection-event-annotation-type-add>'
          ].join(''),
          controller: [
            'study',
            'ceventType',
            function (study, ceventType) {
              this.study      = study;
              this.ceventType = ceventType;
            }
          ],
          controllerAs: 'vm'
        }
      },
      data: {
        displayName: 'Annotation'
      }
    });

    $stateProvider.state('home.admin.studies.study.collection.ceventType.annotationTypeView', {
      url: '/annottype/view/{annotationTypeId}',
      resolve: {
        annotationType: [
          'ceventType',
          '$transition$',
          function (ceventType, $transition$) {
            var annotationType = _.find(ceventType.annotationTypes, { id: $transition$.params().annotationTypeId });
            if (_.isUndefined(annotationType)) {
              throw new Error('could not find annotation type: ' + $transition$.params().annotationTypeId);
            }
            return annotationType;
          }
        ]
      },
      views: {
        'main@': {
          template: [
            '<collection-event-annotation-type-view',
            '  study="vm.study"',
            '  collection-event-type="vm.ceventType"',
            '  annotation-type="vm.annotationType">',
            '</collection-event-annotation-type-view>'
          ].join(''),
          controller: [
            'study',
            'ceventType',
            'annotationType',
            function (study, ceventType, annotationType) {
              this.study = study;
              this.ceventType = ceventType;
              this.annotationType = annotationType;
            }
          ],
          controllerAs: 'vm'
        }
      },
      data: {
        displayName: 'Annotation {{annotationType.name}}'
      }
    });

    /**
     * Collection Event Specimen Spec Add
     */
    $stateProvider.state('home.admin.studies.study.collection.ceventType.specimenDescriptionAdd', {
      url: '/spcspec/add',
      views: {
        'main@': {
          template: [
            '<collection-specimen-description-add',
            '  study="vm.study"',
            '  collection-event-type="vm.ceventType">',
            '</collection-specimen-description-add>'
          ].join(''),
          controller: [
            'study',
            'ceventType',
            function (study, ceventType) {
              this.study = study;
              this.ceventType = ceventType;
            }
          ],
          controllerAs: 'vm'
        }
      },
      data: {
        displayName: 'Specimen'
      }
    });

    /**
     * Collection Event Specimen Spec Add
     */
    $stateProvider.state('home.admin.studies.study.collection.ceventType.specimenDescriptionView', {
      url: '/spcspec/view/{specimenDescriptionId}',
      resolve: {
        specimenDescription: [
          'ceventType',
          '$transition$',
          function (ceventType, $transition$) {
            var specimenDescription = _.find(ceventType.specimenDescriptions,
                                           { id: $transition$.params().specimenDescriptionId });
            if (_.isUndefined(specimenDescription)) {
              throw new Error('could not find specimen spec: ' + $transition$.params().specimenDescriptionId);
            }
            return specimenDescription;
          }
        ]
      },
      views: {
        'main@': {
          template: [
            '<collection-specimen-description-view',
            '  study="vm.study"',
            '  collection-event-type="vm.ceventType"',
            '  specimen-description="vm.specimenDescription">',
            '</collection-specimen-description-view>'
          ].join(''),
          controller: [
            'study',
            'ceventType',
            'specimenDescription',
            function (study, ceventType, specimenDescription) {
              this.study = study;
              this.ceventType = ceventType;
              this.specimenDescription = specimenDescription;
            }
          ],
          controllerAs: 'vm'
        }
      },
      data: {
        displayName: '{{specimenDescription.name}}'
      }
    });

  }

  return config;
});
