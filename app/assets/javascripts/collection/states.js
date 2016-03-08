/**
 * collection routes.
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(['underscore'], function(_) {
  'use strict';

  config.$inject = ['$urlRouterProvider', '$stateProvider', 'authorizationProvider'];

  function config($urlRouterProvider, $stateProvider, authorizationProvider) {

    resolveStudyCounts.$inject = ['StudyCounts'];
    function resolveStudyCounts(StudyCounts) {
      return StudyCounts.get();
    }

    resolveStudy.$inject = ['$stateParams', 'Study'];
    function resolveStudy($stateParams, Study) {
      return Study.get($stateParams.studyId);
    }

    resolveParticipant.$inject = ['$stateParams', 'Participant', 'study', 'annotationTypes'];
    function resolveParticipant($stateParams, Participant, study, annotationTypes) {
      return Participant.get($stateParams.studyId, $stateParams.participantId).then(
        function (p) {
          p.setStudy(study);
          p.setAnnotationTypes(annotationTypes);
          return p;
        });
    }

    resolveParticipantByUniqueId.$inject = ['$stateParams', 'Participant'];
    function resolveParticipantByUniqueId($stateParams, Participant) {
      return Participant.getByUniqueId($stateParams.studyId, $stateParams.uniqueId);
    }

    resolveAnnotationTypes.$inject = ['$stateParams', 'ParticipantAnnotationType'];
    function resolveAnnotationTypes($stateParams, ParticipantAnnotationType) {
      if ($stateParams.studyId) {
        return ParticipantAnnotationType.list($stateParams.studyId);
      }
      throw new Error('state parameter studyId is invalid');
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
      'annotationTypes',
      '$stateParams'
    ];

    function resolveCollectionEvent(CollectionEvent,
                                    participant,
                                    collectionEventTypes,
                                    annotationTypes,
                                    $stateParams) {
      var cevent,
          ceventType = _.findWhere(collectionEventTypes, { id: $stateParams.collectionEventTypeId });

      if (!ceventType) {
        throw new Error('could not find collection event type');
      }
      cevent = new CollectionEvent({}, ceventType, annotationTypes);
      cevent.participantId = participant.id;
      cevent.collectionEventTypeId = ceventType.id;
      return cevent;
    }

    $urlRouterProvider.otherwise('/');

    $stateProvider.state('home.collection', {
      url: '^/collection',
      resolve: {
        user: authorizationProvider.requireAuthenticatedUser,
        studyCounts: resolveStudyCounts
      },
      views: {
        'main@': {
          template: [
            '<collection',
            '  study-counts="vm.studyCounts">',
            '</collection>'
          ].join(''),
          controller: [
            'studyCounts',
            function (studyCounts) {
              var vm = this;
              vm.studyCounts = studyCounts;
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
      url: '/{studyId}',
      resolve: {
        user: authorizationProvider.requireAuthenticatedUser,
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

    $stateProvider.state('home.collection.study.addParticipant', {
      url: '/add/{uniqueId}',
      resolve: {
        user: authorizationProvider.requireAuthenticatedUser,
        annotationTypes: resolveAnnotationTypes
      },
      views: {
        'main@': {
          template: [
            '<participant-add',
            '  study="vm.study"',
            '  annotation-types="vm.annotationTypes"',
            '  unique-id="{{vm.uniqueId}}">',
            '</participant-add>'
          ].join(''),
          controller: [
            '$stateParams',
            'study',
            'annotationTypes',
            function ($stateParams, study, annotationTypes) {
              var vm = this;
              vm.study = study;
              vm.annotationTypes = annotationTypes;
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
      url: '/{participantId}',
      resolve: {
        user: authorizationProvider.requireAuthenticatedUser,
        annotationTypes: resolveAnnotationTypes, // maybe load this in the controller?
        participant: resolveParticipant
      },
      views: {
        'main@': {
          template: [
            '<participant-view',
            '  study="vm.study"',
            '  annotation-types="vm.annotationTypes"',
            '  participant="vm.participant">',
            '</participant-view>'
          ].join(''),
          controller: [
            'study',
            'annotationTypes',
            'participant',
            function (study, annotationTypes, participant) {
              var vm = this;
              vm.study = study;
              vm.annotationTypes = annotationTypes;
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
      resolve: {
        user: authorizationProvider.requireAuthenticatedUser
      },
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
        user: authorizationProvider.requireAuthenticatedUser,
        collectionEventsPagedResult: resolveCollectionEventsPagedResult,
        collectionEventTypes: [
          'CollectionEventType',
          'study',
          function(CollectionEventType, study) {
            return CollectionEventType.list(study.id);
          }],
        annotationTypes: [
          'CollectionEventAnnotationType',
          'study',
          function(CollectionEventAnnotationType, study) {
            return CollectionEventAnnotationType.list(study.id);
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
      resolve: {
        user: authorizationProvider.requireAuthenticatedUser
      },
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
        user: authorizationProvider.requireAuthenticatedUser,
        collectionEvent: resolveCollectionEvent
      },
      views: {
        'main@': {
          template: [
            '<cevent-add',
            '  study="vm.study"',
            '  participant="vm.participant"',
            '  collection-event-annotation-types="vm.annotationTypes"',
            '  collection-event="vm.collectionEvent">',
            '</cevent-add>'
          ].join(''),
          controller: [
            'study',
            'participant',
            'annotationTypes',
            'collectionEvent',
            function (study, participant, annotationTypes, collectionEvent) {
              var vm = this;
              vm.study = study;
              vm.participant = participant;
              vm.collectionEventAnnotationTypes = annotationTypes;
              vm.collectionEvent = collectionEvent;
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
        user: authorizationProvider.requireAuthenticatedUser,
        collectionEvent: [
          '$stateParams',
          'CollectionEvent',
          'participant',
          'collectionEventTypes',
          'annotationTypes',
          function ($stateParams,
                    CollectionEvent,
                    participant,
                    collectionEventTypes,
                    annotationTypes) {
            return CollectionEvent.get(participant.id, $stateParams.collectionEventId)
              .then(function (cevent) {
                var ceventType = _.findWhere(collectionEventTypes,
                                             { id: cevent.collectionEventTypeId });

                if (!ceventType) {
                  throw new Error('could not find collection event type');
                }

                cevent.setCollectionEventType(ceventType);
                cevent.setAnnotationTypes(annotationTypes);
                return cevent;
              });
          }
        ]
      },
      views: {
        'eventDetails': {
          template: [
            '<cevent-view',
            '  collection-event="vm.collectionEvent">',
            '</cevents-view>'
          ].join(''),
          controller: [
            'collectionEvent',
            function ( collectionEvent) {
              var vm = this;
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
