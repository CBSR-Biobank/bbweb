/**
 * collection routes.
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */

/* @ngInject */
function config($stateProvider) {

  $stateProvider
    .state('home.collection', {
      // this state is checked for an authorized user, see uiRouterIsAuthorized() in app.js
      url: 'collection',
      views: {
        'main@': 'collection'
      }
    })
    .state('home.collection.study', {
      url: '/study/:studySlug',
      resolve: {
        study: resolveStudy
      },
      views: {
        'main@': 'participantGet'
      }
    })
    .state('home.collection.study.participantAdd', {
      url: '/add/{uniqueId}',
      resolve: {
        uniqueId: resolveParticipantUniqueId
      },
      views: {
        'main@': 'participantAdd'
      }
    })
    .state('home.collection.study.participant', {
      abstract: true,
      url: '/participant/{participantId}',
      resolve: {
        participant: resolveParticipant
      },
      views: {
        'main@': 'participantView'
      }
    })
    .state('home.collection.study.participant.summary', {
      url: '/summary',
      views: {
        'participantDetails': 'participantSummary'
      }
    })
    .state('home.collection.study.participant.cevents', {
      url: '/events',
      views: {
        'participantDetails': 'ceventsList'
      }
    })
    .state('home.collection.study.participant.cevents.add', {
      url: '/add',
      views: {
        'main@': 'ceventGetType'
      }
    })
    .state('home.collection.study.participant.cevents.add.details', {
      url: '/{eventTypeId}',
      resolve: {
        collectionEventType: resolveCollectionEventType
      },
      views: {
        'main@': 'ceventAdd'
      }
    })
    .state('home.collection.study.participant.cevents.details', {
      url: '/{eventId}',
      resolve: {
        collectionEvent: resolveCollectionEvent
      },
      views: {
        'eventDetails': 'ceventView'
      }
    })
    .state('home.collection.study.participant.cevents.details.specimen', {
      url: '/{inventoryId}',
      resolve: {
        specimen: resolveSpecimen
      },
      views: {
        'main@': 'specimenView'
      }
    });

  /* @ngInject */
  function resolveStudy($transition$, resourceErrorService, Study) {
    const slug = $transition$.params().studySlug
    return Study.get(slug)
      .catch(resourceErrorService.goto404(`study slug not found: ${slug}`))
  }

  /* @ngInject */
  function resolveParticipantUniqueId($transition$) {
    return $transition$.params().uniqueId;
  }

  /* @ngInject */
  function resolveParticipant($transition$, Participant, resourceErrorService, study) {
    const participantId  = $transition$.params().participantId
    return Participant.get(study.id, participantId)
      .then(participant => {
        participant.setStudy(study)
        return participant
      })
      .catch(resourceErrorService.goto404(
        `participant not found: studyId/${study.id}, participantId/${participantId}`))
  }

  /* @ngInject */
  function resolveCollectionEvent($q, $transition$, CollectionEvent, resourceErrorService) {
    const id = $transition$.params().eventId
    return CollectionEvent.get(id)
      .catch(resourceErrorService.goto404(`collectionEvent ID not found: ${id}`))
  }

  /* @ngInject */
  function resolveCollectionEventType($transition$, resourceErrorService, study, CollectionEventType) {
    const typeId = $transition$.params().eventTypeId;
    return CollectionEventType.get(study.id, typeId)
      .catch(resourceErrorService.goto404(`collectionEventType not found: studyId/${study.id}, typeId/${typeId}`))
  }

  /* @ngInject */
  function resolveSpecimen($transition$, Specimen, resourceErrorService) {
    const inventoryId = $transition$.params().inventoryId
    return Specimen.getByInventoryId(inventoryId)
      .catch(resourceErrorService.goto404(`specimen ID not found: ${inventoryId}`))
  }

}

export default ngModule => ngModule.config(config)
