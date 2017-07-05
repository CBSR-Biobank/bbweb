/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(['lodash'], function (_) {
  'use strict';

  config.$inject = [ '$stateProvider' ];

  function config($stateProvider) {

    $stateProvider
      .state('home.admin.studies.study.collection.ceventType', {
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
          'ceventTypeDetails': 'ceventTypeView'
        }
      })
      .state('home.admin.studies.study.collection.ceventTypeAdd', {
        url: '/add',
        views: {
          'main@': 'ceventTypeAdd'
        }
      })
      .state('home.admin.studies.study.collection.ceventType.annotationTypeAdd', {
        url: '/annottype/add',
        views: {
          'main@': 'collectionEventAnnotationTypeAdd'
        }
      })
      .state('home.admin.studies.study.collection.ceventType.annotationTypeView', {
        url: '/annottype/view/{annotationTypeId}',
        resolve: {
          annotationType: [
            'ceventType',
            '$transition$',
            function (ceventType, $transition$) {
              var annotationType = _.find(ceventType.annotationTypes,
                                          { id: $transition$.params().annotationTypeId });
              if (_.isUndefined(annotationType)) {
                throw new Error('could not find annotation type: ' + $transition$.params().annotationTypeId);
              }
              return annotationType;
            }
          ]
        },
        views: {
          'main@': 'collectionEventAnnotationTypeView'
        }
      })
      .state('home.admin.studies.study.collection.ceventType.specimenDescriptionAdd', {
        url: '/spcspec/add',
        views: {
          'main@': 'collectionSpecimenDescriptionAdd'
        }
      })
      .state('home.admin.studies.study.collection.ceventType.specimenDescriptionView', {
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
          'main@': 'collectionSpecimenDescriptionView'
        }
      });

  }

  return config;
});
