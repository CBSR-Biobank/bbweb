/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define([
  'angular',
  'angularMocks',
  'underscore',
  'faker',
  'moment',
  'sprintf'
], function(angular, mocks, _, faker, moment, sprintf) {
  'use strict';

  escribe('CollectionEvent', function() {

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function(entityTestSuite,
                               hasAnnotationsEntityTestSuite,
                               testUtils,
                               extendedDomainEntities) {
      var self = this;

      _.extend(self, entityTestSuite, hasAnnotationsEntityTestSuite);

      self.$httpBackend                  = self.$injector.get('$httpBackend');
      self.Participant                   = self.$injector.get('Participant');
      self.CollectionEventType           = self.$injector.get('CollectionEventType');
      self.CollectionEvent               = self.$injector.get('CollectionEvent');
      self.Annotation                    = self.$injector.get('Annotation');
      self.AnnotationValueType           = self.$injector.get('AnnotationValueType');
      self.AnnotationType                = self.$injector.get('AnnotationType');
      self.jsonEntities                  = self.$injector.get('jsonEntities');
      self.testUtils                     = self.$injector.get('testUtils');

      testUtils.addCustomMatchers();

      self.jsonStudy = self.jsonEntities.study();
      self.jsonCet = self.jsonEntities.collectionEventType();

      self.getCollectionEventEntities = getCollectionEventEntities;
      self.expectCevent = expectCevent;
      self.failTest = failTest;

      //--

      function getCollectionEventEntities(isNew) {
        var jsonAnnotationTypes = self.jsonEntities.allAnnotationTypes(),
            collectionEventType,
            initServerCollectionEvent,
            jsonCevent,
            collectionEvent;

        collectionEventType = self.CollectionEventType.create(
          self.jsonEntities.collectionEventType({ annotationTypes: jsonAnnotationTypes }));

        jsonCevent = self.jsonEntities.collectionEvent();
        initServerCollectionEvent = isNew ? _.omit(jsonCevent, 'id'): jsonCevent;
        collectionEvent = new self.CollectionEvent(initServerCollectionEvent, collectionEventType);

        return {
          jsonAnnotationTypes:   jsonAnnotationTypes,
          collectionEventType:   collectionEventType,
          collectionEvent:       collectionEvent
        };
      }

      // used by promise tests
      function expectCevent(entity) {
        expect(entity).toEqual(jasmine.any(self.CollectionEvent));
      }

      // used by promise tests
      function failTest(error) {
        expect(error).toBeUndefined();
      }
    }));

    afterEach(function() {
      this.$httpBackend.verifyNoOutstandingExpectation();
      this.$httpBackend.verifyNoOutstandingRequest();
    });

    it('constructor with default parameters has default values', function() {
      var ceventType = new this.CollectionEventType(this.jsonEntities.collectionEventType()),
          collectionEvent = new this.CollectionEvent({}, ceventType);

      expect(collectionEvent.id).toBeNull();
      expect(collectionEvent.version).toBe(0);
      expect(collectionEvent.timeAdded).toBeNull();
      expect(collectionEvent.timeModified).toBeNull();
      expect(collectionEvent.timeCompleted).toBeNull();
      expect(collectionEvent.visitNumber).toBeNull();
    });

    it('constructor with annotation parameter has valid values', function() {
      var self                = this,
          annotationData      = self.jsonAnnotationData(),
          jsonAnnotations     = _.pluck(annotationData, 'annotation'),
          jsonAnnotationTypes = _.pluck(annotationData, 'annotationType'),
          ceventType;

      self.jsonCet = self.jsonEntities.collectionEventType({ annotationTypes: jsonAnnotationTypes });

      ceventType = new self.CollectionEventType(self.jsonCet);

      var collectionEvent = new self.CollectionEvent({ annotations: jsonAnnotations },
                                                     ceventType,
                                                     jsonAnnotationTypes);


      _.each(ceventType.annotationTypes, function (annotationType) {
        var annotation = _.findWhere(collectionEvent.annotations,
                                     { annotationTypeId: annotationType.uniqueId }),
            jsonAnnotation = _.findWhere(jsonAnnotations,
                                         { annotationTypeId: annotationType.uniqueId});
        self.validateAnnotationClass(annotationType, annotation);
        annotation.compareToJsonEntity(jsonAnnotation);
        expect(annotation.required).toBe(annotationType.required);
      });
    });

    it('constructor with NO annotations has valid values', function() {
      var self = this,
          annotationData = self.jsonAnnotationData(),
          jsonAnnotationTypes = _.pluck(annotationData, 'annotationType'),
          ceventType;

      self.jsonCet = self.jsonEntities.collectionEventType({ annotationTypes: jsonAnnotationTypes });
      ceventType = self.CollectionEventType.create(self.jsonCet);

      var collectionEvent = new self.CollectionEvent({ }, ceventType, jsonAnnotationTypes);

      expect(collectionEvent.annotations).toBeArrayOfSize(annotationData.length);
      _.each(ceventType.annotationTypes, function (annotationType) {
        var annotation = _.findWhere(collectionEvent.annotations,
                                     { annotationTypeId: annotationType.uniqueId });
        self.validateAnnotationClass(annotationType, annotation);
        expect(annotation.required).toBe(annotationType.required);
      });
    });

    it('fails when constructing with invalid annotation parameter', function() {
      var self = this,
          jsonAnnotation = {},
          ceventType;

      var annotationType = new self.AnnotationType(self.jsonEntities.annotationType());

      // put an invalid value in jsonAnnotation.annotationTypeId
      _.extend(
        jsonAnnotation,
        self.jsonEntities.annotation(self.jsonEntities.valueForAnnotation(annotationType), annotationType),
        { annotationTypeId: self.jsonEntities.stringNext() });

      self.jsonCet = self.jsonEntities.collectionEventType(self.jsonStudy,
                                                       { annotationTypes: [annotationType] });

      ceventType = self.CollectionEventType.create(self.jsonCet);

      expect(function () {
        return new self.CollectionEvent({ annotations: [ jsonAnnotation ] }, ceventType);
      }).toThrowError('annotation types not found: ' + jsonAnnotation.annotationTypeId);
    });

    it('fails when constructing with invalid collection event type', function() {
      var self = this,
          serverCollectionEvent,
          ceventType;

      serverCollectionEvent = self.jsonEntities.collectionEvent({
        collectionEventTypeId: self.jsonEntities.domainEntityNameNext(
          self.jsonEntities.ENTITY_NAME_COLLECTION_EVENT_TYPE())
      });
      ceventType = self.CollectionEventType.create(
        self.jsonEntities.collectionEventType(self.jsonStudy));

      expect(function () {
        return new self.CollectionEvent(serverCollectionEvent, ceventType);
      }).toThrowError('invalid collection event type');
    });

    it('fails when creating from a non object', function() {
      var self = this;

      expect(function () {
        return new self.CollectionEvent.create(1);
      }).toThrowError(/invalid object from server/);
    });

    it('fails when creating from an object with invalid keys', function() {
      var self = this,
          serverObj = { tmp: 1 };
      expect(function () {
        return new self.CollectionEvent.create(serverObj);
      }).toThrowError(/invalid object from server/);
    });

    it('fails when creating from an object with annotation with invalid keys', function() {
      var self = this,
          jsonCevent = _.extend(this.jsonEntities.collectionEvent(),
                                { annotations: [{ tmp: 1 }] });

      expect(function () {
        return new self.CollectionEvent.create(jsonCevent);
      }).toThrowError(/invalid object.*bad annotations/);
    });

    it('has valid values when creating from a server response', function() {
      var annotationData    = this.jsonAnnotationData(),
          annotationTypes   = _.pluck(annotationData, 'annotationType'),
          jsonCet           = this.jsonEntities.collectionEventType({annotationTypes: annotationTypes}),
          cet               = new this.CollectionEventType(jsonCet),
          jsonCevent        = this.jsonEntities.collectionEvent({annotationTypes: annotationTypes});

      var collectionEvent = this.CollectionEvent.create(jsonCevent, cet);
      collectionEvent.compareToJsonEntity(jsonCevent);
    });

    it('can retrieve a single collection event', function() {
      var self = this,
          collectionEvent = self.jsonEntities.collectionEvent();

      self.$httpBackend.whenGET(uri(collectionEvent.id))
        .respond(serverReply(collectionEvent));

      self.CollectionEvent.get(collectionEvent.id).then(function (reply) {
        expect(reply).toEqual(jasmine.any(self.CollectionEvent));
        reply.compareToJsonEntity(collectionEvent);
      });
      self.$httpBackend.flush();
    });

    it('get fails for and invalid collection event id', function() {
      var self = this,
          collectionEventId = self.jsonEntities.stringNext();

      self.$httpBackend.whenGET(uri(collectionEventId))
        .respond(404, { status: 'error', message: 'invalid id' });

      self.CollectionEvent.get(collectionEventId)
        .then(function (reply) { fail('should not be called'); })
        .catch(function (err) { expect(err.data.message).toContain('invalid id'); });
      self.$httpBackend.flush();
    });

    it('can list collection events for a participant', function() {
      var self = this,
          participant = self.jsonEntities.defaultParticipant(),
          collectionEvents = _.map(_.range(2), function () {
            return self.jsonEntities.collectionEvent();
          }),
          reply = self.jsonEntities.pagedResult(collectionEvents),
          serverEntity;

      self.$httpBackend.whenGET(uriWithPath('list', participant.id))
        .respond(serverReply(reply));

      self.CollectionEvent.list(participant.id).then(function (pagedResult) {
        expect(pagedResult.items).toBeArrayOfSize(collectionEvents.length);

        _.each(pagedResult.items, function(obj) {
          expect(obj).toEqual(jasmine.any(self.CollectionEvent));
          serverEntity = _.findWhere(collectionEvents, { id: obj.id });
          expect(serverEntity).toBeDefined();
          obj.compareToJsonEntity(serverEntity);
        });
      });
      self.$httpBackend.flush();
    });

    it('can list collection events sorted by corresponding fields', function() {
      var self = this,
          study = self.jsonEntities.study(),
          participant = self.jsonEntities.participant({ studyId: study.id }),
          reply = self.jsonEntities.pagedResult([]),
          sortFields = [ 'visitNumber', 'timeCompleted'];

      _.each(sortFields, function (sortField) {
        self.$httpBackend.whenGET(uriWithPath('list', participant.id) + '?sort=' + sortField)
          .respond(serverReply(reply));

        self.CollectionEvent.list(participant.id, { sort: sortField }).then(function (pagedResult) {
          expect(pagedResult.items).toBeEmptyArray();
        });
        self.$httpBackend.flush();
      });
    });

    it('can list collection events using a page number', function() {
      var self = this,
          study = self.jsonEntities.study(),
          participant = self.jsonEntities.participant({ studyId: study.id }),
          reply = self.jsonEntities.pagedResult([]),
          pageNumber = 2;

      self.$httpBackend.whenGET(uriWithPath('list', participant.id) + '?page=' + pageNumber)
        .respond(serverReply(reply));

      self.CollectionEvent.list(participant.id, { page: pageNumber }).then(function (pagedResult) {
        expect(pagedResult.items).toBeEmptyArray();
      });
      self.$httpBackend.flush();
    });

    it('can list collection events using a page size', function() {
      var self = this,
          study = self.jsonEntities.study(),
          participant = self.jsonEntities.participant({ studyId: study.id }),
          reply = self.jsonEntities.pagedResult([]),
          pageSize = 2;

      self.$httpBackend.whenGET(uriWithPath('list', participant.id) + '?pageSize=' + pageSize)
        .respond(serverReply(reply));

      self.CollectionEvent.list(participant.id, { pageSize: pageSize }).then(function (pagedResult) {
        expect(pagedResult.items).toBeEmptyArray();
      });
      self.$httpBackend.flush();
    });

    it('can retrieve a single collection event by visit number', function() {
      var self            = this,
          entities        = self.getCollectionEventEntities(true),
          jsonParticipant = self.jsonEntities.defaultParticipant(),
          jsonCevent      = self.jsonEntities.defaultCollectionEvent();

      self.$httpBackend.whenGET(uri(jsonParticipant.id) + '/visitNumber/' + jsonCevent.visitNumber)
        .respond(serverReply(jsonCevent));

      self.CollectionEvent.getByVisitNumber(jsonParticipant.id,
                                            jsonCevent.visitNumber,
                                            entities.collectionEventType,
                                            entities.annotationTypes)
        .then(function (reply) {
          expect(reply).toEqual(jasmine.any(self.CollectionEvent));
          reply.compareToJsonEntity(jsonCevent);
        });
      self.$httpBackend.flush();
    });

    it('can list collection events using ordering', function() {
      var self = this,
          study = self.jsonEntities.study(),
          participant = self.jsonEntities.participant({ studyId: study.id }),
          reply = self.jsonEntities.pagedResult([]),
          orderingTypes = [ 'asc', 'desc'];

      _.each(orderingTypes, function (orderingType) {
        self.$httpBackend.whenGET(uriWithPath('list', participant.id) + '?order=' + orderingType)
          .respond(serverReply(reply));

        self.CollectionEvent.list(participant.id, { order: orderingType }).then(function (pagedResult) {
          expect(pagedResult.items).toBeEmptyArray();
        });
        self.$httpBackend.flush();
      });
    });

    it('setting annotation types fails when it does not belong to collection event type',
        function() {
          var annotationData    = this.jsonAnnotationData(),
              annotationTypes     = _.pluck(annotationData, 'annotationType'),
              badAnnotationTypeId = 'bad-annotation-type-id',
              ceventType,
              collectionEvent;

          ceventType = this.CollectionEventType.create(
            this.jsonEntities.collectionEventType({
              annotationTypes: annotationTypes
            }));
          collectionEvent = new this.CollectionEvent({}, ceventType);

          // replace id with a bad one
          annotationTypes[0].uniqueId = badAnnotationTypeId;
          expect(function () {
            collectionEvent.setAnnotationTypes(annotationTypes);
          }).toThrowError(/annotation types not found/);
        });

    it('can add a collectionEvent', function() {
      var jsonCevent      = this.jsonEntities.collectionEvent(),
          collectionEvent = new this.CollectionEvent(_.omit(jsonCevent, 'id')),
          cmd             = addCommand(collectionEvent);

      this.$httpBackend.expectPOST(uri(jsonCevent.participantId), cmd).respond(201, serverReply(jsonCevent));

      collectionEvent.add().then(function(reply) {
        _.extend(collectionEvent, { id: reply.id });
        collectionEvent.compareToJsonEntity(reply);
      });
      this.$httpBackend.flush();
    });

    it('can add a collection event with annotations', function() {
      var entities = this.getCollectionEventEntities(true),
          cmd      = addCommand(entities.collectionEvent);

      this.$httpBackend.expectPOST(uri(entities.collectionEvent.participantId), cmd)
        .respond(201, serverReply(entities.serverCollectionEvent));

      entities.collectionEvent.add().then(function(reply) {
        expect(reply.id).toEqual(entities.serverCollectionEvent.id);
        expect(reply.version).toEqual(0);
        expect(reply.participantId).toEqual(entities.participant.id);
        expect(reply.timeCompleted).toEqual(entities.serverCollectionEvent.timeCompleted);
        expect(reply.visitNumber).toEqual(entities.serverCollectionEvent.visitNumber);
        expect(reply.annotations)
          .toBeArrayOfSize(entities.serverCollectionEvent.annotations.length);
      });
      this.$httpBackend.flush();
    });

    it('can not add a collection event with empty required annotations', function() {
      var self = this,
          jsonAnnotationTypes = self.jsonEntities.allAnnotationTypes();

      _.each(jsonAnnotationTypes, function (jsonAnnotType) {
        var jsonAnnotation, annotationType, ceventType, jsonCevent, collectionEvent;

        jsonAnnotation = self.jsonEntities.annotation({ value: null,
                                                        annotationTypeId: jsonAnnotType.uniqueId});

        annotationType = new self.AnnotationType(jsonAnnotType);
        ceventType = self.CollectionEventType.create(
          self.jsonEntities.collectionEventType({ annotationTypes: [ annotationType ] }));
        ceventType.annotationTypes[0].required = true;
        jsonCevent = _.omit(self.jsonEntities.collectionEvent({ annotations: [ jsonAnnotation ]}), 'id)');
        collectionEvent = new self.CollectionEvent(jsonCevent, ceventType);

        _.each(collectionEvent.annotations, function (annotation) {
          expect(annotation.getValue()).toBeFalsy();
        });

        expect(function () {
          collectionEvent.add();
        }).toThrowError(/required annotation has no value/);
      });
    });

    it('can update the visit number on a collectionEvent', function() {
      var entities = this.getCollectionEventEntities(false),
          cevent   = entities.collectionEvent;

      this.updateEntity(cevent,
                        'updateVisitNumber',
                        cevent.visitNumber,
                        uriWithPath('visitNumber', cevent.id),
                        { visitNumber: cevent.visitNumber },
                        this.jsonEntities.defaultCollectionEvent(),
                        this.expectCevent,
                        this.failTest);
    });

    it('can update the time completed on a collectionEvent', function() {
      var entities = this.getCollectionEventEntities(false),
          cevent   = entities.collectionEvent;

      this.updateEntity(cevent,
                        'updateTimeCompleted',
                        cevent.timeCompleted,
                        uriWithPath('timeCompleted', cevent.id),
                        { timeCompleted: cevent.timeCompleted },
                        this.jsonEntities.defaultCollectionEvent(),
                        this.expectCevent,
                        this.failTest);
    });

    it('should be able to remove a collection event', function() {
      var entities = this.getCollectionEventEntities(false),
          cevent = entities.collectionEvent,
          url = uri(cevent.participantId, cevent.id, cevent.version);

      this.$httpBackend.expectDELETE(url).respond(201, serverReply(true));

      cevent.remove();
      this.$httpBackend.flush();
    });

    var addCommandKeys = [
      'participantId',
      'collectionEventTypeId',
      'timeCompleted',
      'visitNumber'
    ];

    function addCommand(collectionEvent) {
      return _.extend(_.pick(collectionEvent, addCommandKeys),
                      { annotations: annotationsForCommand(collectionEvent) } );
    }

    function annotationsForCommand(collectionEvent) {
      return _.map(collectionEvent.annotations, function (annotation) {
        return annotation.getServerAnnotation();
      });
    }

    function serverReply(event) {
      return { status: 'success', data: event };
    }

    function uri(/* participantId, collectionEventId, version */) {
      var participantId,
          collectionEventId,
          version,
          result = '/participants/cevents',
          args = _.toArray(arguments);

      if (args.length > 0) {
        participantId = args.shift();
        result += '/' + participantId;
      }

      if (args.length > 0) {
        collectionEventId = args.shift();
        result += '/' + collectionEventId;
      }

      if (args.length > 0) {
        version = args.shift();
        result += '/' + version;
      }
      return result;
    }

    function uriWithPath(/* path, collectionEventId */) {
      var path,
          collectionEventId,
          result = '/participants/cevents',
          args = _.toArray(arguments);

      if (args.length > 0) {
        path = args.shift();
        result += '/' + path;
      }

      if (args.length > 0) {
        collectionEventId = args.shift();
        result += '/' + collectionEventId;
      }
      return result;
    }
  });

});
