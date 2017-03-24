/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(function(require) {
  'use strict';

  var mocks                           = require('angularMocks'),
      _                               = require('lodash'),
      sprintf                         = require('sprintf-js'),
      entityWithAnnotationsSharedSpec = require('../../test/entityWithAnnotationsSharedSpec');

  describe('Participant', function() {

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function(EntityTestSuiteMixin,
                               ServerReplyMixin,
                               AnnotationsEntityTestSuiteMixin,
                               testDomainEntities) {
      var self = this;

      _.extend(this,
               EntityTestSuiteMixin.prototype,
               ServerReplyMixin.prototype,
               AnnotationsEntityTestSuiteMixin.prototype);

      self.injectDependencies('$q',
                              '$rootScope',
                              '$httpBackend',
                              'Participant',
                              'Study',
                              'Annotation',
                              'annotationFactory',
                              'DateTimeAnnotation',
                              'MultipleSelectAnnotation',
                              'NumberAnnotation',
                              'SingleSelectAnnotation',
                              'TextAnnotation',
                              'AnnotationValueType',
                              'AnnotationType',
                              'AppConfig',
                              'factory',
                              'testUtils');

      self.testUtils.addCustomMatchers();

      self.getParticipantEntities = getParticipantEntities;
      self.generateJsonAnnotationTypesAndAnnotations = generateJsonAnnotationTypesAndAnnotations;
      self.expectParticipant = expectParticipant;
      self.failTest = failTest;
      testDomainEntities.extend();

      //---

      function getParticipantEntities(isNew) {
        var jsonAnnotationTypes = self.factory.allAnnotationTypes(),
            jsonStudy           = self.factory.study({ annotationTypes: jsonAnnotationTypes }),
            study               = new self.Study(jsonStudy),
            jsonParticipant     = self.factory.participant(),
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
        var annotationTypes = self.factory.allAnnotationTypes(),
            study           = self.factory.study({ annotationTypes: annotationTypes }),
            annotations     = _.map(annotationTypes, function (annotationType) {
              var value = self.factory.valueForAnnotation(annotationType);
              return self.factory.annotation({ value: value }, annotationType);
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
      this.$httpBackend.verifyNoOutstandingExpectation();
      this.$httpBackend.verifyNoOutstandingRequest();
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
      var self               = this,
          jsonAnnotationData = self.generateJsonAnnotationTypesAndAnnotations(),
          study              = self.Study.create(jsonAnnotationData.study),
          annotations,
          participant;

      annotations = study.annotationTypes.map(function (annotationType) {
        var annotation = _.find(jsonAnnotationData.annotations, { annotationTypeId: annotationType.uniqueId });
        return self.annotationFactory.create(annotation, annotationType);
      });

      participant = new self.Participant({}, study, annotations);

      expect(participant.annotations).toBeArrayOfSize(study.annotationTypes.length);
      _.each(participant.annotations, function (annotation) {
        var jsonAnnotation = _.find(jsonAnnotationData.annotations,
                                    { annotationTypeId: annotation.annotationTypeId }),
            annotationType = _.find(study.annotationTypes,
                                    { uniqueId: annotation.annotationTypeId });

        self.validateAnnotationClass(annotationType, annotation);
        annotation.compareToJsonEntity(jsonAnnotation);
        expect(annotation.required).toBe(annotationType.required);
      });
    });

    it('constructor with study parameter has valid values', function() {
      var study = new this.Study(this.factory.study());
      var participant = new this.Participant({}, study);

      expect(participant.study).toEqual(study);
      expect(participant.studyId).toBe(study.id);
    });

    it('constructor with NO annotation parameters has valid values', function() {
      var self = this,
          annotationData = this.generateJsonAnnotationTypesAndAnnotations(),
          study = this.Study.create(annotationData.study),
          participant = new this.Participant({}, study);

      expect(participant.annotations).toBeArrayOfSize(study.annotationTypes.length);
      _.each(participant.annotations, function (annotation) {
        var annotationType = _.find(study.annotationTypes,
                                         { uniqueId: annotation.annotationTypeId });
        self.validateAnnotationClass(annotationType, annotation);
        expect(annotation.required).toBe(annotationType.required);
      });
    });

    it('constructor with invalid annotation parameter throws error', function() {
      var self             = this,
          annotationType   = new this.AnnotationType(this.factory.annotationType()),
          jsonStudy        = this.factory.study({ annotationTypes: [ annotationType ]}),
          study            = this.Study.create(jsonStudy),
          serverAnnotation = self.factory.annotation({ value: self.factory.valueForAnnotation(annotationType) },
                                                     annotationType),
          annotation;

      // put an invalid value in serverAnnotation.annotationTypeId
      annotation = _.extend(this.annotationFactory.create(serverAnnotation, annotationType),
               { annotationTypeId: self.factory.stringNext() });
      expect(function () {
        return new self.Participant({}, study, [ annotation ]);
      }).toThrowError(/annotation types not found/);
    });

    describe('when creating', function() {

      it('fails when creating from a non object', function() {
        var self = this;
        expect(function () {
          self.Participant.create(1);
        }).toThrowError(/Invalid type/);
      });

      it('fails when creating from an object with invalid keys', function() {
        var self = this,
            serverObj = { tmp: 1 };
        expect(function () {
          self.Participant.create(serverObj);
        }).toThrowError(/Missing required property/);
      });

      it('fails when creating from an object and an annotation has invalid keys', function() {
        var self = this,
            study = this.factory.study(),
            jsonParticipant = this.factory.participant({ studyId: study.id });

        jsonParticipant.annotations = [{ tmp: 1 }];
        expect(function () {
          self.Participant.create(jsonParticipant);
        }).toThrowError(/bad annotation type/);
      });

      it('has valid values when creating from a server response', function() {
        var study = this.factory.study(),
            jsonParticipant = this.factory.participant(study);

        // TODO: add annotations to the server response
        var participant = this.Participant.create(jsonParticipant);
        participant.compareToJsonEntity(jsonParticipant);
      });

      it('fails when creating async from an object with invalid keys', function() {
        var serverObj = { tmp: 1 },
            catchTriggered = false;

        this.Participant.asyncCreate(serverObj)
          .catch(function (err) {
            expect(err.message).toContain('Missing required property');
            catchTriggered = true;
          });
        this.$rootScope.$digest();
        expect(catchTriggered).toBeTrue();
      });

    });

    it('can retrieve a single participant', function() {
      var self = this,
          study = self.factory.study(),
          participant = self.factory.participant({ studyId: study.id });

      self.$httpBackend.whenGET(uri(study.id, participant.id)).respond(this.reply(participant));

      self.Participant.get(study.id, participant.id).then(function (reply) {
        expect(reply).toEqual(jasmine.any(self.Participant));
        reply.compareToJsonEntity(participant);
      });
      self.$httpBackend.flush();
    });

    it('can retrieve a single participant by uniqueId', function() {
      var self = this,
          study = self.factory.study(),
          participant = self.factory.participant({ studyId: study.id });

      self.$httpBackend.whenGET(
        sprintf.sprintf('/participants/uniqueId/%s/%s', study.id, participant.uniqueId)
      ).respond(this.reply(participant));

      self.Participant.getByUniqueId(study.id, participant.uniqueId).then(function (reply) {
        expect(reply).toEqual(jasmine.any(self.Participant));
        reply.compareToJsonEntity(participant);
      });
      self.$httpBackend.flush();
    });

    it('can add a participant', function() {
      var self = this,
          study = self.factory.study(),
          jsonParticipant = self.factory.participant({ studyId: study.id }),
          participant = new self.Participant(_.omit(jsonParticipant, 'id')),
          reqJson = addJson(participant);

      self.$httpBackend.expectPOST(uri(study.id), reqJson).respond(this.reply(jsonParticipant));

      participant.add().then(function(reply) {
        expect(reply).toEqual(jasmine.any(self.Participant));
      });
      self.$httpBackend.flush();
    });

    it('can add a participant with annotations', function() {
      var entities = this.getParticipantEntities(true),
          reqJson = addJson(entities.participant);

      this.$httpBackend.expectPOST(uri(entities.jsonStudy.id), reqJson)
        .respond(this.reply(entities.jsonParticipant));

      entities.participant.add().then(function(replyParticipant) {
        expect(replyParticipant.id).toEqual(entities.jsonParticipant.id);
      });
      this.$httpBackend.flush();
    });

    it('can not add a participant with empty required annotations', function() {
      var self = this,
          biobankApi = this.$injector.get('biobankApi'),
          jsonAnnotationTypes = this.factory.allAnnotationTypes(),
          replyParticipant = this.factory.participant();

      spyOn(biobankApi, 'post').and.returnValue(this.$q.when(replyParticipant));

      _.each(jsonAnnotationTypes, function (serverAnnotationType) {
        var annotationType  = new self.AnnotationType(serverAnnotationType),
            jsonStudy       = self.factory.study({ annotationTypes: [ annotationType ]}),
            study           = self.Study.create(jsonStudy),
            jsonParticipant = self.factory.participant(),
            participant;

        jsonParticipant.annotations[0] = self.factory.annotation({value: undefined}, annotationType);

        participant = new self.Participant(_.omit(jsonParticipant, 'id'), study);

        _.each(participant.annotations, function (annotation) {
          annotation.required = true;
          expect(annotation.getValue()).toBeFalsy();
        });

        participant.add().then(failTest).catch(checkErrorMsg);
      });

      function failTest() {
        fail('should not be called');
      }

      function checkErrorMsg(error) {
        expect(error).toContain('required annotation has no value');
      }
    });

    it('can update the unique ID on a participant', function() {
      var self = this,
          study = self.factory.study(),
          jsonParticipant = self.factory.participant({ studyId: study.id }),
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

    describe('updates to annotations', function () {

      var context = {};

      beforeEach(inject(function () {
        var jsonAnnotationType = this.factory.annotationType(),
            jsonStudy = this.factory.study({ annotationTypes: [ jsonAnnotationType ]}),
            jsonParticipant = this.factory.participant({
              studyId: jsonStudy.id,
              annotationTypes: [ jsonAnnotationType ]
            }),
            study = this.Study.create(jsonStudy),
            participant = new this.Participant(jsonParticipant, study);

        context.entityType     = this.Participant;
        context.entity         = participant;
        context.updateFuncName = 'addAnnotation';
        context.removeFuncName = 'removeAnnotation';
        context.annotation     = participant.annotations[0];
        context.$httpBackend   = this.$httpBackend;
        context.addUrl         = updateUri('annot', participant.id);
        context.removeUrl      = sprintf.sprintf('%s/%d/%s',
                                                 uri('annot', participant.id),
                                                 participant.version,
                                                 participant.annotations[0].annotationTypeId);
        context.response       = jsonParticipant;
      }));

      entityWithAnnotationsSharedSpec(context);

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
