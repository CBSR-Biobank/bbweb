/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
/* global angular */

import _ from 'lodash';
import annotationsSharedSpec from '../../../test/entityWithAnnotationsSharedSpec';

describe('Participant', function() {

  beforeEach(() => {
    angular.mock.module('biobankApp', 'biobank.test');
    angular.mock.inject(function(AnnotationsEntityTestSuiteMixin,
                                 ServerReplyMixin) {
      _.extend(this,
               AnnotationsEntityTestSuiteMixin,
               ServerReplyMixin);

      this.injectDependencies('$q',
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
                              'Factory',
                              'TestUtils');

      this.TestUtils.addCustomMatchers();

      this.getParticipantEntities = (isNew) => {
        var jsonAnnotationTypes = this.Factory.allAnnotationTypes(),
            jsonStudy           = this.Factory.study({ annotationTypes: jsonAnnotationTypes }),
            study               = new this.Study(jsonStudy),
            jsonParticipant     = this.Factory.participant(),
            annotationTypes,
            participant;

        if (isNew) {
          participant = new this.Participant(_.omit(jsonParticipant, 'id'), study);
        } else {
          participant = new this.Participant(jsonParticipant, study);
        }

        return {
          jsonStudy:           study,
          jsonAnnotationTypes: jsonAnnotationTypes,
          jsonParticipant:     jsonParticipant,
          annotationTypes:     annotationTypes,
          participant:         participant
        };
      };

      this.generateJsonAnnotationTypesAndAnnotations = () => {
        var annotationTypes = this.Factory.allAnnotationTypes(),
            study           = this.Factory.study({ annotationTypes: annotationTypes }),
            annotations     = annotationTypes.map((annotationType) => {
              var value = this.Factory.valueForAnnotation(annotationType);
              return this.Factory.annotation({ value: value }, annotationType);
            });
        return {
          study: study,
          annotations: annotations
        };
      };

      // used by promise tests
      this.expectParticipant = (entity) => {
        expect(entity).toEqual(jasmine.any(this.Participant));
      };

      // used by promise tests
      this.failTest = (error) => {
        expect(error).toBeUndefined();
      };

    });
  });

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
      var annotation = _.find(jsonAnnotationData.annotations, { annotationTypeId: annotationType.id });
      return self.annotationFactory.create(annotation, annotationType);
    });

    participant = new self.Participant({}, study, annotations);

    expect(participant.annotations).toBeArrayOfSize(study.annotationTypes.length);
    participant.annotations.forEach((annotation) => {
      var jsonAnnotation = _.find(jsonAnnotationData.annotations,
                                  { annotationTypeId: annotation.annotationTypeId }),
          annotationType = _.find(study.annotationTypes, { id: annotation.annotationTypeId });

      self.validateAnnotationClass(annotationType, annotation);
      expect(annotation.required).toBe(annotationType.required);
    });
  });

  it('constructor with study parameter has valid values', function() {
    var study = new this.Study(this.Factory.study());
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
    participant.annotations.forEach((annotation) => {
      var annotationType = _.find(study.annotationTypes, { id: annotation.annotationTypeId });
      self.validateAnnotationClass(annotationType, annotation);
      expect(annotation.required).toBe(annotationType.required);
    });
  });

  it('constructor with invalid annotation parameter throws error', function() {
    var self             = this,
        annotationType   = new this.AnnotationType(this.Factory.annotationType()),
        jsonStudy        = this.Factory.study({ annotationTypes: [ annotationType ]}),
        study            = this.Study.create(jsonStudy),
        serverAnnotation = self.Factory.annotation({ value: self.Factory.valueForAnnotation(annotationType) },
                                                   annotationType),
        annotation;

    // put an invalid value in serverAnnotation.annotationTypeId
    annotation = _.extend(this.annotationFactory.create(serverAnnotation, annotationType),
                          { annotationTypeId: self.Factory.stringNext() });
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
          study = this.Factory.study(),
          jsonParticipant = this.Factory.participant({ studyId: study.id });

      jsonParticipant.annotations = [{ tmp: 1 }];
      expect(function () {
        self.Participant.create(jsonParticipant);
      }).toThrowError(/bad annotation type/);
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
    var study = this.Factory.study(),
        participant = this.Factory.participant({ studyId: study.id });

    this.$httpBackend.whenGET(this.url('participants', study.id, participant.id))
      .respond(this.reply(participant));

    this.Participant.get(study.id, participant.id).then((reply) => {
      expect(reply).toEqual(jasmine.any(this.Participant));
    });
    this.$httpBackend.flush();
  });

  it('can retrieve a single participant by uniqueId', function() {
    var study = this.Factory.study(),
        participant = this.Factory.participant({ studyId: study.id });

    this.$httpBackend.whenGET(this.url('participants/uniqueId', study.id, participant.uniqueId))
      .respond(this.reply(participant));

    this.Participant.getByUniqueId(study.id, participant.uniqueId).then((reply) => {
      expect(reply).toEqual(jasmine.any(this.Participant));
    });
    this.$httpBackend.flush();
  });

  it('can add a participant', function() {
    var study = this.Factory.study(),
        jsonParticipant = this.Factory.participant({ studyId: study.id }),
        participant = new this.Participant(_.omit(jsonParticipant, 'id')),
        reqJson = addJson(participant);

    this.$httpBackend.expectPOST(this.url('participants', study.id), reqJson)
      .respond(this.reply(jsonParticipant));

    participant.add().then((reply) => {
      expect(reply).toEqual(jasmine.any(this.Participant));
    });
    this.$httpBackend.flush();
  });

  it('can add a participant with annotations', function() {
    var entities = this.getParticipantEntities(true),
        reqJson = addJson(entities.participant);

    this.$httpBackend.expectPOST(this.url('participants', entities.jsonStudy.id), reqJson)
      .respond(this.reply(entities.jsonParticipant));

    entities.participant.add().then((replyParticipant) => {
      expect(replyParticipant.id).toEqual(entities.jsonParticipant.id);
    });
    this.$httpBackend.flush();
  });

  it('can not add a participant with empty required annotations', function() {
    var biobankApi = this.$injector.get('biobankApi'),
        jsonAnnotationTypes = this.Factory.allAnnotationTypes(),
        replyParticipant = this.Factory.participant();

    spyOn(biobankApi, 'post').and.returnValue(this.$q.when(replyParticipant));

    jsonAnnotationTypes.forEach((serverAnnotationType) => {
      var annotationType  = new this.AnnotationType(serverAnnotationType),
          jsonStudy       = this.Factory.study({ annotationTypes: [ annotationType ]}),
          study           = this.Study.create(jsonStudy),
          jsonParticipant = this.Factory.participant(),
          participant;

      jsonParticipant.annotations[0] = this.Factory.annotation({value: undefined}, annotationType);

      participant = new this.Participant(_.omit(jsonParticipant, 'id'), study);

      participant.annotations.forEach((annotation) => {
        annotation.required = true;
        expect(annotation.getDisplayValue()).toBeFalsy();
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
    var study = this.Factory.study(),
        jsonParticipant = this.Factory.participant({ studyId: study.id }),
        participant = new this.Participant(jsonParticipant);

    this.updateEntity(participant,
                      'updateUniqueId',
                      participant.uniqueId,
                      this.url('participants/uniqueId', participant.id),
                      { uniqueId: participant.uniqueId },
                      jsonParticipant,
                      this.expectParticipant,
                      this.failTest);
  });

  describe('updates to annotations', function () {

    var context = {};

    beforeEach(function () {
      var jsonAnnotationType = this.Factory.annotationType(),
          jsonStudy = this.Factory.study({ annotationTypes: [ jsonAnnotationType ]}),
          jsonParticipant = this.Factory.participant({
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
      context.addUrl         = this.url('participants/annot', participant.id);
      context.removeUrl      = this.url('participants/annot',
                                        participant.id,
                                        participant.version,
                                        participant.annotations[0].annotationTypeId);
      context.response       = jsonParticipant;
    });

    annotationsSharedSpec(context);

  });

  function annotationsForCommand(participant) {
    if (!participant.annotations) { return []; }
    return participant.annotations.map((annotation) => annotation.getServerAnnotation());
  }

  function addJson(participant) {
    return _.extend(_.pick(participant, 'studyId', 'uniqueId'),
                    { annotations: annotationsForCommand(participant) } );
  }

});
