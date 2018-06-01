/**
 * UI Router states for {@link domain.studies.Study Study} administration.
 *
 * @namespace admin.studies.states
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

import _ from 'lodash'

/**
 * UI Router states used for {@link domain.studies.Study Study} Administration.
 *
 * @function admin.studies.states.adminStudiesUiRouterConfig
 *
 * @param {AngularJS_Service} $stateProvider
 */
/* @ngInject */
function adminStudiesUiRouterConfig($stateProvider) {
  $stateProvider
    .state('home.admin.studies', {
      url: '/studies',
      views: {
        'main@': 'studiesAdmin'
      }
    })
    .state('home.admin.studies.add', {
      url: '/add',
      resolve: {
        study: ['Study', function(Study) {
          return new Study();
        }]
      },
      views: {
        'main@': 'studyAdd'
      }
    })
    .state('home.admin.studies.study', {
      abstract: true,
      url: '/{studySlug}',
      resolve: {
        study: resolveStudy
      },
      views: {
        'main@': 'studyView'
      }
    })
    .state('home.admin.studies.study.summary', {
      url: '/summary',
      views: {
        'studyDetails': 'studySummary'
      }
    })
    .state('home.admin.studies.study.participants', {
      url: '/participants',
      views: {
        'studyDetails': 'studyParticipantsTab'
      }
    })
    .state('home.admin.studies.study.collection', {
      url: '/collection',
      views: {
        'studyDetails': 'studyCollection'
      }
    })
    .state('home.admin.studies.study.participants.annotationTypeAdd', {
      url: '/annottype/add',
      views: {
        'main@': 'participantAnnotationTypeAdd'
      }
    })
    .state('home.admin.studies.study.participants.annotationTypeView', {
      url: '/annottype/{annotationTypeSlug}',
      resolve: {
        annotationType: resolveParticipantAnnotationType
      },
      views: {
        'main@': 'participantAnnotationTypeView'
      }
    })
    .state('home.admin.studies.study.collection.ceventTypeAdd', {
      url: '/add',
      views: {
        'main@': 'ceventTypeAdd'
      }
    })
    .state('home.admin.studies.study.collection.ceventType.annotationTypeAdd', {
      url: '/annottypes/add',
      views: {
        'main@': 'collectionEventAnnotationTypeAdd'
      }
    })
    .state('home.admin.studies.study.collection.ceventType.annotationTypeView', {
      url: '/annottypes/{annotationTypeSlug}',
      resolve: {
        annotationType: resolveAnnotationType
      },
      views: {
        'main@': 'collectionEventAnnotationTypeView'
      }
    })
    .state('home.admin.studies.study.collection.ceventType.specimenDefinitionView', {
      url: '/spcdefs/{specimenDefinitionSlug}',
      resolve: {
        specimenDefinition: resolveSpcimenDescription
      },
      views: {
        'main@': 'collectionSpecimenDefinitionView'
      }
    });

  /* @ngInject */
  function resolveStudy($transition$, Study, resourceErrorService) {
    const slug = $transition$.params().studySlug
    return Study.get(slug)
      .catch(resourceErrorService.goto404(`study slug invalid: ${slug}`))
  }

  /* @ngInject */
  function resolveParticipantAnnotationType($q, $transition$, study, resourceErrorService) {
    const slug = $transition$.params().annotationTypeSlug,
          annotationType = _.find(study.annotationTypes, { slug }),
          result = annotationType ? $q.when(annotationType) : $q.reject('invalid annotation type slug')
    return result.catch(resourceErrorService.goto404(`invalid participant annotation type ID: ${slug}`))
  }

  /* @ngInject */
  function resolveAnnotationType($q, $transition$, collectionEventType, resourceErrorService) {
    const slug = $transition$.params().annotationTypeSlug,
          annotationType = _.find(collectionEventType.annotationTypes, { slug  }),
          result = annotationType ? $q.when(annotationType) : $q.reject('invalid annotation type ID')
    return result.catch(resourceErrorService.goto404(`invalid event-type annotation-type ID: ${slug}`))
  }

  /* @ngInject */
  function resolveSpcimenDescription($q, $transition$, collectionEventType, resourceErrorService) {
    const slug = $transition$.params().specimenDefinitionSlug,
          spcDefinition = _.find(collectionEventType.specimenDefinitions, { slug }),
          result = spcDefinition ? $q.when(spcDefinition) : $q.reject('invalid specimen-definition ID')
    return result.catch(resourceErrorService.goto404(`invalid event-type specimen-definition ID: ${slug}`))
  }

}

export default ngModule => ngModule.config(adminStudiesUiRouterConfig)
