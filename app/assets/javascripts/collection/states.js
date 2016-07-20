/**
 * collection routes.
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(['lodash'], function(_) {
  'use strict';

  config.$inject = ['$urlRouterProvider', '$stateProvider'];

  function config($urlRouterProvider, $stateProvider) {

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

    resolveCollectionEvent.$inject = [
      'CollectionEvent',
      'participant',
      'collectionEventTypes',
      '$stateParams'
    ];

    function resolveCollectionEvent(CollectionEvent,
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

    $urlRouterProvider.otherwise('/');

    $stateProvider.state('home.collection', {
      url: '^/collection',
      resolve: {
        studyCounts: resolveStudyCounts,
        centreCounts: resolveCentreCounts
      },
      views: {
        'main@': {
          template: '<collection study-counts="vm.studyCounts" centre-counts="vm.centreCounts"></collection>',
          controller: [
            'studyCounts',
            'centreCounts',
            function (studyCounts, centreCounts) {
              this.studyCounts = studyCounts;
              this.centreCounts = centreCounts;
            }
          ],
          controllerAs: 'vm'
        }
      },
      data: {
        displayName: 'Collection'
      }
    });

    $stateProvider.state('home.collection.study', {
      url: '/study/{studyId}',
      resolve: {
        study: resolveStudy
      },
      views: {
        'main@': {
          template: [
            '<participant-get',
            '  study="vm.study"',
            '</participant-get>'
          ].join(''),
          controller: [
            'study',
            function (study) {
              var vm = this;
              vm.study = study;
            }
          ],
          controllerAs: 'vm'
        }
      },
      data: {
        displayName: '{{study.name}}'
      }
    });

    $stateProvider.state('home.collection.study.participantAdd', {
      url: '/add/{uniqueId}',
      views: {
        'main@': {
          template: [
            '<participant-add',
            '  study="vm.study"',
            '  unique-id="{{vm.uniqueId}}">',
            '</participant-add>'
          ].join(''),
          controller: [
            '$stateParams',
            'study',
            function ($stateParams, study) {
              var vm = this;
              vm.study = study;
              vm.uniqueId = $stateParams.uniqueId;
            }
          ],
          controllerAs: 'vm'
        }
      },
      data: {
        displayName: 'Add participant'
      }
    });

    $stateProvider.state('home.collection.study.participant', {
      abstract: true,
      url: '/participant/{participantId}',
      resolve: {
        participant: resolveParticipant
      },
      views: {
        'main@': {
          template: [
            '<participant-view',
            '  study="vm.study"',
            '  participant="vm.participant">',
            '</participant-view>'
          ].join(''),
          controller: [
            'study',
            'participant',
            function (study, participant) {
              var vm = this;
              vm.study = study;
              vm.participant = participant;
            }
          ],
          controllerAs: 'vm'
        }
      },
      data: {
        breadcrumProxy: 'home.collection.study.participant.summary'
      }
    });

    $stateProvider.state('home.collection.study.participant.summary', {
      url: '/summary',
      views: {
        'participantDetails': {
          template: [
            '<participant-summary',
            '  study="vm.study"',
            '  participant="vm.participant">',
            '</participant-summary>'
          ].join(''),
          controller: [
            'study',
            'participant',
            function (study, participant) {
              var vm = this;
              vm.study = study;
              vm.participant = participant;
            }
          ],
          controllerAs: 'vm'
        }
      },
      data: {
        displayName: 'Participant {{participant.uniqueId}}'
      }
    });

    $stateProvider.state('home.collection.study.participant.cevents', {
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
          controller: [
            'participant',
            'collectionEventsPagedResult',
            'collectionEventTypes',
            function (participant, collectionEventsPagedResult, collectionEventTypes) {
              var vm = this;
              vm.participant = participant;
              vm.collectionEventsPagedResult = collectionEventsPagedResult;
              vm.collectionEventTypes = collectionEventTypes;
            }
          ],
          controllerAs: 'vm'
        }
      },
      data: {
        displayName: 'Participant {{participant.uniqueId}}'
      }
    });

    $stateProvider.state('home.collection.study.participant.cevents.add', {
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
          controller: [
            'study',
            'participant',
            'collectionEventTypes',
            function (study, participant, collectionEventTypes) {
              var vm = this;
              vm.study = study;
              vm.participant = participant;
              vm.collectionEventTypes = collectionEventTypes;
            }
          ],
          controllerAs: 'vm'
        }
      },
      data: {
        displayName: 'Add collection event'
      }
    });

    $stateProvider.state('home.collection.study.participant.cevents.add.details', {
      url: '/{collectionEventTypeId}',
      resolve: {
        collectionEvent: resolveCollectionEvent
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
          controller: [
            '$stateParams',
            'study',
            'participant',
            'collectionEventTypes',
            'collectionEvent',
            function ($stateParams, study, participant, collectionEventTypes, collectionEvent) {
              var vm = this;
              vm.study = study;
              vm.participant = participant;
              vm.collectionEvent = collectionEvent;

              vm.collectionEventType = _.find(collectionEventTypes,
                                                   { id: $stateParams.collectionEventTypeId});
              if (_.isUndefined(vm.collectionEventType)) {
                  throw new Error('could not find collection event type');
              }
            }
          ],
          controllerAs: 'vm'
        }
      },
      data: {
        displayName: '{{ collectionEvent.collectionEventType.name }}'
      }
    });

    $stateProvider.state('home.collection.study.participant.cevents.details', {
      url: '/{collectionEventId}',
      resolve: {
        collectionEvent: [
          '$q',
          '$stateParams',
          'CollectionEvent',
          'collectionEventTypes',
          function ($q,
                    $stateParams,
                    CollectionEvent,
                    collectionEventTypes) {
            return CollectionEvent.get($stateParams.collectionEventId)
              .then(function (cevent) {
                var deferred = $q.defer(),
                    ceventType = _.find(collectionEventTypes,
                                             { id: cevent.collectionEventTypeId });

                if (!ceventType) {
                  deferred.reject('could not find collection event type');
                } else {
                  cevent.setCollectionEventType(ceventType);
                  deferred.resolve(cevent);
                }
                return deferred.promise;
              });
          }
        ]
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
          controller: [
            'study',
            'collectionEventTypes',
            'collectionEvent',
            function (study, collectionEventTypes, collectionEvent) {
              var vm = this;
              vm.study = study;
              vm.collectionEventTypes = collectionEventTypes;
              vm.collectionEvent = collectionEvent;
            }
          ],
          controllerAs: 'vm'
        }
      },
      data: {
        displayName: 'Visit # {{ collectionEvent.visitNumber }}'
      }
    });

  }

  return config;
});
