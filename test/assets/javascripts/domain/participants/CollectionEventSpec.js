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
                               testDomainEntities) {
      var self = this;

      _.extend(self,
               EntityTestSuiteMixin.prototype,
               ServerReplyMixin.prototype,
               AnnotationsEntityTestSuiteMixin.prototype);

      self.injectDependencies('$rootScope',
                              '$httpBackend',
                              '$httpParamSerializer',
                              'Participant',
                              'CollectionEventType',
                              'CollectionEvent',
                              'Annotation',
                              'AnnotationValueType',
                              'AnnotationType',
                              'annotationFactory',
                              'factory',
                              'testUtils');

      testUtils.addCustomMatchers();

      self.jsonStudy = self.factory.study();
      self.jsonCet = self.factory.collectionEventType();

      self.getCollectionEventEntities = getCollectionEventEntities;
      self.expectCevent = expectCevent;
      self.failTest = failTest;
      testDomainEntities.extend();

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
          collectionEvent:       collectionEvent,
          serverCollectionEvent: jsonCevent
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
      var self = this,
          jsonAnnotationEntities,
          jsonAnnotationTypes,
          jsonAnnotations,
          annotations,
          ceventType,
          collectionEvent;

      jsonAnnotationEntities = this.jsonAnnotationData().map(function (jsonEntities) {
        var annotationType = new self.AnnotationType(jsonEntities.annotationType),
            annotation = self.annotationFactory.create(jsonEntities.annotation, annotationType);
        return {
          jsonAnnotationType: jsonEntities.annotationType,
          jsonAnnotation:     jsonEntities.annotation,
          annotationType:     annotationType,
          annotation:         annotation
        };
      });

      jsonAnnotationTypes = _.map(jsonAnnotationEntities, 'jsonAnnotationType');
      jsonAnnotations     = _.map(jsonAnnotationEntities, 'jsonAnnotation');
      annotations         = _.map(jsonAnnotationEntities, 'annotation');

      this.jsonCet = this.factory.collectionEventType({ annotationTypes: jsonAnnotationTypes });
      ceventType = this.CollectionEventType.create(this.jsonCet);
      collectionEvent = new this.CollectionEvent({}, ceventType, annotations);


      ceventType.annotationTypes.forEach(function (annotationType) {
        var annotation = _.find(collectionEvent.annotations, { annotationTypeId: annotationType.id }),
            jsonAnnotation = _.find(jsonAnnotations, { annotationTypeId: annotationType.id});
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

      var collectionEvent = new self.CollectionEvent({ }, ceventType);

      expect(collectionEvent.annotations).toBeArrayOfSize(annotationData.length);
      _.each(ceventType.annotationTypes, function (annotationType) {
        var annotation = _.find(collectionEvent.annotations, { annotationTypeId: annotationType.id });
        self.validateAnnotationClass(annotationType, annotation);
        expect(annotation.required).toBe(annotationType.required);
      });
    });

    it('fails when constructing with invalid annotation parameter', function() {
      var self = this,
          jsonAnnotation = {},
          jsonCet,
          jsonCevent,
          ceventType;

      var annotationType = new this.AnnotationType(this.factory.annotationType());

      // put an invalid value in jsonAnnotation.annotationTypeId
      _.extend(
        jsonAnnotation,
        this.factory.annotation(this.factory.valueForAnnotation(annotationType), annotationType),
        { annotationTypeId: this.factory.stringNext() });

      jsonCet = this.factory.collectionEventType(this.jsonStudy,
                                                 { annotationTypes: [annotationType] });

      jsonCevent = this.factory.collectionEvent();
      ceventType = this.CollectionEventType.create(jsonCet);

      expect(function () {
        return self.CollectionEvent.create(
          _.extend(jsonCevent, {
            collectionEventType: ceventType,
            annotations: [ jsonAnnotation ]
          }));
      }).toThrowError(/annotation type not found/);
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

    describe('when creating', function() {

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
            jsonAnnotationType = this.factory.annotationType(),
            jsonCet =  this.factory.collectionEventType({ annotationTypes: [ jsonAnnotationType ] }),
            jsonCevent = this.factory.collectionEvent({
              collectionEventType: jsonCet,
              annotations: [ { annotationTypeId: jsonAnnotationType.id, tmp: 1 } ]
            });

        expect(function () {
          return new self.CollectionEvent.create(jsonCevent);
        }).toThrowError(/invalid annotation from server/);
      });

      it('has valid values when creating from a server response', function() {
        var annotationData  = this.jsonAnnotationData(),
            annotationTypes = _.map(annotationData, 'annotationType'),
            jsonCet         = this.factory.collectionEventType({ annotationTypes: annotationTypes }),
            jsonCevent      = this.factory.collectionEvent({ collectionEventType: jsonCet }),
            collectionEvent = this.CollectionEvent.create(jsonCevent);
        collectionEvent.compareToJsonEntity(jsonCevent);
      });

      it('fails when creating async from an object with invalid keys', function() {
        var serverObj = { tmp: 1 },
            catchTriggered = false;

        this.CollectionEvent.asyncCreate(serverObj)
          .catch(function (err) {
            expect(err.message).toContain('invalid object from server');
            catchTriggered = true;
          });
        this.$rootScope.$digest();
        expect(catchTriggered).toBeTrue();
      });

      it('fails when creating async from invalid annotations', function() {
        var cevent         = this.factory.collectionEvent(),
            catchTriggered = false;

        cevent.collectionEventType = undefined;
        cevent.annotations = [{ test: 1 }];
        this.CollectionEvent.asyncCreate(cevent)
          .catch(function (err) {
            expect(err.message).toContain('invalid object to create from');
            catchTriggered = true;
          });
        this.$rootScope.$digest();
        expect(catchTriggered).toBeTrue();
      });

    });

    describe('when getting a single collection event', function() {

      beforeEach(function() {
        this.collectionEvent = this.factory.collectionEvent();
      });

      it('can retrieve a single collection event', function() {
        var self = this;

        this.$httpBackend.whenGET(uri(this.collectionEvent.id))
          .respond(this.reply(this.collectionEvent));

        self.CollectionEvent.get(this.collectionEvent.id).then(function (reply) {
          expect(reply).toEqual(jasmine.any(self.CollectionEvent));
          reply.compareToJsonEntity(self.collectionEvent);
        });
        self.$httpBackend.flush();
      });

      it('get fails for and invalid collection event id', function() {
        var self = this;

        self.$httpBackend.whenGET(uri(this.collectionEvent.id))
          .respond(404, { status: 'error', message: 'invalid id' });

        self.CollectionEvent.get(this.collectionEvent.id)
          .then(function () { fail('should not be called'); })
          .catch(function (err) {
            expect(err.message).toContain('invalid id');
          });
        self.$httpBackend.flush();
      });

      it('throws a domain error if id is falsy', function() {
        var self = this;

        expect(function () {
          self.CollectionEvent.get();
        }).toThrowError(/collection event id not specified/);
      });

    });

    describe('when listing collection events', function() {

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

      it('returns rejected promise if collection events have invalid format', function() {
        var participant    = this.factory.participant(),
            reply          = this.factory.pagedResult([{ tmp: 1 }]),
            catchTriggered = false;

        this.$httpBackend.whenGET(uriWithPath('list', participant.id)).respond(this.reply(reply));

        this.CollectionEvent.list(participant.id)
          .catch(function (err) {
            expect(err.indexOf('invalid collection events from server')).not.toBeNull();
            catchTriggered = true;
          });
        this.$httpBackend.flush();
        expect(catchTriggered).toBeTrue();
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
          annotationTypes[0].id = badAnnotationTypeId;
          expect(function () {
            collectionEvent.setAnnotationTypes(annotationTypes);
          }).toThrowError(/annotation types not found/);
        });

    it('can add a collectionEvent', function() {
      var self            = this,
          jsonCevent      = this.factory.collectionEvent(),
          collectionEvent = this.CollectionEvent.create(jsonCevent),
          json            = addJson(collectionEvent);

      this.$httpBackend.expectPOST(uri(jsonCevent.participantId), json).respond(this.reply(jsonCevent));

      collectionEvent.add().then(function(reply) {
        expect(reply).toEqual(jasmine.any(self.CollectionEvent));
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
        expect(reply.participantId).toEqual(entities.serverCollectionEvent.participantId);
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

        jsonAnnotation = self.factory.annotation({ value: null, annotationTypeId: jsonAnnotType.id});

        annotationType = new self.AnnotationType(jsonAnnotType);
        ceventType = self.CollectionEventType.create(
          self.factory.collectionEventType({ annotationTypes: [ annotationType ] }));
        ceventType.annotationTypes[0].required = true;
        jsonCevent = _.omit(self.factory.collectionEvent({ annotations: [ jsonAnnotation ]}), 'id)');
        collectionEvent = new self.CollectionEvent(jsonCevent, ceventType);

        _.each(collectionEvent.annotations, function (annotation) {
          expect(annotation.getDisplayValue()).toBeFalsy();
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

    it('can add an annotation type', function() {
      var self = this,
          jsonCevent = this.factory.collectionEvent(),
          cevent = new this.CollectionEvent(jsonCevent),
          annotationType = new this.AnnotationType(this.factory.annotationType()),
          annotation = this.annotationFactory.create(undefined, annotationType),
          jsonAnnotation = _.extend(annotation.getServerAnnotation(), { expectedVersion: cevent.version }),
          thenTriggered = false;

      this.$httpBackend.expectPOST(uriWithPath('annot', cevent.id), jsonAnnotation)
        .respond(this.reply(jsonCevent));

      cevent.addAnnotation(annotation).then(function (reply) {
        expect(reply).toEqual(jasmine.any(self.CollectionEvent));
        thenTriggered = true;
      });
      this.$httpBackend.flush();
      expect(thenTriggered).toBeTrue();
    });

    describe('when removing annotations', function() {

      it('can remove an annotation type', function() {
        var self = this,
            annotationType = this.factory.annotationType(),
            annotation = this.factory.annotation(undefined, annotationType),
            jsonCet    = this.factory.collectionEventType({ annotationTypes: [ annotationType ]}),
            jsonCevent = this.factory.collectionEvent({
              collectionEvenType: jsonCet,
              annotations: [ annotation ]
            }),
            cevent = this.CollectionEvent.create(jsonCevent),
            url = sprintf('%s/%s/%d',
                          uriWithPath('annot', cevent.id),
                          annotation.annotationTypeId,
                          cevent.version),
            thenTriggered = false;

        this.$httpBackend.expectDELETE(url).respond(this.reply(true));

        cevent.removeAnnotation(annotation).then(function (reply) {
          expect(reply).toEqual(jasmine.any(self.CollectionEvent));
          thenTriggered = true;
        });
        this.$httpBackend.flush();
        expect(thenTriggered).toBeTrue();
      });

      it('fails when removing an annotation it does not contain', function() {
        var annotationType = this.factory.annotationType(),
            annotation = this.factory.annotation(undefined, annotationType),
            jsonCevent = this.factory.collectionEvent(),
            cevent = new this.CollectionEvent(jsonCevent),
            catchTriggered = false;

        cevent.removeAnnotation(annotation)
          .catch(function (err) {
            expect(err.indexOf('annotation with annotation type ID not present')).not.toBeNull();
            catchTriggered = true;
          });
        this.$rootScope.$digest();
        expect(catchTriggered).toBeTrue();
      });

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
