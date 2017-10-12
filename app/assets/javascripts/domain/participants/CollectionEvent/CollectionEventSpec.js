/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
/* global angular */

import _ from 'lodash';
import annotationsSharedSpec from '../../../test/entityWithAnnotationsSharedSpec';

describe('CollectionEvent', function() {

  beforeEach(() => {
    angular.mock.module('biobankApp', 'biobank.test');
    angular.mock.inject(function(AnnotationsEntityTestSuiteMixin,
                                 ServerReplyMixin,
                                 TestUtils) {
      _.extend(this,
               AnnotationsEntityTestSuiteMixin,
               ServerReplyMixin);

      this.injectDependencies('$rootScope',
                              '$httpBackend',
                              '$httpParamSerializer',
                              'Participant',
                              'CollectionEventType',
                              'CollectionEvent',
                              'Annotation',
                              'AnnotationValueType',
                              'AnnotationType',
                              'annotationFactory',
                              'Factory',
                              'TestUtils');

      TestUtils.addCustomMatchers();

      this.jsonStudy = this.Factory.study();
      this.jsonCet = this.Factory.collectionEventType();

      this.getCollectionEventEntities = (isNew) => {
        var jsonAnnotationTypes = this.Factory.allAnnotationTypes(),
            collectionEventType,
            initServerCollectionEvent,
            jsonCevent,
            collectionEvent;

        collectionEventType = this.CollectionEventType.create(
          this.Factory.collectionEventType({ annotationTypes: jsonAnnotationTypes }));

        jsonCevent = this.Factory.collectionEvent();
        initServerCollectionEvent = isNew ? _.omit(jsonCevent, 'id'): jsonCevent;
        collectionEvent = new this.CollectionEvent(initServerCollectionEvent, collectionEventType);

        return {
          jsonAnnotationTypes:   jsonAnnotationTypes,
          collectionEventType:   collectionEventType,
          collectionEvent:       collectionEvent,
          serverCollectionEvent: jsonCevent
        };
      };

      // used by promise tests
      this.expectCevent = (entity) => {
        expect(entity).toEqual(jasmine.any(this.CollectionEvent));
      };

      // used by promise tests
      this.failTest = (error) => {
        expect(error).toBeUndefined();
      };

      this.url = url;

      //---

      function url() {
        const args = [ 'participants/cevents' ].concat(_.toArray(arguments));
        return AnnotationsEntityTestSuiteMixin.url.apply(null, args);
      }
    });
  });

  afterEach(function() {
    this.$httpBackend.verifyNoOutstandingExpectation();
    this.$httpBackend.verifyNoOutstandingRequest();
  });

  it('constructor with default parameters has default values', function() {
    var ceventType = new this.CollectionEventType(this.Factory.collectionEventType()),
        collectionEvent = new this.CollectionEvent({}, ceventType);

    expect(collectionEvent.id).toBeNull();
    expect(collectionEvent.version).toBe(0);
    expect(collectionEvent.timeAdded).toBeNull();
    expect(collectionEvent.timeModified).toBeNull();
    expect(collectionEvent.timeCompleted).toBeNull();
    expect(collectionEvent.visitNumber).toBeNull();
  });

  it('constructor with annotation parameter has valid values', function() {
    var jsonAnnotationEntities,
        jsonAnnotationTypes,
        jsonAnnotations,
        annotations,
        ceventType,
        collectionEvent;

    jsonAnnotationEntities = this.jsonAnnotationData().map((jsonEntities) => {
      var annotationType = new this.AnnotationType(jsonEntities.annotationType),
          annotation = this.annotationFactory.create(jsonEntities.annotation, annotationType);
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

    this.jsonCet = this.Factory.collectionEventType({ annotationTypes: jsonAnnotationTypes });
    ceventType = this.CollectionEventType.create(this.jsonCet);
    collectionEvent = new this.CollectionEvent({}, ceventType, annotations);

    ceventType.annotationTypes.forEach((annotationType) => {
      var annotation = _.find(collectionEvent.annotations, { annotationTypeId: annotationType.id }),
          jsonAnnotation = _.find(jsonAnnotations, { annotationTypeId: annotationType.id});
      this.validateAnnotationClass(annotationType, annotation);
      expect(annotation.required).toBe(annotationType.required);
    });
  });

  it('constructor with NO annotations has valid values', function() {
    var annotationData = this.jsonAnnotationData(),
        jsonAnnotationTypes = _.map(annotationData, 'annotationType'),
        ceventType;

    this.jsonCet = this.Factory.collectionEventType({ annotationTypes: jsonAnnotationTypes });
    ceventType = this.CollectionEventType.create(this.jsonCet);

    var collectionEvent = new this.CollectionEvent({ }, ceventType);

    expect(collectionEvent.annotations).toBeArrayOfSize(annotationData.length);
    ceventType.annotationTypes.forEach((annotationType) => {
      var annotation = _.find(collectionEvent.annotations, { annotationTypeId: annotationType.id });
      this.validateAnnotationClass(annotationType, annotation);
      expect(annotation.required).toBe(annotationType.required);
    });
  });

  it('fails when constructing with invalid annotation parameter', function() {
    var jsonAnnotation = {},
        jsonCet,
        jsonCevent;

    var annotationType = this.AnnotationType.create(this.Factory.annotationType());

    // put an invalid value in jsonAnnotation.annotationTypeId
    _.extend(
      jsonAnnotation,
      this.Factory.annotation(this.Factory.valueForAnnotation(annotationType), annotationType),
      { annotationTypeId: this.Factory.stringNext() });

    jsonCet = this.Factory.collectionEventType(this.jsonStudy,
                                               { annotationTypes: [annotationType] });

    jsonCevent = this.Factory.collectionEvent();

    expect(() =>
           this.CollectionEvent.create(
             _.extend(jsonCevent, {
               collectionEventType: jsonCet,
               annotations: [ jsonAnnotation ]
             }))
          ).toThrowError(/annotation type not found/);
  });

  it('fails when constructing with invalid collection event type', function() {
    var serverCollectionEvent,
        ceventType;

    serverCollectionEvent = this.Factory.collectionEvent({
      collectionEventTypeId: this.Factory.domainEntityNameNext(
        this.Factory.ENTITY_NAME_COLLECTION_EVENT_TYPE())
    });
    ceventType = this.CollectionEventType.create(
      this.Factory.collectionEventType(this.jsonStudy));

    expect(() => new this.CollectionEvent(serverCollectionEvent, ceventType))
     .toThrowError('invalid collection event type');
  });

  describe('when creating', function() {

    it('fails when creating from a non object', function() {
      expect(() => this.CollectionEvent.create(1))
        .toThrowError(/invalid object from server/);
    });

    it('fails when creating from an object with invalid keys', function() {
      var serverObj = { tmp: 1 };
      expect(() => new this.CollectionEvent.create(serverObj))
        .toThrowError(/invalid object from server/);
    });

    it('fails when creating from an object with annotation with invalid keys', function() {
      var jsonAnnotationType = this.Factory.annotationType(),
          jsonCet =  this.Factory.collectionEventType({ annotationTypes: [ jsonAnnotationType ] }),
          jsonCevent = this.Factory.collectionEvent({
            collectionEventType: jsonCet,
            annotations: [ { annotationTypeId: jsonAnnotationType.id, tmp: 1 } ]
          });

      expect(() => new this.CollectionEvent.create(jsonCevent))
        .toThrowError(/invalid annotation from server/);
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
      var cevent         = this.Factory.collectionEvent(),
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
      this.collectionEvent = this.Factory.collectionEvent();
    });

    it('can retrieve a single collection event', function() {
      this.$httpBackend.whenGET(this.url(this.collectionEvent.id))
        .respond(this.reply(this.collectionEvent));

      this.CollectionEvent.get(this.collectionEvent.id).then((reply) => {
        expect(reply).toEqual(jasmine.any(this.CollectionEvent));
      });
      this.$httpBackend.flush();
    });

    it('get fails for and invalid collection event id', function() {
      this.$httpBackend.whenGET(this.url(this.collectionEvent.id))
        .respond(404, { status: 'error', message: 'invalid id' });

      this.CollectionEvent.get(this.collectionEvent.id)
        .then(() => { fail('should not be called'); })
        .catch((err) => {
          expect(err.message).toContain('invalid id');
        });
      this.$httpBackend.flush();
    });

    it('throws a domain error if id is falsy', function() {
      expect(() => {
        this.CollectionEvent.get();
      }).toThrowError(/collection event id not specified/);
    });

  });

  describe('when listing collection events', function() {

    it('can list collection events for a participant', function() {
      var participant = this.Factory.defaultParticipant(),
          collectionEvents = _.range(2).map(() => this.Factory.collectionEvent()),
          reply = this.Factory.pagedResult(collectionEvents),
          serverEntity;

      this.$httpBackend.whenGET(this.url('list', participant.id))
        .respond(this.reply(reply));

      this.CollectionEvent.list(participant.id).then((pagedResult) => {
        expect(pagedResult.items).toBeArrayOfSize(collectionEvents.length);

        pagedResult.items.forEach((obj) => {
          expect(obj).toEqual(jasmine.any(this.CollectionEvent));
          serverEntity = _.find(collectionEvents, { id: obj.id });
          expect(serverEntity).toBeDefined();
        });
      });
      this.$httpBackend.flush();
    });

    it('can list collection events sorted by corresponding fields', function() {
      var study = this.Factory.study(),
          participant = this.Factory.participant({ studyId: study.id }),
          reply = this.Factory.pagedResult([]),
          sortFields = [ 'visitNumber', 'timeCompleted'];

      sortFields.forEach((sortField) => {
        this.$httpBackend.whenGET(this.url('list', participant.id) + '?sort=' + sortField)
          .respond(this.reply(reply));

        this.CollectionEvent.list(participant.id, { sort: sortField }).then((pagedResult) => {
          expect(pagedResult.items).toBeEmptyArray();
        });
        this.$httpBackend.flush();
      });
    });

    it('can list collection events using a page number', function() {
      var study = this.Factory.study(),
          participant = this.Factory.participant({ studyId: study.id }),
          reply = this.Factory.pagedResult([]),
          pageNumber = 2;

      this.$httpBackend.whenGET(this.url('list', participant.id) + '?page=' + pageNumber)
        .respond(this.reply(reply));

      this.CollectionEvent.list(participant.id, { page: pageNumber }).then((pagedResult) => {
        expect(pagedResult.items).toBeEmptyArray();
      });
      this.$httpBackend.flush();
    });

    it('can list collection events using a page size', function() {
      var study = this.Factory.study(),
          participant = this.Factory.participant({ studyId: study.id }),
          reply = this.Factory.pagedResult([]),
          limit = 2;

      this.$httpBackend.whenGET(this.url('list', participant.id) + '?limit=' + limit)
        .respond(this.reply(reply));

      this.CollectionEvent.list(participant.id, { limit: limit }).then((pagedResult) => {
        expect(pagedResult.items).toBeEmptyArray();
      });
      this.$httpBackend.flush();
    });

    it('can retrieve a single collection event by visit number', function() {
      var entities        = this.getCollectionEventEntities(true),
          jsonParticipant = this.Factory.defaultParticipant(),
          jsonCevent      = this.Factory.defaultCollectionEvent();

      this.$httpBackend.whenGET(this.url(jsonParticipant.id, 'visitNumber',  jsonCevent.visitNumber))
        .respond(this.reply(jsonCevent));

      this.CollectionEvent.getByVisitNumber(jsonParticipant.id,
                                            jsonCevent.visitNumber,
                                            entities.collectionEventType,
                                            entities.annotationTypes)
        .then((reply) => {
          expect(reply).toEqual(jasmine.any(this.CollectionEvent));
        });
      this.$httpBackend.flush();
    });

    it('can list collection events using ordering', function() {
      var participant = this.Factory.participant(),
          reply = this.Factory.pagedResult([]),
          sortExprs = [
            { sort: 'visitNumber' },
            { sort: '-visitNumber' }
          ];

      sortExprs.forEach((sortExpr) => {
        var url = this.url('list', participant.id) + '?' + this.$httpParamSerializer(sortExpr);
        this.$httpBackend.whenGET(url).respond(this.reply(reply));

        this.CollectionEvent.list(participant.id, sortExpr).then(function (pagedResult) {
          expect(pagedResult.items).toBeEmptyArray();
        });
        this.$httpBackend.flush();
      });
    });

    it('returns rejected promise if collection events have invalid format', function() {
      var participant    = this.Factory.participant(),
          reply          = this.Factory.pagedResult([{ tmp: 1 }]),
          catchTriggered = false;

      this.$httpBackend.whenGET(this.url('list', participant.id)).respond(this.reply(reply));

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
         this.Factory.collectionEventType({
           annotationTypes: annotationTypes
         }));
       collectionEvent = new this.CollectionEvent({}, ceventType);

       // replace id with a bad one
       annotationTypes[0].id = badAnnotationTypeId;
       expect(() => {
         collectionEvent.setAnnotationTypes(annotationTypes);
       }).toThrowError(/annotation types not found/);
     });

  it('can add a collectionEvent', function() {
    var jsonCevent      = this.Factory.collectionEvent(),
        collectionEvent = this.CollectionEvent.create(jsonCevent),
        json            = addJson(collectionEvent);

    this.$httpBackend.expectPOST(this.url(jsonCevent.participantId), json).respond(this.reply(jsonCevent));

    collectionEvent.add().then((reply) => {
      expect(reply).toEqual(jasmine.any(this.CollectionEvent));
    });
    this.$httpBackend.flush();
  });

  it('can add a collection event with annotations', function() {
    var entities = this.getCollectionEventEntities(true),
        cmd      = addJson(entities.collectionEvent);

    this.$httpBackend.expectPOST(this.url(entities.collectionEvent.participantId), cmd)
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
    var jsonAnnotationTypes = this.Factory.allAnnotationTypes();

    jsonAnnotationTypes.forEach((jsonAnnotType) => {
      var jsonAnnotation, annotationType, ceventType, jsonCevent, collectionEvent;

      jsonAnnotation = this.Factory.annotation({ value: null, annotationTypeId: jsonAnnotType.id});

      annotationType = new this.AnnotationType(jsonAnnotType);
      ceventType = this.CollectionEventType.create(
        this.Factory.collectionEventType({ annotationTypes: [ annotationType ] }));
      ceventType.annotationTypes[0].required = true;
      jsonCevent = _.omit(this.Factory.collectionEvent({ annotations: [ jsonAnnotation ]}), 'id)');
      collectionEvent = new this.CollectionEvent(jsonCevent, ceventType);

      collectionEvent.annotations.forEach((annotation) => {
        expect(annotation.getDisplayValue()).toBeFalsy();
      });

      expect(() => {
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
                      this.url('visitNumber', cevent.id),
                      { visitNumber: cevent.visitNumber },
                      this.Factory.defaultCollectionEvent(),
                      this.expectCevent,
                      this.failTest);
  });

  it('can update the time completed on a collectionEvent', function() {
    var entities = this.getCollectionEventEntities(false),
        cevent   = entities.collectionEvent;

    this.updateEntity(cevent,
                      'updateTimeCompleted',
                      cevent.timeCompleted,
                      this.url('timeCompleted', cevent.id),
                      { timeCompleted: cevent.timeCompleted },
                      this.Factory.defaultCollectionEvent(),
                      this.expectCevent,
                      this.failTest);
  });

  it('should be able to remove a collection event', function() {
    var entities = this.getCollectionEventEntities(false),
        cevent = entities.collectionEvent,
        url = this.url(cevent.participantId, cevent.id, cevent.version);

    this.$httpBackend.expectDELETE(url).respond(this.reply(true));

    cevent.remove();
    this.$httpBackend.flush();
  });

  describe('updates to annotations', function () {

    var context = {};

    beforeEach(function () {
      var annotationType = this.Factory.annotationType(),
          annotation = this.Factory.annotation(undefined, annotationType),
          jsonCet    = this.Factory.collectionEventType({ annotationTypes: [ annotationType ]}),
          jsonCevent = this.Factory.collectionEvent({
            collectionEvenType: jsonCet,
            annotations: [ annotation ]
          }),
          cevent = this.CollectionEvent.create(jsonCevent);

      context.entityType     = this.CollectionEvent;
      context.entity         = cevent;
      context.updateFuncName = 'addAnnotation';
      context.removeFuncName = 'removeAnnotation';
      context.annotation     = cevent.annotations[0];
      context.$httpBackend   = this.$httpBackend;
      context.addUrl         = this.url('annot', cevent.id);
      context.removeUrl      = this.url('annot', cevent.id, cevent.version, annotation.annotationTypeId);
      context.response       = jsonCevent;
    });

    annotationsSharedSpec(context);

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
    return collectionEvent.annotations.map((annotation) => annotation.getServerAnnotation());
  }
});
