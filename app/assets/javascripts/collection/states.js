/**
 * collection routes.
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */

import _ from 'lodash'

/* @ngInject */
function config($stateProvider) {

  $stateProvider
    .state('home.collection', {
      // this state is checked for an authorized user, see uiRouterIsAuthorized() in app.js
      url: '^/collection',
      views: {
        'main@': 'collection'
      }
    })
    .state('home.collection.study', {
      url: '/study/{studyId}',
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
      url: '/cevents',
      resolve: {
        collectionEventTypes: [
          'CollectionEventType',
          'study',
          function(CollectionEventType, study) {
            return CollectionEventType.list(study.id)
              .then(function (pagedResults) {
                return pagedResults.items;
              });
          }]
      },
      views: {
        'participantDetails': 'ceventsList'
      }
    })
    .state('home.collection.study.participant.cevents.add', {
      url: '/cevent/add',
      views: {
        'main@': 'ceventGetType'
      }
    })
    .state('home.collection.study.participant.cevents.add.details', {
      url: '/{collectionEventTypeId}',
      resolve: {
        collectionEventType: resolveCollectionEventType,
        collectionEvent: resolveNewCollectionEvent
      },
      views: {
        'main@': 'ceventAdd'
      }
    })
    .state('home.collection.study.participant.cevents.details', {
      url: '/{collectionEventId}',
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
        collectionEventType: [
          'collectionEvent',
          'collectionEventTypes',
          function (collectionEvent, collectionEventTypes) {
            return findCollectionEventType(collectionEventTypes, collectionEvent.collectionEventTypeId);
          }
        ],
        specimen: resolveSpecimen
      },
      views: {
        'main@': 'specimenView'
      }
    });

  // FIXME: should be moved to state service, so this function can be used by other code
  /* @ngInject */
  function redirectToLogin($state) {
    return function (error) {
      if (error.status && (error.status === 401)) {
        $state.go('home.users.login', {}, { reload: true });
      }
    };
  }

  /* @ngInject */
  function resolveStudy($state, $transition$, Study) {
    return Study.get($transition$.params().studyId)
      .catch(redirectToLogin($state));
  }

  /* @ngInject */
  function resolveParticipantUniqueId($transition$) {
    return $transition$.params().uniqueId;
  }

  /* @ngInject */
  function resolveParticipant($transition$, Participant, study) {
    return Participant.get($transition$.params().studyId, $transition$.params().participantId)
      .then(function (p) {
        p.setStudy(study);
        return p;
      });
  }

  // /* @ngInject */
  // function resolveParticipantByUniqueId($transition$, Participant) {
  //   return Participant.getByUniqueId($transition$.params().studyId, $transition$.params().uniqueId);
  // }

  /* @ngInject */
  function resolveNewCollectionEvent(CollectionEvent,
                                     participant,
                                     collectionEventTypes,
                                     $transition$) {
    var cevent,
        ceventType = _.find(collectionEventTypes, { id: $transition$.params().collectionEventTypeId });

    if (!ceventType) {
      throw new Error('could not find collection event type');
    }
    cevent = new CollectionEvent({}, ceventType);
    cevent.participantId = participant.id;
    cevent.collectionEventTypeId = ceventType.id;
    return cevent;
  }

  /* @ngInject */
  function resolveCollectionEvent($q,
                                  $transition$,
                                  CollectionEvent,
                                  collectionEventTypes) {
    return CollectionEvent.get($transition$.params().collectionEventId)
      .then(function (cevent) {
        var ceventType = _.find(collectionEventTypes, { id: cevent.collectionEventTypeId });

        if (!ceventType) {
          return $q.reject('could not find collection event type');
        }

        cevent.setCollectionEventType(ceventType);
        return $q.when(cevent);
      });
  }

  /* @ngInject */
  function resolveSpecimen($transition$, Specimen) {
    return Specimen.getByInventoryId($transition$.params().inventoryId);
  }

  function findCollectionEventType(collectionEventTypes, id) {
    var collectionEventType = _.find(collectionEventTypes, { id: id });
    if (_.isUndefined(collectionEventType)) {
      throw new Error('could not find collection event type');
    }
    return collectionEventType;
  }

  /* @ngInject */
  function resolveCollectionEventType($transition$, collectionEventTypes) {
    return findCollectionEventType(collectionEventTypes,
                                   $transition$.params().collectionEventTypeId);
  }
}

export default ngModule => ngModule.config(config)
