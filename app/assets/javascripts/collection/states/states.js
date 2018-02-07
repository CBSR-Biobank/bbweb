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
      url: '/participants/:participantSlug',
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
      url: '/:eventTypeSlug',
      resolve: {
        collectionEventType: resolveCollectionEventType
      },
      views: {
        'main@': 'ceventAdd'
      }
    })
    .state('home.collection.study.participant.cevents.details', {
      url: '/:visitNumber',
      resolve: {
        collectionEvent: resolveCollectionEvent
      },
      views: {
        'eventDetails': 'ceventView'
      }
    })
    .state('home.collection.study.participant.cevents.details.specimen', {
      url: '/spc/:specimenSlug',
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
    const slug  = $transition$.params().participantSlug
    return Participant.get(slug)
      .then(participant => {
        participant.setStudy(study)
        return participant
      })
      .catch(resourceErrorService.goto404(`participant not found: slug/${slug}`))
  }

  /* @ngInject */
  function resolveCollectionEvent($q, $transition$, participant, CollectionEvent, resourceErrorService) {
    const visitNumber = $transition$.params().visitNumber
    return CollectionEvent.getByVisitNumber(participant.id, visitNumber)
      .catch(resourceErrorService.goto404(
        `collectionEvent ID not found: participantSlug/${participant.slug} visitNumber/${visitNumber}`))
  }

  /* @ngInject */
  function resolveCollectionEventType($transition$, resourceErrorService, study, CollectionEventType) {
    const typeSlug = $transition$.params().eventTypeSlug;
    return CollectionEventType.get(study.slug, typeSlug)
      .catch(resourceErrorService.goto404(
        `collectionEventType not found: studySlug/${study.id}, typeSlug/${typeSlug}`))
  }

  /* @ngInject */
  function resolveSpecimen($transition$, Specimen, resourceErrorService) {
    const slug = $transition$.params().specimenSlug;
    return Specimen.get(slug)
      .catch(resourceErrorService.goto404(`specimen slug not found: ${slug}`))
  }

}

export default ngModule => ngModule.config(config)
