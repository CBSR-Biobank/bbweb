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

    $stateProvider.state('home.admin.studies.study.collection.view', {
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
      url: '/cetypes/add',
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
    $stateProvider.state('home.admin.studies.study.collection.view.annotationTypeAdd', {
      url: '/cetype/annottype/add',
      resolve: {
        user: authorizationProvider.requireAuthenticatedUser
      },
      views: {
        'main@': {
          template: [
            '<collection-event-annotation-type-add',
            '  collection-event-type="vm.ceventType">',
            '</collection-event-annotation-type-add>'
          ].join(''),
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
        displayName: 'Annotation'
      }
    });

    $stateProvider.state('home.admin.studies.study.collection.view.annotationTypeView', {
      url: '/annottype/{annotationTypeId}',
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
            '  collection-event-type="vm.ceventType"',
            '  annotation-type="vm.annotationType">',
            '</collection-event-annotation-type-view>'
          ].join(''),
          controller: [
            'ceventType',
            'annotationType',
            function (ceventType, annotationType) {
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

  }

  return config;
});
