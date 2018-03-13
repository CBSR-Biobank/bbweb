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
    .state('home.admin.studies.study.processing', {
      url: '/processing',
      views: {
        'studyDetails': 'studyProcessingTab'
      }
    })
    // .state('home.admin.studies.study.processing.processingTypeAdd', {
    //   url: '/proctypes/add',
    //   resolve: {
    //     processingType: [
    //       '$transition$', 'ProcessingType',
    //       function($transition$, ProcessingType) {
    //         var pt = new ProcessingType();
    //         pt.studyId = $transition$.params().studyId;
    //         return pt;
    //       }]
    //   },
    //   views: {
    //     'main@': {
    //       template: require('./processingTypeForm.html'),
    //       controller: 'ProcessingTypeEditCtrl as vm'
    //     }
    //   }
    // })
    // .state('home.admin.studies.study.processing.processingTypeUpdate', {
    //   url: '/proctypes/update/{processingTypeId}',
    //   resolve: {
    //     processingType: [
    //       '$transition$', 'ProcessingType',
    //       function($transition$, ProcessingType) {
    //         return ProcessingType.get($transition$.params().studyId, $transition$.params().processingTypeId);
    //       }
    //     ]
    //   },
    //   views: {
    //     'main@': {
    //       template: require('./processingTypeForm.html'),
    //       controller: 'ProcessingTypeEditCtrl as vm'
    //     }
    //   }
    // })
    // .state('home.admin.studies.study.processing.spcLinkAnnotationTypeAdd', {
    //   url: '/annottype/add',
    //   resolve: {
    //     annotationType: ['SpecimenLinkAnnotationType', function(SpecimenLinkAnnotationType) {
    //       return new SpecimenLinkAnnotationType();
    //     }]
    //   },
    //   views: {
    //     'main@': {
    //       template: require('./annotationTypeForm.html'),
    //       controller: 'AnnotationTypeEditCtrl as vm'
    //     }
    //   }
    // })
    // .state('home.admin.studies.study.processing.spcLinkAnnotationTypeUpdate', {
    //   url: '/annottype/update/{annotationTypeId}',
    //   resolve: {
    //     annotationType: [
    //       '$transition$', 'SpecimenLinkAnnotationType',
    //       function($transition$, SpecimenLinkAnnotationType) {
    //         return SpecimenLinkAnnotationType.get($transition$.params().studyId, $transition$.params().annotationTypeId);
    //       }
    //     ]
    //   },
    //   views: {
    //     'main@': {
    //       template: require('./annotationTypeForm.html'),
    //       controller: 'AnnotationTypeEditCtrl as vm'
    //     }
    //   }
    // })
    // .state('home.admin.studies.study.processing.spcLinkTypeAdd', {
    //   url: '/sltype/add',
    //   resolve: {
    //     spcLinkType: [
    //       'SpecimenLinkType',
    //       function(SpecimenLinkType) {
    //         return new SpecimenLinkType();
    //       }
    //     ]
    //   },
    //   views: {
    //     'main@': {
    //       template: require('./spcLinkTypeForm.html'),
    //       controller: 'SpcLinkTypeEditCtrl as vm'
    //     }
    //   }
    // })
    // .state('home.admin.studies.study.processing.spcLinkTypeUpdate', {
    //   url: '/sltype/update/{procTypeId}/{spcLinkTypeId}',
    //   resolve: {
    //     spcLinkType: [
    //       '$transition$', 'SpecimenLinkType',
    //       function($transition$, SpecimenLinkType) {
    //         return SpecimenLinkType.get($transition$.params().procTypeId, $transition$.params().spcLinkTypeId);
    //       }
    //     ]
    //   },
    //   views: {
    //     'main@': {
    //       template: require('./spcLinkTypeForm.html'),
    //       controller: 'SpcLinkTypeEditCtrl as vm'
    //     }
    //   }
    // })
    // .state('home.admin.studies.study.specimens.groupAdd', {
    //   url: '/spcgroup/add',
    //   resolve: {
    //     specimenGroup: [
    //       '$transition$',
    //       'SpecimenGroup',
    //       function($transition$, SpecimenGroup) {
    //         var sg = new SpecimenGroup();
    //         sg.studyId = $transition$.params().studyId;
    //         return sg;
    //       }]
    //   },
    //   views: {
    //     'main@': {
    //       template: require('./specimenGroupForm.html'),
    //       controller: 'SpecimenGroupEditCtrl as vm'
    //     }
    //   }
    // })
    // .state('home.admin.studies.study.specimens.groupUpdate', {
    //   url: '/spcgroup/update/{specimenGroupId}',
    //   resolve: {
    //     specimenGroup: [
    //       '$transition$',
    //       'SpecimenGroup',
    //       function($transition$, SpecimenGroup) {
    //         return SpecimenGroup.get($transition$.params().studyId, $transition$.params().specimenGroupId);
    //       }
    //     ]
    //   },
    //   views: {
    //     'main@': {
    //       template: require('./specimenGroupForm.html'),
    //       controller: 'SpecimenGroupEditCtrl as vm'
    //     }
    //   }
    // })
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
    .state('home.admin.studies.study.collection.ceventType', {
      url: '/events/{ceventTypeSlug}',
      resolve: {
        collectionEventType: resolveCollectionEventType
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
    .state('home.admin.studies.study.collection.ceventType.specimenDescriptionAdd', {
      url: '/spcdescs/add',
      views: {
        'main@': 'collectionSpecimenDescriptionAdd'
      }
    })
    .state('home.admin.studies.study.collection.ceventType.specimenDescriptionView', {
      url: '/spcdescs/{specimenDescriptionSlug}',
      resolve: {
        specimenDescription: resolveSpcimenDescription
      },
      views: {
        'main@': 'collectionSpecimenDescriptionView'
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
  function resolveCollectionEventType($transition$, study, CollectionEventType, resourceErrorService) {
    const slug = $transition$.params().ceventTypeSlug
    return CollectionEventType.get(study.slug, slug)
      .catch(resourceErrorService.goto404(
        `collection event type ID not found: studyId/${study.slug}, ceventTypeSlug/${slug}`))
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
    const slug = $transition$.params().specimenDescriptionSlug,
          spcDescription = _.find(collectionEventType.specimenDescriptions, { slug }),
          result = spcDescription ? $q.when(spcDescription) : $q.reject('invalid specimen-description ID')
    return result.catch(resourceErrorService.goto404(`invalid event-type specimen-description ID: ${slug}`))
  }

}

export default ngModule => ngModule.config(adminStudiesUiRouterConfig)
