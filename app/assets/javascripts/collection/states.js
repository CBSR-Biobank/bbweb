/**
 * collection routes.
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(['lodash'], function(_) {
  'use strict';

  config.$inject = [ '$stateProvider' ];

  function config($stateProvider) {

    $stateProvider
      .state('home.collection', {
        url: '^/collection',
        views: {
          'main@': {
            component: 'collection'
          }
        }
      })
      .state('home.collection.study', {
        url: '/study/{studyId}',
        resolve: {
          study: resolveStudy
        },
        views: {
          'main@': {
            template: '<participant-get study="vm.study"></participant-get>',
            controller: CollectionStudyCtrl,
            controllerAs: 'vm'
          }
        }
      })
      .state('home.collection.study.participantAdd', {
        url: '/add/{uniqueId}',
        views: {
          'main@': {
            template: '<participant-add study="vm.study" unique-id="{{vm.uniqueId}}"></participant-add>',
            controller: ParticipantAddCtrl,
            controllerAs: 'vm'
          }
        }
      })
      .state('home.collection.study.participant', {
        abstract: true,
        url: '/participant/{participantId}',
        resolve: {
          participant: resolveParticipant
        },
        views: {
          'main@': {
            template: '<participant-view study="vm.study" participant="vm.participant"></participant-view>',
            controller: StudyParticipantCtrl,
            controllerAs: 'vm'
          }
        }
      })
      .state('home.collection.study.participant.summary', {
        url: '/summary',
        views: {
          'participantDetails': {
            template:
            '<participant-summary study="vm.study" participant="vm.participant"> </participant-summary>',
            controller: StudyParticipantCtrl,
            controllerAs: 'vm'
          }
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
          'participantDetails': {
            template: [
              '<cevents-list',
              '  participant="vm.participant"',
              '  collection-event-types="vm.collectionEventTypes">',
              '</cevents-list>'
            ].join(''),
            controller: ParticipantCeventsController,
            controllerAs: 'vm'
          }
        }
      })
      .state('home.collection.study.participant.cevents.add', {
        url: '/cevent/add',
        views: {
          'main@': {
            template: [
              '<cevent-get-type',
              '  study="vm.study"',
              '  participant="vm.participant"',
              '  collection-event-types="vm.collectionEventTypes">',
              '</ceven-get-type>'
            ].join(''),
            controller: ParticipantCeventsAddController,
            controllerAs: 'vm'
          }
        }
      })
      .state('home.collection.study.participant.cevents.add.details', {
        url: '/{collectionEventTypeId}',
        resolve: {
          collectionEvent: resolveNewCollectionEvent
        },
        views: {
          'main@': {
            template: [
              '<cevent-add',
              '  study="vm.study"',
              '  participant="vm.participant"',
              '  collection-event-type="vm.collectionEventType">',
              '  collection-event="vm.collectionEvent">',
              '</cevent-add>'
            ].join(''),
            controller: CeventAddCtrl,
            controllerAs: 'vm'
          }
        }
      })
      .state('home.collection.study.participant.cevents.details', {
        url: '/{collectionEventId}',
        resolve: {
          collectionEvent: resolveCollectionEvent
        },
        views: {
          'eventDetails': {
            template: [
              '<cevent-view',
              '  study="vm.study"',
              '  collection-event-types="vm.collectionEventTypes"',
              '  collection-event="vm.collectionEvent">',
              '</cevents-view>'
            ].join(''),
            controller: CeventDetailsCtrl,
            controllerAs: 'vm'
          }
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
          'main@': {
            component: 'specimenView'
          }
        }
      });

    resolveStudy.$inject = ['$transition$', 'Study'];
    function resolveStudy($transition$, Study) {
      return Study.get($transition$.params().studyId);
    }

    resolveParticipant.$inject = ['$transition$', 'Participant', 'study'];
    function resolveParticipant($transition$, Participant, study) {
      return Participant.get($transition$.params().studyId, $transition$.params().participantId)
        .then(function (p) {
          p.setStudy(study);
          return p;
        });
    }

    resolveParticipantByUniqueId.$inject = ['$transition$', 'Participant'];
    function resolveParticipantByUniqueId($transition$, Participant) {
      return Participant.getByUniqueId($transition$.params().studyId, $transition$.params().uniqueId);
    }

    resolveNewCollectionEvent.$inject = [
      'CollectionEvent',
      'participant',
      'collectionEventTypes',
      '$transition$'
    ];

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

    resolveCollectionEvent.$inject = [
      '$q',
      '$transition$',
      'CollectionEvent',
      'collectionEventTypes'
    ];

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

    resolveSpecimen.$inject = [ '$transition$', 'Specimen' ];

    function resolveSpecimen($transition$, Specimen) {
      return Specimen.getByInventoryId($transition$.params().inventoryId);
    }

    CollectionCtrl.$inject = [ 'studyCounts', 'centreCounts' ];

    function CollectionCtrl(studyCounts, centreCounts) {
      this.studyCounts = studyCounts;
      this.centreCounts = centreCounts;
    }

    CollectionStudyCtrl.$inject = [ 'study' ];

    function CollectionStudyCtrl(study) {
      this.study = study;
    }

    ParticipantAddCtrl.$inject = [ '$transition$', 'study' ];

    function ParticipantAddCtrl($transition$, study) {
      this.study    = study;
      this.uniqueId = $transition$.params().uniqueId;
    }

    StudyParticipantCtrl.$inject = [ 'study', 'participant' ];

    function StudyParticipantCtrl(study, participant) {
      this.study = study;
      this.participant = participant;
    }

    ParticipantCeventsController.$inject = [
      'participant',
      'collectionEventTypes'
    ];

    function ParticipantCeventsController(participant, collectionEventTypes) {
      this.participant = participant;
      this.collectionEventTypes = collectionEventTypes;
    }

    ParticipantCeventsAddController.$inject = [
      'study',
      'participant',
      'collectionEventTypes'
    ];

    function ParticipantCeventsAddController(study, participant, collectionEventTypes) {
      this.study = study;
      this.participant = participant;
      this.collectionEventTypes = collectionEventTypes;
    }

    CeventAddCtrl.$inject = [
      '$transition$',
      'study',
      'participant',
      'collectionEventTypes',
      'collectionEvent'
    ];

    function findCollectionEventType(collectionEventTypes, id) {
      var collectionEventType = _.find(collectionEventTypes, { id: id });
      if (_.isUndefined(collectionEventType)) {
        throw new Error('could not find collection event type');
      }
      return collectionEventType;
    }

    function CeventAddCtrl($transition$, study, participant, collectionEventTypes, collectionEvent) {
      this.study = study;
      this.participant = participant;
      this.collectionEvent = collectionEvent;

      this.collectionEventType = findCollectionEventType(collectionEventTypes,
                                                         $transition$.params().collectionEventTypeId);
    }

    CeventDetailsCtrl.$inject = [
      'study',
      'collectionEventTypes',
      'collectionEvent'
    ];

    function CeventDetailsCtrl(study, collectionEventTypes, collectionEvent) {
      this.study = study;
      this.collectionEventTypes = collectionEventTypes;
      this.collectionEvent = collectionEvent;
    }
  }

  return config;
});
