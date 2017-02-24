/**
 * collection routes.
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(['lodash'], function(_) {
  'use strict';

  config.$inject = [
    '$urlRouterProvider',
    '$stateProvider'
  ];

  function config($urlRouterProvider, $stateProvider) {

    $stateProvider
      .state('home.collection', {
        url: '^/collection',
        resolve: {
          studyCounts: resolveStudyCounts,
          centreCounts: resolveCentreCounts
        },
        views: {
          'main@': {
            template: '<collection study-counts="vm.studyCounts" centre-counts="vm.centreCounts"></collection>',
            controller: CollectionCtrl,
            controllerAs: 'vm'
          }
        },
        data: {
          displayName: 'Collection'
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
        },
        data: {
          displayName: '{{study.name}}'
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
        },
        data: {
          displayName: 'Add participant'
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
        },
        data: {
          breadcrumProxy: 'home.collection.study.participant.summary'
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
        },
        data: {
          displayName: 'Participant {{participant.uniqueId}}'
        }
      })
      .state('home.collection.study.participant.cevents', {
        url: '/cevents',
        resolve: {
          collectionEventsPagedResult: resolveCollectionEventsPagedResult,
          collectionEventTypes: [
            'CollectionEventType',
            'study',
            function(CollectionEventType, study) {
              return CollectionEventType.list(study.id);
            }]
        },
        views: {
          'participantDetails': {
            template: [
              '<cevents-list',
              '  participant="vm.participant"',
              '  collection-events-paged-result="vm.collectionEventsPagedResult"',
              '  collection-event-types="vm.collectionEventTypes">',
              '</cevents-list>'
            ].join(''),
            controller: ParticipantCeventsController,
            controllerAs: 'vm'
          }
        },
        data: {
          displayName: 'Participant {{participant.uniqueId}}: Events'
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
        },
        data: {
          displayName: 'Add collection event'
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
        },
        data: {
          displayName: '{{ collectionEvent.collectionEventType.name }}'
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
        },
        data: {
          displayName: 'Visit # {{ collectionEvent.visitNumber }}'
        }
      });

    resolveStudyCounts.$inject = ['StudyCounts'];
    function resolveStudyCounts(StudyCounts) {
      return StudyCounts.get();
    }

    resolveCentreCounts.$inject = ['CentreCounts'];
    function resolveCentreCounts(CentreCounts) {
      return CentreCounts.get();
    }

    resolveStudy.$inject = ['$stateParams', 'Study'];
    function resolveStudy($stateParams, Study) {
      return Study.get($stateParams.studyId);
    }

    resolveParticipant.$inject = ['$stateParams', 'Participant', 'study'];
    function resolveParticipant($stateParams, Participant, study) {
      return Participant.get($stateParams.studyId, $stateParams.participantId)
        .then(function (p) {
          p.setStudy(study);
          return p;
        });
    }

    resolveParticipantByUniqueId.$inject = ['$stateParams', 'Participant'];
    function resolveParticipantByUniqueId($stateParams, Participant) {
      return Participant.getByUniqueId($stateParams.studyId, $stateParams.uniqueId);
    }

    resolveCollectionEventsPagedResult.$inject = ['CollectionEvent', 'participant'];
    function resolveCollectionEventsPagedResult(CollectionEvent, participant) {
      // returns all collection events for a participant
      return CollectionEvent.list(participant.id);
    }

    resolveNewCollectionEvent.$inject = [
      'CollectionEvent',
      'participant',
      'collectionEventTypes',
      '$stateParams'
    ];

    function resolveNewCollectionEvent(CollectionEvent,
                                    participant,
                                    collectionEventTypes,
                                    $stateParams) {
      var cevent,
          ceventType = _.find(collectionEventTypes, { id: $stateParams.collectionEventTypeId });

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
      '$stateParams',
      'CollectionEvent',
      'collectionEventTypes'
    ];

    function resolveCollectionEvent($q,
                                    $stateParams,
                                    CollectionEvent,
                                    collectionEventTypes) {
      return CollectionEvent.get($stateParams.collectionEventId)
        .then(function (cevent) {
          var deferred = $q.defer(),
              ceventType = _.find(collectionEventTypes, { id: cevent.collectionEventTypeId });

          if (!ceventType) {
            deferred.reject('could not find collection event type');
          } else {
            cevent.setCollectionEventType(ceventType);
            deferred.resolve(cevent);
          }
          return deferred.promise;
        });

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

    ParticipantAddCtrl.$inject = [ '$stateParams', 'study' ];

    function ParticipantAddCtrl($stateParams, study) {
      this.study    = study;
      this.uniqueId = $stateParams.uniqueId;
    }

    StudyParticipantCtrl.$inject = [ 'study', 'participant' ];

    function StudyParticipantCtrl(study, participant) {
      this.study = study;
      this.participant = participant;
    }

    ParticipantCeventsController.$inject = [
      'participant',
      'collectionEventsPagedResult',
      'collectionEventTypes'
    ];

    function ParticipantCeventsController(participant, collectionEventsPagedResult, collectionEventTypes) {
      this.participant = participant;
      this.collectionEventsPagedResult = collectionEventsPagedResult;
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
      '$stateParams',
      'study',
      'participant',
      'collectionEventTypes',
      'collectionEvent'
    ];

    function CeventAddCtrl($stateParams, study, participant, collectionEventTypes, collectionEvent) {
      this.study = study;
      this.participant = participant;
      this.collectionEvent = collectionEvent;

      this.collectionEventType = _.find(collectionEventTypes,
                                      { id: $stateParams.collectionEventTypeId});
      if (_.isUndefined(this.collectionEventType)) {
        throw new Error('could not find collection event type');
      }
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
