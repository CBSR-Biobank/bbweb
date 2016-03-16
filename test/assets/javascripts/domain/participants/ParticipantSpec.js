/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(function(require) {
  'use strict';

  var mocks   = require('angularMocks'),
      _       = require('underscore'),
      sprintf = require('sprintf');

  fdescribe('Participant', function() {

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function(entityTestSuite,
                               hasAnnotationsEntityTestSuite,
                               extendedDomainEntities) {
      var self = this;

      _.extend(this, entityTestSuite, hasAnnotationsEntityTestSuite);

      self.httpBackend              = self.$injector.get('$httpBackend');
      self.Participant              = self.$injector.get('Participant');
      self.Study                    = self.$injector.get('Study');
      self.Annotation               = self.$injector.get('Annotation');

      self.DateTimeAnnotation       = self.$injector.get('DateTimeAnnotation');
      self.MultipleSelectAnnotation = self.$injector.get('MultipleSelectAnnotation');
      self.NumberAnnotation         = self.$injector.get('NumberAnnotation');
      self.SingleSelectAnnotation   = self.$injector.get('SingleSelectAnnotation');
      self.TextAnnotation           = self.$injector.get('TextAnnotation');

      self.AnnotationValueType      = self.$injector.get('AnnotationValueType');
      self.AnnotationType           = self.$injector.get('AnnotationType');
      self.bbwebConfig              = self.$injector.get('bbwebConfig');
      self.jsonEntities             = self.$injector.get('jsonEntities');
      self.testUtils                = self.$injector.get('testUtils');

      self.testUtils.addCustomMatchers();

      self.getParticipantEntities = getParticipantEntities;
      self.generateJsonAnnotationTypesAndAnnotations = generateJsonAnnotationTypesAndAnnotations;
      self.expectParticipant = expectParticipant;
      self.failTest = failTest;

      function getParticipantEntities(isNew) {
        var jsonAnnotationTypes = self.jsonEntities.allAnnotationTypes(),
            jsonStudy           = self.jsonEntities.study({ annotationTypes: jsonAnnotationTypes }),
            study               = new self.Study(jsonStudy),
            jsonParticipant     = self.jsonEntities.participant({ studyId: study.id }),
            annotationTypes,
            participant;

        if (isNew) {
          participant = new self.Participant(_.omit(jsonParticipant, 'id'), study);
        } else {
          participant = new self.Participant(jsonParticipant, study);
        }

        return {
          jsonStudy:           study,
          jsonAnnotationTypes: jsonAnnotationTypes,
          jsonParticipant:     jsonParticipant,
          annotationTypes:     annotationTypes,
          participant:         participant
        };
      }

      function generateJsonAnnotationTypesAndAnnotations() {
        var annotationTypes = self.jsonEntities.allAnnotationTypes(),
            study = self.jsonEntities.study({ annotationTypes: annotationTypes }),
            annotations = _.map(annotationTypes, function (annotationType) {
              var value = self.jsonEntities.valueForAnnotation(annotationType);
              return self.jsonEntities.annotation({ value: value }, annotationType);
            });
        return {
          study: study,
          annotations: annotations
        };
      }

      // used by promise tests
      function expectParticipant(entity) {
        expect(entity).toEqual(jasmine.any(self.Participant));
      }

      // used by promise tests
      function failTest(error) {
        expect(error).toBeUndefined();
      }

    }));

    afterEach(function() {
      this.httpBackend.verifyNoOutstandingExpectation();
      this.httpBackend.verifyNoOutstandingRequest();
    });

    it('constructor with no parameters has default values', function() {
      var participant = new this.Participant();

      expect(participant.id).toBeNull();
      expect(participant.version).toBe(0);
      expect(participant.timeAdded).toBeNull();
      expect(participant.timeModified).toBeNull();
      expect(participant.uniqueId).toBeEmptyString();
    });

    it('constructor with annotation parameter has valid values', function() {
      var self = this,
          annotationData = this.generateJsonAnnotationTypesAndAnnotations(),
          study = new this.Study(annotationData.study),
          participant;

      participant = new this.Participant({ annotations: annotationData.annotations }, study);

      expect(participant.annotations).toBeArrayOfSize(study.annotationTypes.length);
      _.each(participant.annotations, function (annotation) {
        var jsonAnnotation = _.findWhere(annotationData.annotations,
                                         { annotationTypeId: annotation.annotationTypeId }),
            annotationType = _.findWhere(study.annotationTypes,
                                         { uniqueId: annotation.annotationTypeId });

        self.validateAnnotationClass(annotationType, annotation);
        annotation.compareToJsonEntity(jsonAnnotation);
        expect(annotation.required).toBe(annotationType.required);
      });
    });

    it('constructor with study parameter has valid values', function() {
      var study = new this.Study(this.jsonEntities.study());
      var participant = new this.Participant({}, study);

      expect(participant.study).toEqual(study);
      expect(participant.studyId).toBe(study.id);
    });

    it('constructor with NO annotation parameters has valid values', function() {
      var self = this,
          annotationData = this.generateJsonAnnotationTypesAndAnnotations(),
          study = new this.Study(annotationData.study),
          participant = new this.Participant({}, study);

      expect(participant.annotations).toBeArrayOfSize(study.annotationTypes.length);
      _.each(participant.annotations, function (annotation) {
        var annotationType = _.findWhere(study.annotationTypes,
                                         { uniqueId: annotation.annotationTypeId });
        self.validateAnnotationClass(annotationType, annotation);
        expect(annotation.required).toBe(annotationType.required);
      });
    });

    it('constructor with invalid annotation parameter throws error', function() {
      var self = this,
          annotationType = new this.AnnotationType(
            this.jsonEntities.annotationType({ valueType: this.AnnotationValueType.TEXT() })),
          jsonStudy = this.jsonEntities.study({ annotationTypes: [ annotationType ]}),
          study = new this.Study(jsonStudy),
          serverAnnotation = {};

      // put an invalid value in serverAnnotation.annotationTypeId
      _.extend(
        serverAnnotation,
        self.jsonEntities.annotation(
          { value: self.jsonEntities.valueForAnnotation(annotationType) },
          annotationType),
        { annotationTypeId: self.jsonEntities.stringNext() });

      expect(function () {
        return new self.Participant({ annotations: [ serverAnnotation ] },
                                    study);
      }).toThrow(new Error('annotation types not found: ' + serverAnnotation.annotationTypeId));
    });

    it('fails when creating from a non object', function() {
      var self = this;
      expect(function () { self.Participant.create(1); }).toThrowErrorOfType('Error');
    });

    it('fails when creating from an object with invalid keys', function() {
      var self = this,
          serverObj = { tmp: 1 };
      expect(function () { self.Participant.create(serverObj); }).toThrowErrorOfType('Error');
    });

    it('fails when creating from an object and an annotation has invalid keys', function() {
      var self = this,
          study = this.jsonEntities.study(),
          jsonParticipant = this.jsonEntities.participant({ studyId: study.id });

      jsonParticipant.annotations = [{ tmp: 1 }];
      expect(function () { self.Participant.create(jsonParticipant); })
        .toThrowErrorOfType('Error');
    });

    it('has valid values when creating from a server response', function() {
      var study = this.jsonEntities.study(),
          jsonParticipant = this.jsonEntities.participant(study);

      // TODO: add annotations to the server response
      var participant = this.Participant.create(jsonParticipant);
      participant.compareToJsonEntity(jsonParticipant);
    });

    it('can retrieve a single participant', function() {
      var self = this,
          study = self.jsonEntities.study(),
          participant = self.jsonEntities.participant({ studyId: study.id });

      self.httpBackend.whenGET(uri(study.id, participant.id)).respond(serverReply(participant));

      self.Participant.get(study.id, participant.id).then(function (reply) {
        expect(reply).toEqual(jasmine.any(self.Participant));
        reply.compareToJsonEntity(participant);
      });
      self.httpBackend.flush();
    });

    it('can retrieve a single participant by uniqueId', function() {
      var self = this,
          study = self.jsonEntities.study(),
          participant = self.jsonEntities.participant({ studyId: study.id });

      self.httpBackend.whenGET(
        sprintf.sprintf('/participants/uniqueId/%s/%s', study.id, participant.uniqueId)
      ).respond(serverReply(participant));

      self.Participant.getByUniqueId(study.id, participant.uniqueId).then(function (reply) {
        expect(reply).toEqual(jasmine.any(self.Participant));
        reply.compareToJsonEntity(participant);
      });
      self.httpBackend.flush();
    });

    it('can add a participant', function() {
      var self = this,
          study = self.jsonEntities.study(),
          jsonParticipant = self.jsonEntities.participant({ studyId: study.id }),
          participant = new self.Participant(_.omit(jsonParticipant, 'id')),
          reqJson = addJson(participant);

      self.httpBackend.expectPOST(uri(study.id), reqJson).respond(201, serverReply(jsonParticipant));

      participant.add().then(function(reply) {
        expect(reply).toEqual(jasmine.any(self.Participant));
        expect(reply.id).toEqual(jsonParticipant.id);
        expect(reply.version).toEqual(0);
        expect(reply.studyId).toEqual(study.id);
        expect(reply.uniqueId).toEqual(participant.uniqueId);
        expect(reply.annotations).toBeArrayOfSize(participant.annotations.length);
      });
      self.httpBackend.flush();
    });

    fit('can add a participant with annotations', function() {
      var entities = this.getParticipantEntities(true),
          reqJson = addJson(entities.participant);

      this.httpBackend.expectPOST(uri(entities.jsonStudy.id), reqJson)
        .respond(201, serverReply(entities.jsonParticipant));

      entities.participant.add().then(function(replyParticipant) {
        expect(replyParticipant.id).toEqual(entities.jsonParticipant.id);
        expect(replyParticipant.version).toEqual(0);
        expect(replyParticipant.studyId).toEqual(entities.jsonStudy.id);
        expect(replyParticipant.uniqueId).toEqual(entities.participant.uniqueId);
        expect(replyParticipant.annotations).toBeArrayOfSize(entities.participant.annotations.length);
      });
      this.httpBackend.flush();
    });

    it('can not add a participant with empty required annotations', function() {
      var self = this,
          jsonAnnotationTypes = self.jsonEntities.allAnnotationTypes();

      _.each(jsonAnnotationTypes, function (serverAnnotationType) {
        var annotationType = new self.AnnotationType(serverAnnotationType),
            jsonStudy = self.jsonEntities.study({ annotationTypes: [ annotationType ]}),
            study = new self.Study(jsonStudy),
            jsonParticipant = self.jsonEntities.participant(),
            participant = new self.Participant(_.omit(jsonParticipant, 'id'), study);

        _.each(participant.annotations, function (annotation) {
          annotation.required = true;
          expect(annotation.getValue()).toBeFalsy();
        });

        expect(function () { participant.add(); }).toThrowAnyError();
      });
    });

    it('can update the unique ID on a participant', function() {
      var self = this,
          study = self.jsonEntities.study(),
          jsonParticipant = self.jsonEntities.participant({ studyId: study.id }),
          participant = new self.Participant(jsonParticipant);

      self.updateEntity(participant,
                        'updateUniqueId',
                        participant.uniqueId,
                        updateUri('uniqueId', participant.id),
                        { uniqueId: participant.uniqueId },
                        jsonParticipant,
                        self.expectParticipant,
                        self.failTest);
    });

    it('can add an annotation to a participant', function() {
      var self = this,
          jsonAnnotationType = self.jsonEntities.annotationType(),
          jsonStudy = self.jsonEntities.study({ annotationTypes: [ jsonAnnotationType ]}),
          jsonParticipant = self.jsonEntities.participant({
            studyId: jsonStudy.id
          }),
          study = new self.Study(jsonStudy),
          participant = new self.Participant(jsonParticipant, study);

      self.updateEntity(participant,
                        'addAnnotation',
                        participant.annotations[0],
                        updateUri('annot', participant.id),
                        _.pick(participant.annotations[0],
                               'stringValue',
                               'numberValue',
                               'selectedValues'),
                        jsonParticipant,
                        self.expectParticipant,
                        self.failTest);
    });

    describe('updates to annotations', function () {

      var context = {};

      beforeEach(inject(function () {
        var jsonAnnotationType = this.jsonEntities.annotationType(),
            jsonStudy = this.jsonEntities.study({ annotationTypes: [ jsonAnnotationType ]}),
            jsonParticipant = this.jsonEntities.participant({
              studyId: jsonStudy.id,
              annotationTypes: [ jsonAnnotationType ]
            }),
            study = new this.Study(jsonStudy),
            participant = new this.Participant(jsonParticipant, study);

        context.entityType     = this.Participant;
        context.entity         = participant;
        context.updateFuncName = 'addAnnotation';
        context.removeFuncName = 'removeAnnotation';
        context.annotation     = participant.annotations[0];
        context.addUrl         = updateUri('annot', participant.id);
        context.deleteUrl      = sprintf.sprintf('%s/%d/%s',
                                                 uri('annot', participant.id),
                                                 participant.version,
                                                 participant.annotations[0].annotationTypeId);
        context.response       = jsonParticipant;
      }));

      this.annotationSetSharedSpec(context);

    });

    function annotationsForCommand(participant) {
      return _.map(participant.annotations, function (annotation) {
        return annotation.getServerAnnotation();
      });
    }

    function addJson(participant) {
      return _.extend(_.pick(participant, 'studyId', 'uniqueId'),
                      { annotations: annotationsForCommand(participant) } );
    }

    function serverReply(event) {
      return { status: 'success', data: event };
    }


    function uri(/* studyId, participantId */) {
      var studyId,
          participantId,
          result = '/participants',
          args = _.toArray(arguments);

      if (args.length > 0) {
        studyId = args.shift();
        result += '/' + studyId;
      }

      if (args.length > 0) {
        participantId = args.shift();
        result += '/' + participantId;
      }
      return result;
    }

    function updateUri(/* path, participantId */) {
      var path,
          participantId,
          result = uri(),
          args = _.toArray(arguments);

      if (args.length > 0) {
        path = args.shift();
        result += '/' + path;
      }

      if (args.length > 0) {
        participantId = args.shift();
        result += '/' + participantId;
      }

      return result;
    }

  });

});
