/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(function (require) {
  'use strict';

  var mocks   = require('angularMocks'),
      _       = require('lodash'),
      sprintf = require('sprintf-js').sprintf;

  describe('CollectionEvent', function() {

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function(EntityTestSuiteMixin,
                               ServerReplyMixin,
                               AnnotationsEntityTestSuiteMixin,
                               testUtils,
                               extendedDomainEntities) {
      var self = this;

      _.extend(self,
               EntityTestSuiteMixin.prototype,
               ServerReplyMixin.prototype,
               AnnotationsEntityTestSuiteMixin.prototype);

      self.injectDependencies('$httpBackend',
                              '$httpParamSerializer',
                              'Participant',
                              'CollectionEventType',
                              'CollectionEvent',
                              'Annotation',
                              'AnnotationValueType',
                              'AnnotationType',
                              'factory',
                              'testUtils');

      testUtils.addCustomMatchers();

      self.jsonStudy = self.factory.study();
      self.jsonCet = self.factory.collectionEventType();

      self.getCollectionEventEntities = getCollectionEventEntities;
      self.expectCevent = expectCevent;
      self.failTest = failTest;

      //--

      function getCollectionEventEntities(isNew) {
        var jsonAnnotationTypes = self.factory.allAnnotationTypes(),
            collectionEventType,
            initServerCollectionEvent,
            jsonCevent,
            collectionEvent;

        collectionEventType = self.CollectionEventType.create(
          self.factory.collectionEventType({ annotationTypes: jsonAnnotationTypes }));

        jsonCevent = self.factory.collectionEvent();
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
      var ceventType = new this.CollectionEventType(this.factory.collectionEventType()),
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
          jsonAnnotations     = _.map(annotationData, 'annotation'),
          jsonAnnotationTypes = _.map(annotationData, 'annotationType'),
          ceventType;

      self.jsonCet = self.factory.collectionEventType({ annotationTypes: jsonAnnotationTypes });

      ceventType = new self.CollectionEventType(self.jsonCet);

      var collectionEvent = new self.CollectionEvent({ annotations: jsonAnnotations },
                                                     ceventType,
                                                     jsonAnnotationTypes);


      _.each(ceventType.annotationTypes, function (annotationType) {
        var annotation = _.find(collectionEvent.annotations,
                                     { annotationTypeId: annotationType.uniqueId }),
            jsonAnnotation = _.find(jsonAnnotations,
                                         { annotationTypeId: annotationType.uniqueId});
        self.validateAnnotationClass(annotationType, annotation);
        annotation.compareToJsonEntity(jsonAnnotation);
        expect(annotation.required).toBe(annotationType.required);
      });
    });

    it('constructor with NO annotations has valid values', function() {
      var self = this,
          annotationData = self.jsonAnnotationData(),
          jsonAnnotationTypes = _.map(annotationData, 'annotationType'),
          ceventType;

      self.jsonCet = self.factory.collectionEventType({ annotationTypes: jsonAnnotationTypes });
      ceventType = self.CollectionEventType.create(self.jsonCet);

      var collectionEvent = new self.CollectionEvent({ }, ceventType, jsonAnnotationTypes);

      expect(collectionEvent.annotations).toBeArrayOfSize(annotationData.length);
      _.each(ceventType.annotationTypes, function (annotationType) {
        var annotation = _.find(collectionEvent.annotations,
                                     { annotationTypeId: annotationType.uniqueId });
        self.validateAnnotationClass(annotationType, annotation);
        expect(annotation.required).toBe(annotationType.required);
      });
    });

    it('fails when constructing with invalid annotation parameter', function() {
      var self = this,
          jsonAnnotation = {},
          ceventType;

      var annotationType = new self.AnnotationType(self.factory.annotationType());

      // put an invalid value in jsonAnnotation.annotationTypeId
      _.extend(
        jsonAnnotation,
        self.factory.annotation(self.factory.valueForAnnotation(annotationType), annotationType),
        { annotationTypeId: self.factory.stringNext() });

      self.jsonCet = self.factory.collectionEventType(self.jsonStudy,
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

      serverCollectionEvent = self.factory.collectionEvent({
        collectionEventTypeId: self.factory.domainEntityNameNext(
          self.factory.ENTITY_NAME_COLLECTION_EVENT_TYPE())
      });
      ceventType = self.CollectionEventType.create(
        self.factory.collectionEventType(self.jsonStudy));

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
          jsonCevent = _.extend(this.factory.collectionEvent(),
                                { annotations: [{ tmp: 1 }] });

      expect(function () {
        return new self.CollectionEvent.create(jsonCevent);
      }).toThrowError(/invalid object.*bad annotations/);
    });

    it('has valid values when creating from a server response', function() {
      var annotationData    = this.jsonAnnotationData(),
          annotationTypes   = _.map(annotationData, 'annotationType'),
          jsonCet           = this.factory.collectionEventType({annotationTypes: annotationTypes}),
          cet               = new this.CollectionEventType(jsonCet),
          jsonCevent        = this.factory.collectionEvent({annotationTypes: annotationTypes});

      var collectionEvent = this.CollectionEvent.create(jsonCevent, cet);
      collectionEvent.compareToJsonEntity(jsonCevent);
    });

    it('can retrieve a single collection event', function() {
      var self = this,
          collectionEvent = self.factory.collectionEvent();

      self.$httpBackend.whenGET(uri(collectionEvent.id))
        .respond(this.reply(collectionEvent));

      self.CollectionEvent.get(collectionEvent.id).then(function (reply) {
        expect(reply).toEqual(jasmine.any(self.CollectionEvent));
        reply.compareToJsonEntity(collectionEvent);
      });
      self.$httpBackend.flush();
    });

    it('get fails for and invalid collection event id', function() {
      var self = this,
          collectionEventId = self.factory.stringNext();

      self.$httpBackend.whenGET(uri(collectionEventId))
        .respond(404, { status: 'error', message: 'invalid id' });

      self.CollectionEvent.get(collectionEventId)
        .then(function (reply) { fail('should not be called'); })
        .catch(function (err) { expect(err.data.message).toContain('invalid id'); });
      self.$httpBackend.flush();
    });

    it('can list collection events for a participant', function() {
      var self = this,
          participant = self.factory.defaultParticipant(),
          collectionEvents = _.map(_.range(2), function () {
            return self.factory.collectionEvent();
          }),
          reply = self.factory.pagedResult(collectionEvents),
          serverEntity;

      self.$httpBackend.whenGET(uriWithPath('list', participant.id))
        .respond(this.reply(reply));

      self.CollectionEvent.list(participant.id).then(function (pagedResult) {
        expect(pagedResult.items).toBeArrayOfSize(collectionEvents.length);

        _.each(pagedResult.items, function(obj) {
          expect(obj).toEqual(jasmine.any(self.CollectionEvent));
          serverEntity = _.find(collectionEvents, { id: obj.id });
          expect(serverEntity).toBeDefined();
          obj.compareToJsonEntity(serverEntity);
        });
      });
      self.$httpBackend.flush();
    });

    it('can list collection events sorted by corresponding fields', function() {
      var self = this,
          study = self.factory.study(),
          participant = self.factory.participant({ studyId: study.id }),
          reply = self.factory.pagedResult([]),
          sortFields = [ 'visitNumber', 'timeCompleted'];

      _.each(sortFields, function (sortField) {
        self.$httpBackend.whenGET(uriWithPath('list', participant.id) + '?sort=' + sortField)
          .respond(self.reply(reply));

        self.CollectionEvent.list(participant.id, { sort: sortField }).then(function (pagedResult) {
          expect(pagedResult.items).toBeEmptyArray();
        });
        self.$httpBackend.flush();
      });
    });

    it('can list collection events using a page number', function() {
      var self = this,
          study = self.factory.study(),
          participant = self.factory.participant({ studyId: study.id }),
          reply = self.factory.pagedResult([]),
          pageNumber = 2;

      self.$httpBackend.whenGET(uriWithPath('list', participant.id) + '?page=' + pageNumber)
        .respond(this.reply(reply));

      self.CollectionEvent.list(participant.id, { page: pageNumber }).then(function (pagedResult) {
        expect(pagedResult.items).toBeEmptyArray();
      });
      self.$httpBackend.flush();
    });

    it('can list collection events using a page size', function() {
      var self = this,
          study = self.factory.study(),
          participant = self.factory.participant({ studyId: study.id }),
          reply = self.factory.pagedResult([]),
          limit = 2;

      self.$httpBackend.whenGET(uriWithPath('list', participant.id) + '?limit=' + limit)
        .respond(this.reply(reply));

      self.CollectionEvent.list(participant.id, { limit: limit }).then(function (pagedResult) {
        expect(pagedResult.items).toBeEmptyArray();
      });
      self.$httpBackend.flush();
    });

    it('can retrieve a single collection event by visit number', function() {
      var self            = this,
          entities        = self.getCollectionEventEntities(true),
          jsonParticipant = self.factory.defaultParticipant(),
          jsonCevent      = self.factory.defaultCollectionEvent();

      self.$httpBackend.whenGET(uri(jsonParticipant.id) + '/visitNumber/' + jsonCevent.visitNumber)
        .respond(this.reply(jsonCevent));

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
          participant = self.factory.participant(),
          reply = self.factory.pagedResult([]),
          sortExprs = [
            { sort: 'visitNumber' },
            { sort: '-visitNumber' }
          ];

      _.each(sortExprs, function (sortExpr) {
        var url = sprintf('%s?%s', uriWithPath('list', participant.id), self.$httpParamSerializer(sortExpr));
        self.$httpBackend.whenGET(url).respond(self.reply(reply));

        self.CollectionEvent.list(participant.id, sortExpr).then(function (pagedResult) {
          expect(pagedResult.items).toBeEmptyArray();
        });
        self.$httpBackend.flush();
      });
    });

    it('setting annotation types fails when it does not belong to collection event type',
        function() {
          var annotationData      = this.jsonAnnotationData(),
              annotationTypes     = _.map(annotationData, 'annotationType'),
              badAnnotationTypeId = 'bad-annotation-type-id',
              ceventType,
              collectionEvent;

          ceventType = this.CollectionEventType.create(
            this.factory.collectionEventType({
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
      var jsonCevent      = this.factory.collectionEvent(),
          collectionEvent = new this.CollectionEvent(_.omit(jsonCevent, 'id')),
          json            = addJson(collectionEvent);

      this.$httpBackend.expectPOST(uri(jsonCevent.participantId), json).respond(this.reply(jsonCevent));

      collectionEvent.add().then(function(reply) {
        _.extend(collectionEvent, { id: reply.id });
        collectionEvent.compareToJsonEntity(reply);
      });
      this.$httpBackend.flush();
    });

    it('can add a collection event with annotations', function() {
      var entities = this.getCollectionEventEntities(true),
          cmd      = addJson(entities.collectionEvent);

      this.$httpBackend.expectPOST(uri(entities.collectionEvent.participantId), cmd)
        .respond(this.reply(entities.serverCollectionEvent));

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
          jsonAnnotationTypes = self.factory.allAnnotationTypes();

      _.each(jsonAnnotationTypes, function (jsonAnnotType) {
        var jsonAnnotation, annotationType, ceventType, jsonCevent, collectionEvent;

        jsonAnnotation = self.factory.annotation({ value: null,
                                                        annotationTypeId: jsonAnnotType.uniqueId});

        annotationType = new self.AnnotationType(jsonAnnotType);
        ceventType = self.CollectionEventType.create(
          self.factory.collectionEventType({ annotationTypes: [ annotationType ] }));
        ceventType.annotationTypes[0].required = true;
        jsonCevent = _.omit(self.factory.collectionEvent({ annotations: [ jsonAnnotation ]}), 'id)');
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
                        this.factory.defaultCollectionEvent(),
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
                        this.factory.defaultCollectionEvent(),
                        this.expectCevent,
                        this.failTest);
    });

    it('should be able to remove a collection event', function() {
      var entities = this.getCollectionEventEntities(false),
          cevent = entities.collectionEvent,
          url = uri(cevent.participantId, cevent.id, cevent.version);

      this.$httpBackend.expectDELETE(url).respond(this.reply(true));

      cevent.remove();
      this.$httpBackend.flush();
    });

    var addJsonKeys = [
      'participantId',
      'collectionEventTypeId',
      'timeCompleted',
      'visitNumber'
    ];

    function addJson(collectionEvent) {
      return _.extend(_.pick(collectionEvent, addJsonKeys),
                      { annotations: annotationsForJson(collectionEvent) } );
    }

    function annotationsForJson(collectionEvent) {
      return _.map(collectionEvent.annotations, function (annotation) {
        return annotation.getServerAnnotation();
      });
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
