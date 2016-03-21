/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(['underscore'], function (_) {
  'use strict';

  config.$inject = [
    '$urlRouterProvider',
    '$stateProvider',
    'authorizationProvider'
  ];

  function config($urlRouterProvider,
                  $stateProvider,
                  authorizationProvider) {

    $urlRouterProvider.otherwise('/');

    $stateProvider.state('home.admin.studies.study.collection.ceventType', {
      url: '/cetype/{ceventTypeId}',
      resolve: {
        user: authorizationProvider.requireAuthenticatedUser,
        ceventType: [
          '$stateParams',
          'study',
          'CollectionEventType',
          function ($stateParams, study, CollectionEventType) {
            return CollectionEventType.get(study.id, $stateParams.ceventTypeId);
          }
        ]
      },
      views: {
        'ceventTypeDetails': {
          template: '<cevent-type-view cevent-type="vm.ceventType"></cevent-type-view>',
          controller: [
            'ceventType',
            function (ceventType) {
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
      resolve: {
        user: authorizationProvider.requireAuthenticatedUser
      },
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
      resolve: {
        user: authorizationProvider.requireAuthenticatedUser
      },
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
        user: authorizationProvider.requireAuthenticatedUser,
        annotationType: [
          'ceventType',
          '$stateParams',
          function (ceventType, $stateParams) {
            var annotationType = _.findWhere(ceventType.annotationTypes,
                                             { uniqueId: $stateParams.annotationTypeId });
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
    $stateProvider.state('home.admin.studies.study.collection.ceventType.specimenSpecAdd', {
      url: '/spcspec/add',
      resolve: {
        user: authorizationProvider.requireAuthenticatedUser
      },
      views: {
        'main@': {
          template: [
            '<collection-specimen-spec-add',
            '  study="vm.study"',
            '  collection-event-type="vm.ceventType">',
            '</collection-specimen-spec-add>'
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
    $stateProvider.state('home.admin.studies.study.collection.ceventType.specimenSpecView', {
      url: '/spcspec/view/{specimenSpecId}',
      resolve: {
        user: authorizationProvider.requireAuthenticatedUser,
        specimenSpec: [
          'ceventType',
          '$stateParams',
          function (ceventType, $stateParams) {
            var specimenSpec = _.findWhere(ceventType.specimenSpecs,
                                           { uniqueId: $stateParams.specimenSpecId });
            if (_.isUndefined(specimenSpec)) {
              throw new Error('could not find specimen spec: ' + $stateParams.specimenSpecId);
            }
            return specimenSpec;
          }
        ]
      },
      views: {
        'main@': {
          template: [
            '<collection-specimen-spec-view',
            '  study="vm.study"',
            '  collection-event-type="vm.ceventType"',
            '  specimen-spec="vm.specimenSpec">',
            '</collection-specimen-spec-view>'
          ].join(''),
          controller: [
            'study',
            'ceventType',
            'specimenSpec',
            function (study, ceventType, specimenSpec) {
              this.study = study;
              this.ceventType = ceventType;
              this.specimenSpec = specimenSpec;
            }
          ],
          controllerAs: 'vm'
        }
      },
      data: {
        displayName: '{{specimenSpec.name}}'
      }
    });

  }

  return config;
});
