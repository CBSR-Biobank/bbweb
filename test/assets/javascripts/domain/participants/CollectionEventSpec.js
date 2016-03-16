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
  'moment'
], function(angular, mocks, _, faker, moment) {
  'use strict';

  // FIXME: fix ignored tests

  xdescribe('CollectionEvent', function() {

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function(entityTestSuite,
                               hasAnnotationsEntityTestSuite,
                               testUtils,
                               extendedDomainEntities) {
      _.extend(this, entityTestSuite, hasAnnotationsEntityTestSuite);

      this.httpBackend                   = this.$injector.get('$httpBackend');
      this.Participant                   = this.$injector.get('Participant');
      this.CollectionEventType           = this.$injector.get('CollectionEventType');
      this.CollectionEvent               = this.$injector.get('CollectionEvent');
      this.Annotation                    = this.$injector.get('Annotation');
      this.AnnotationValueType           = this.$injector.get('AnnotationValueType');
      this.AnnotationType                = this.$injector.get('AnnotationType');
      this.jsonEntities                  = this.$injector.get('jsonEntities');
      this.testUtils                     = this.$injector.get('testUtils');

      testUtils.addCustomMatchers();

      this.jsonStudy = this.jsonEntities.study();
      this.jsonCet = this.jsonEntities.collectionEventType(this.jsonStudy);
    }));

    afterEach(function() {
      this.httpBackend.verifyNoOutstandingExpectation();
      this.httpBackend.verifyNoOutstandingRequest();
    });

    it('constructor with no parameters has default values', function() {
      var collectionEvent = new this.CollectionEvent();

      expect(collectionEvent.id).toBeNull();
      expect(collectionEvent.version).toBe(0);
      expect(collectionEvent.timeAdded).toBeNull();
      expect(collectionEvent.timeModified).toBeNull();
      expect(collectionEvent.timeCompleted).toBeNull();
      expect(collectionEvent.visitNumber).toBeNull();
    });

    it('constructor with annotation parameter has valid values', function() {
      var self              = this,
          annotationData    = self.jsonAnnotationData(),
          jsonAnnotations   = _.pluck(annotationData, 'annotation'),
          jsonAnnotationTypes   = _.pluck(annotationData, 'annotationType'),
          ceventType;

      self.jsonCet = self.jsonEntities.collectionEventType(
        self.jsonStudy, { annotationTypes: jsonAnnotationTypes });

      ceventType = self.CollectionEventType.create(self.jsonCet);

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

      self.jsonCet = self.jsonEntities.collectionEventType(
        self.jsonStudy, { annotationTypes: jsonAnnotationTypes });

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

      serverCollectionEvent =
        _.extend(self.jsonEntities.collectionEvent(),
                 {
                   collectionEventTypeId: self.jsonEntities.domainEntityNameNext(
                     self.jsonEntities.ENTITY_NAME_COLLECTION_EVENT_TYPE())
                 });
      ceventType = self.CollectionEventType.create(
        self.jsonEntities.collectionEventType(self.jsonStudy));

      expect(function () {
        return new self.CollectionEvent(serverCollectionEvent, ceventType);
      }).toThrowError('invalid collection event type');
    });

    it('fails when constructing a collection event with annotations and no collection event type',
       function() {
         var self = this,
             jsonAnnotationTypes = this.jsonEntities.allAnnotationTypes();

         _.each(jsonAnnotationTypes, function (jsonAnnotationType) {
           var jsonCevent = _.omit(self.jsonEntities.collectionEvent(), 'id)');

           expect(function () {
             return new self.CollectionEvent(jsonCevent, undefined);
           }).toThrowError('collection event type not defined');
         });
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
          serverCollectionEvent = this.jsonEntities.collectionEvent({annotationTypes: annotationTypes});

      var collectionEvent = this.CollectionEvent.create(serverCollectionEvent);
      collectionEvent.compareToJsonEntity(serverCollectionEvent);
    });

    it('can retrieve a single collection event', function(done) {
      var participant = this.jsonEntities.participant(),
          collectionEvent = this.jsonEntities.collectionEvent({
            participantId: participant.id
          });

      this.httpBackend.whenGET(uri(participant.id) + '?ceventId=' + collectionEvent.id)
        .respond(serverReply(collectionEvent));

      this.CollectionEvent.get(participant.id, collectionEvent.id).then(function (reply) {
        expect(reply).toEqual(jasmine.any(this.CollectionEvent));
        reply.compareToJsonEntity(collectionEvent);
        done();
      });
      this.httpBackend.flush();
    });

    it('get fails when collection event ID not specified', function() {
      var participant = this.jsonEntities.participant();

      expect(function () {
        return this.CollectionEvent.get(participant.id);
      }).toThrow(new Error('collection event id not specified'));
    });

    it('can list collection events for a participant', function(done) {
      var study = this.jsonEntities.study(),
          participant = this.jsonEntities.participant({ studyId: study.id }),
          ceventType = this.jsonEntities.collectionEventType(study),
          collectionEvents = _.map(_.range(2), function () {
            return this.jsonEntities.collectionEvent({
              participantId: participant.id,
              collectionEventTypeId: ceventType.id
            });
          }),
          reply = this.jsonEntities.pagedResult(collectionEvents),
          serverEntity;

      this.httpBackend.whenGET(uri(participant.id) + '/list')
        .respond(serverReply(reply));

      this.CollectionEvent.list(participant.id).then(function (pagedResult) {
        expect(pagedResult.items).toBeArrayOfSize(collectionEvents.length);

        _.each(pagedResult.items, function(obj) {
          expect(obj).toEqual(jasmine.any(this.CollectionEvent));
          serverEntity = _.findWhere(collectionEvents, { id: obj.id });
          expect(serverEntity).toBeDefined();
          obj.compareToJsonEntity(serverEntity);
        });
        done();
      });
      this.httpBackend.flush();
    });

    it('can list collection events sorted by corresponding fields',
       function(done) {
         var study = this.jsonEntities.study(),
             participant = this.jsonEntities.participant({ studyId: study.id }),
             reply = this.jsonEntities.pagedResult([]),
             sortFields = [ 'visitNumber', 'timeCompleted'];

         _.each(sortFields, function (sortField) {
           this.httpBackend.whenGET(uri(participant.id) + '/list?sort=' + sortField)
             .respond(serverReply(reply));

           this.CollectionEvent.list(participant.id, { sort: sortField }).then(function (pagedResult) {
             expect(pagedResult.items).toBeEmptyArray();
             done();
           });
           this.httpBackend.flush();
         });
       });

    it('can list collection events using a page number',
       function(done) {
         var study = this.jsonEntities.study(),
             participant = this.jsonEntities.participant({ studyId: study.id }),
             reply = this.jsonEntities.pagedResult([]),
             pageNumber = 2;

         this.httpBackend.whenGET(uri(participant.id) + '/list?page=' + pageNumber)
           .respond(serverReply(reply));

         this.CollectionEvent.list(participant.id, { page: pageNumber }).then(function (pagedResult) {
           expect(pagedResult.items).toBeEmptyArray();
           done();
         });
         this.httpBackend.flush();
       });

    it('can list collection events using a page size',
       function(done) {
         var study = this.jsonEntities.study(),
             participant = this.jsonEntities.participant({ studyId: study.id }),
             reply = this.jsonEntities.pagedResult([]),
             pageSize = 2;

         this.httpBackend.whenGET(uri(participant.id) + '/list?pageSize=' + pageSize)
           .respond(serverReply(reply));

         this.CollectionEvent.list(participant.id, { pageSize: pageSize }).then(function (pagedResult) {
           expect(pagedResult.items).toBeEmptyArray();
           done();
         });
         this.httpBackend.flush();
       });

    it('can retrieve a single collection event by visit number', function(done) {
      var entities              = getCollectionEventEntities(true),
          participant           = entities.participant,
          serverCollectionEvent = entities.serverCollectionEvent;

      this.httpBackend.whenGET(uri(participant.id) + '/visitNumber/' + serverCollectionEvent.visitNumber)
        .respond(serverReply(serverCollectionEvent));

      this.CollectionEvent.getByVisitNumber(participant.id,
                                       serverCollectionEvent.visitNumber,
                                       entities.collectionEventType,
                                       entities.annotationTypes)
        .then(function (reply) {
          expect(reply).toEqual(jasmine.any(this.CollectionEvent));
          reply.compareToJsonEntity(serverCollectionEvent);
          done();
        });
      this.httpBackend.flush();
    });

    it('can list collection events using ordering',
       function(done) {
         var study = this.jsonEntities.study(),
             participant = this.jsonEntities.participant({ studyId: study.id }),
             reply = this.jsonEntities.pagedResult([]),
             orderingTypes = [ 'asc', 'desc'];

         _.each(orderingTypes, function (orderingType) {
           this.httpBackend.whenGET(uri(participant.id) + '/list?order=' + orderingType)
             .respond(serverReply(reply));

           this.CollectionEvent.list(participant.id, { order: orderingType }).then(function (pagedResult) {
             expect(pagedResult.items).toBeEmptyArray();
             done();
           });
           this.httpBackend.flush();
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
            this.jsonEntities.collectionEventType(this.jsonStudy, {
              specimenGroups:  this.jsonStudy.specimenGroups,
              annotationTypes: annotationTypes
            }));
          collectionEvent = new this.CollectionEvent({}, ceventType);

          // replace id with a bad one
          annotationTypes[0].id = badAnnotationTypeId;
          expect(function () {
            collectionEvent.setAnnotationTypes(annotationTypes);
          }).toThrow(new Error(
            'annotation types not belonging to collection event type found: ' +
              badAnnotationTypeId));
        });

    it('can add a collectionEvent', function(done) {
      var participant = this.jsonEntities.participant(),
          baseCollectionEvent = this.jsonEntities.collectionEvent({
            participantId: participant.id
          }),
          collectionEvent     = new this.CollectionEvent(_.omit(baseCollectionEvent, 'id')),
          cmd                 = addCommand(collectionEvent);

      this.httpBackend.expectPOST(uri(participant.id), cmd).respond(201, serverReply(baseCollectionEvent));

      collectionEvent.addOrUpdate().then(function(replyCollectionEvent) {
        _.extend(collectionEvent, { id: replyCollectionEvent.id });
        collectionEvent.compareToJsonEntity(replyCollectionEvent);
        done();
      });
      this.httpBackend.flush();
    });

    it('can add a collection event with annotations', function(done) {
      var entities = getCollectionEventEntities(true),
          cmd      = addCommand(entities.collectionEvent);

      this.httpBackend.expectPOST(uri(entities.collectionEvent.participantId), cmd)
        .respond(201, serverReply(entities.serverCollectionEvent));

      entities.collectionEvent.addOrUpdate().then(function(replyCollectionEvent) {
        expect(replyCollectionEvent.id).toEqual(entities.serverCollectionEvent.id);
        expect(replyCollectionEvent.version).toEqual(0);
        expect(replyCollectionEvent.participantId).toEqual(entities.participant.id);
        expect(replyCollectionEvent.timeCompleted).toEqual(entities.serverCollectionEvent.timeCompleted);
        expect(replyCollectionEvent.visitNumber).toEqual(entities.serverCollectionEvent.visitNumber);
        expect(replyCollectionEvent.annotations)
          .toBeArrayOfSize(entities.serverCollectionEvent.annotations.length);
        done();
      });
      this.httpBackend.flush();
    });

    it('can not add a collection event with empty required annotations', function() {
      var jsonAnnotationTypes = this.jsonEntities.allStudyAnnotationTypes(this.jsonStudy);

      _.each(jsonAnnotationTypes, function (jsonAnnotationType) {
        var annotationType, ceventType, serverCevent, collectionEvent;

        annotationType = new this.AnnotationType(jsonAnnotationType);
        ceventType = this.CollectionEventType.create(this.jsonEntities.collectionEventType(
          this.jsonStudy, { annotationTypes: [ annotationType ] } ));
        ceventType.annotationTypeData[0].required = true;
        serverCevent = _.omit(this.jsonEntities.collectionEvent(), 'id)');
        collectionEvent = new this.CollectionEvent(serverCevent, ceventType, [ annotationType ]);

        _.each(collectionEvent.annotations, function (annotation) {
          expect(annotation.getValue()).toBeFalsy();
        });

        expect(function () {
          collectionEvent.addOrUpdate();
        }).toThrow();
      });
    });

    it('can update a collectionEvent', function(done) {
      var entities        = getCollectionEventEntities(false),
          collectionEvent = entities.collectionEvent,
          cmd             = updateCommand(collectionEvent),
          reply           = replyCollectionEvent(entities.serverCollectionEvent);

      this.httpBackend.expectPUT(uri(collectionEvent.participantId, collectionEvent.id), cmd)
        .respond(201, serverReply(reply));

      collectionEvent.addOrUpdate().then(function(replyCollectionEvent) {
        expect(replyCollectionEvent.id).toEqual(collectionEvent.id);
        expect(replyCollectionEvent.version).toEqual(collectionEvent.version + 1);
        expect(replyCollectionEvent.participantId).toEqual(collectionEvent.participantId);
        expect(replyCollectionEvent.timeCompleted).toEqual(collectionEvent.timeCompleted);
        expect(replyCollectionEvent.visitNumber).toEqual(collectionEvent.visitNumber);
        expect(replyCollectionEvent.annotations)
          .toBeArrayOfSize(collectionEvent.annotations.length);
        expect(replyCollectionEvent.annotations)
          .toContainAll(collectionEvent.annotations);
        done();
      });
      this.httpBackend.flush();
    });

    it('can not update a collectionEvent with empty required annotations', function() {
      var entities = getCollectionEventEntities(false);

      _.each(entities.jsonAnnotationTypes, function (jsonAnnotationType) {
        var annotationType,
            collectionEvent;

        entities.serverCollectionEvent.annotations =
          _.map(entities.serverCollectionEvent.annotations,
                function (annotation) {
                  return {
                    annotationTypeId: annotation.annotationTypeId,
                    selectedValues: []
                  };
                });

        annotationType = new this.AnnotationType(jsonAnnotationType);
        collectionEvent = new this.CollectionEvent(entities.serverCollectionEvent,
                                              entities.collectionEventType,
                                              [ annotationType ]);

        _.each(collectionEvent.annotations, function (annotation) {
          annotation.required = true;
          expect(annotation.getValue()).toBeFalsy();
        });

        expect(function () {
          collectionEvent.addOrUpdate();
        }).toThrow();
      });
    });

    it('should be able to remove a collection event', function() {
      var entities = getCollectionEventEntities(false),
          collectionEvent = entities.collectionEvent;

      this.httpBackend.expectDELETE(uri(collectionEvent.participantId,
                                   collectionEvent.id,
                                   collectionEvent.version))
        .respond(201, serverReply(true));

      collectionEvent.remove();
      this.httpBackend.flush();
    });

    function getCollectionEventEntities(isNew) {
      // var study,
      //     participant,
      //     collectionEventType,
      //     jsonAnnotationTypes,
      //     serverCollectionEvent,
      //     initServerCollectionEvent,
      //     annotationTypes,
      //     collectionEvent;

      // study = this.jsonEntities.study();
      // participant = this.jsonEntities.participant(study);
      // jsonAnnotationTypes = this.jsonEntities.allStudyAnnotationTypes(study);
      // annotationTypes = _.map(jsonAnnotationTypes, function (jsonAnnotationType) {
      //   return new AnnotationType(jsonAnnotationType);
      // });

      // collectionEventType = CollectionEventType.create(
      //   this.jsonEntities.collectionEventType(this.jsonStudy, {
      //     specimenGroups:  this.jsonStudy.specimenGroups,
      //     annotationTypes: annotationTypes
      //   }));

      // serverCollectionEvent = this.jsonEntities.collectionEvent({
      //   participantId:         participant.id,
      //   collectionEventTypeId: collectionEventType.id,
      //   annotationTypes:       jsonAnnotationTypes
      // });
      // initServerCollectionEvent = isNew ?
      //   _.omit(serverCollectionEvent, 'id'): serverCollectionEvent;

      // collectionEvent = new this.CollectionEvent(initServerCollectionEvent,
      //                                       collectionEventType,
      //                                       annotationTypes);

      // return {
      //   serverStudy:           study,
      //   participant:           participant,
      //   collectionEventType:   collectionEventType,
      //   jsonAnnotationTypes: jsonAnnotationTypes,
      //   serverCollectionEvent: serverCollectionEvent,
      //   annotationTypes:       annotationTypes,
      //   collectionEvent:       collectionEvent
      // };
    }

    function annotationsForCommand(collectionEvent) {
      return _.map(collectionEvent.annotations, function (annotation) {
        return annotation.getJsonAnnotation();
      });
    }

    var addCommandKeys = [
      'participantId',
      'collectionEventTypeId',
      'timeCompleted',
      'visitNumber'
    ];

    //var updateCommandKeys = addCommandKeys.concat('id');

    function addCommand(collectionEvent) {
      return _.extend(_.pick(collectionEvent, addCommandKeys),
                      { annotations: annotationsForCommand(collectionEvent) } );
    }

    function updateCommand(collectionEvent) {
      // return _.extend(_.pick(collectionEvent, updateCommandKeys),
      //                 { annotations: annotationsForCommand(collectionEvent) },
      //                 this.testUtils.expectedVersion(collectionEvent.version));
    }

    function replyCollectionEvent(collectionEvent, newValues) {
      // newValues = newValues || {};
      // return new this.CollectionEvent(_.extend({}, collectionEvent, newValues, {version: collectionEvent.version + 1}));
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

      if (args.length < 1) {
        throw new Error('participant id not specified');
      }

      participantId = args.shift();
      result += '/' + participantId;

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
  });

});
