/**
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

  function config($urlRouterProvider,
                  $stateProvider,
                  authorizationProvider
                 ) {

    resolveSpecimenGroups.$inject = ['$stateParams', 'SpecimenGroup'];
    function resolveSpecimenGroups($stateParams, SpecimenGroup) {
      return SpecimenGroup.list($stateParams.studyId);
    }

    resolveAnnotationTypes.$inject = ['CollectionEventAnnotationType', 'study'];
    function resolveAnnotationTypes(CollectionEventAnnotationType, study) {
      return CollectionEventAnnotationType.list(study.id);
    }

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
        user: authorizationProvider.requireAuthenticatedUser,
        studySpecimenGroups: resolveSpecimenGroups,
        studyAnnotationTypes: resolveAnnotationTypes,
        ceventType: [
          'CollectionEventType',
          'studySpecimenGroups',
          'studyAnnotationTypes',
          function(CollectionEventType,
                   studySpecimenGroups,
                   studyAnnotationTypes) {
            var cet = new CollectionEventType();
            cet.studySpecimenGroups(studySpecimenGroups);
            cet.studyAnnotationTypes(studyAnnotationTypes);
            return cet;
          }]
      },
      views: {
        'main@': {
          templateUrl: '/assets/javascripts/admin/studies/ceventTypes/ceventTypeForm.html',
          controller: 'CeventTypeEditCtrl as vm'
        }
      },
      data: {
        displayName: 'Collection Event Type'
      }
    });

    /**
     * Collection Event Type Update
     */
    $stateProvider.state('home.admin.studies.study.collection.ceventTypeUpdate', {
      url: '/cetypes/update/{ceventTypeId}',
      resolve: {
        user: authorizationProvider.requireAuthenticatedUser,
        studySpecimenGroups: resolveSpecimenGroups,
        studyAnnotationTypes: resolveAnnotationTypes,
        ceventType: [
          '$stateParams',
          'CollectionEventType',
          'studySpecimenGroups',
          'studyAnnotationTypes',
          function($stateParams,
                   CollectionEventType,
                   studySpecimenGroups,
                   studyAnnotationTypes) {
            return CollectionEventType.get(
              $stateParams.studyId,
              $stateParams.ceventTypeId).then(function (cet) {
                cet.studySpecimenGroups(studySpecimenGroups);
                cet.studyAnnotationTypes(studyAnnotationTypes);
                return cet;
              });
          }
        ]
      },
      views: {
        'main@': {
          templateUrl: '/assets/javascripts/admin/studies/ceventTypes/ceventTypeForm.html',
          controller: 'CeventTypeEditCtrl as vm'
        }
      },
      data: {
        displayName: 'Collection Event Type'
      }
    });

    /**
     * Collection Event Annotation Type Add
     */
    $stateProvider.state('home.admin.studies.study.collection.view.annotationTypeAdd', {
      url: '/cetype/annottype/add',
      resolve: {
        user: authorizationProvider.requireAuthenticatedUser,
        annotationType: ['CollectionEventAnnotationType', function(CollectionEventAnnotationType) {
          return new CollectionEventAnnotationType();
        }]
      },
      views: {
        'main@': {
          templateUrl: '/assets/javascripts/admin/studies/annotationTypes/annotationTypeForm.html',
          controller: 'AnnotationTypeEditCtrl as vm'
        }
      },
      data: {
        displayName: 'Annotation Type'
      }
    });

    /**
     * Collection Event Annotation Type Update
     */
    // FIXME - no longer require
    $stateProvider.state('home.admin.studies.study.collection.ceventAnnotationTypeUpdate', {
      url: '/cevent/annottype/update/{annotationTypeId}',
      resolve: {
        user: authorizationProvider.requireAuthenticatedUser,
        annotationType: [
          '$stateParams', 'CollectionEventAnnotationType',
          function($stateParams, CollectionEventAnnotationType) {
            return CollectionEventAnnotationType.get($stateParams.studyId,
                                                     $stateParams.annotationTypeId);
          }
        ]
      },
      views: {
        'main@': {
          templateUrl: '/assets/javascripts/admin/studies/annotationTypes/annotationTypeForm.html',
          controller: 'AnnotationTypeEditCtrl as vm'
        }
      },
      data: {
        displayName: 'Collection Event Annotation Type'
      }
    });

    $stateProvider.state('home.admin.studies.study.collection.view.specimenGroupAdd', {
      url: '/spcgroup/add',
      resolve: {
        user: authorizationProvider.requireAuthenticatedUser,
        specimenGroup: [
          '$stateParams',
          'SpecimenGroup',
          function($stateParams, SpecimenGroup) {
            var sg = new SpecimenGroup();
            sg.studyId = $stateParams.studyId;
            return sg;
          }]
      },
      views: {
        'main@': {
          templateUrl: '/assets/javascripts/admin/studies/specimenGroups/specimenGroupForm.html',
          controller: 'SpecimenGroupEditCtrl as vm'
        }
      },
      data: {
        displayName: 'Add Specimen Group'
      }
    });
  }

  return config;
});
