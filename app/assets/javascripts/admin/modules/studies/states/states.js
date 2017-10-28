/**
 * Configure routes of studies module.
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */

import _ from 'lodash'

/* @ngInject */
function config($stateProvider) {
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
      url: '/{studyId}',
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
      url: '/annottype/view/{annotationTypeId}',
      resolve: {
        annotationType: [
          'study',
          '$transition$',
          function (study, $transition$) {
            var annotationType = _.find(study.annotationTypes, { id: $transition$.params().annotationTypeId });
            if (_.isUndefined(annotationType)) {
              throw new Error('could not find annotation type: ' + $transition$.params().annotationTypeId);
            }
            return annotationType;
          }
        ]
      },
      views: {
        'main@': 'participantAnnotationTypeView'
      }
    })
    .state('home.admin.studies.study.collection.ceventType', {
      url: '/event/{ceventTypeId}',
      resolve: {
        collectionEventType: [
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
        annotationType: resolveAnnotationType
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
        specimenDescription: resolveSpcimenDescription
      },
      views: {
        'main@': 'collectionSpecimenDescriptionView'
      }
    });

  /* @ngInject */
  function resolveStudy($transition$, Study) {
    if ($transition$.params().studyId) {
      return Study.get($transition$.params().studyId);
    }
    throw new Error('state parameter studyId is invalid');
  }

  /* @ngInject */
  function resolveAnnotationType(collectionEventType, $transition$) {
    var annotationType = _.find(collectionEventType.annotationTypes,
                                { id: $transition$.params().annotationTypeId });
    if (_.isUndefined(annotationType)) {
      throw new Error('could not find annotation type: ' + $transition$.params().annotationTypeId);
    }
    return annotationType;
  }

  /* @ngInject */
  function resolveSpcimenDescription(collectionEventType, $transition$) {
    var specimenDescription = _.find(collectionEventType.specimenDescriptions,
                                     { id: $transition$.params().specimenDescriptionId });
    if (_.isUndefined(specimenDescription)) {
      throw new Error('could not find specimen spec: ' + $transition$.params().specimenDescriptionId);
    }
    return specimenDescription;
  }

}

export default ngModule => ngModule.config(config)
