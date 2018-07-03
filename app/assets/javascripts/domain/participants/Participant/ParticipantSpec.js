/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */
/* global angular */

import { AnnotationsEntityTestSuiteMixin } from 'test/mixins/AnnotationsEntityTestSuiteMixin';
import { ServerReplyMixin } from 'test/mixins/ServerReplyMixin';
import _ from 'lodash';
import annotationsSharedBehaviour from 'test/behaviours/entityWithAnnotationsSharedBehaviour';
import ngModule from '../../index'

describe('Participant', function() {

  beforeEach(() => {
    angular.mock.module(ngModule, 'biobank.test');
    angular.mock.inject(function() {
      Object.assign(this,
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
                              'Factory');

      this.addCustomMatchers();

      this.fixture = (isNew) => {
        const jsonAnnotationTypes = this.Factory.allAnnotationTypes();
        const jsonStudy           = this.Factory.study({ annotationTypes: jsonAnnotationTypes });
        const study               = this.Study.create(jsonStudy);
        const jsonParticipant     = this.Factory.participant();
        let participant;

        if (isNew) {
          participant = new this.Participant(_.omit(jsonParticipant, 'id'), study);
        } else {
          participant = new this.Participant(jsonParticipant, study);
        }

        return {
          jsonStudy,
          jsonAnnotationTypes,
          jsonParticipant,
          participant
        };
      };

      this.generatePlainAnnotationTypesAndAnnotations = () => {
        const annotationTypes = this.Factory.allAnnotationTypes();
        const study           = this.Factory.study({ annotationTypes: annotationTypes });
        const annotations     = annotationTypes.map((annotationType) => {
          const value = this.Factory.valueForAnnotation(annotationType);
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
    this.$httpBackend.verifyNoOutstandingExpectation(false);
    this.$httpBackend.verifyNoOutstandingRequest();
  });

  it('constructor with no parameters has default values', function() {
    const participant = new this.Participant();

    expect(participant.id).toBeNull();
    expect(participant.version).toBe(0);
    expect(participant.timeAdded).toBeUndefined();
    expect(participant.timeModified).toBeUndefined();
    expect(participant.uniqueId).toBeEmptyString();
  });

  it('constructor with annotation parameter has valid values', function() {
    const jsonAnnotationData = this.generatePlainAnnotationTypesAndAnnotations();
    const study              = this.Study.create(jsonAnnotationData.study);

    const annotations = study.annotationTypes.map(annotationType => {
      const annotation = _.find(jsonAnnotationData.annotations, { annotationTypeId: annotationType.id });
      return this.annotationFactory.create(annotation, annotationType);
    });

    const participant = new this.Participant({}, study, annotations);

    expect(participant.annotations).toBeArrayOfSize(study.annotationTypes.length);
    participant.annotations.forEach((annotation) => {
      const jsonAnnotation = _.find(jsonAnnotationData.annotations,
                                    { annotationTypeId: annotation.annotationTypeId });
      const annotationType = _.find(study.annotationTypes, { id: annotation.annotationTypeId });

      expect(jsonAnnotation).toBeDefined();
      this.validateAnnotationClass(annotationType, annotation);
      expect(annotation.required).toBe(annotationType.required);
    });
  });

  it('constructor with study parameter has valid values', function() {
    const study = new this.Study(this.Factory.study());
    const participant = new this.Participant({}, study);

    expect(participant.study).toEqual(study);
    expect(participant.studyId).toBe(study.id);
  });

  it('constructor with NO annotation parameters has valid values', function() {
    const annotationData = this.generatePlainAnnotationTypesAndAnnotations();
    const study = this.Study.create(annotationData.study);
    const participant = new this.Participant({}, study);

    expect(participant.annotations).toBeArrayOfSize(study.annotationTypes.length);
    participant.annotations.forEach(annotation => {
      const  annotationType = _.find(study.annotationTypes, { id: annotation.annotationTypeId });
      this.validateAnnotationClass(annotationType, annotation);
      expect(annotation.required).toBe(annotationType.required);
    });
  });

  it('constructor with invalid annotation parameter throws error', function() {
    const annotationType   = new this.AnnotationType(this.Factory.annotationType());
    const jsonStudy        = this.Factory.study({ annotationTypes: [ annotationType ]});
    const study            = this.Study.create(jsonStudy);
    const serverAnnotation = this.Factory.annotation(
      { value: this.Factory.valueForAnnotation(annotationType) },
      annotationType);

    // put an invalid value in serverAnnotation.annotationTypeId
    const annotation = Object.assign(this.annotationFactory.create(serverAnnotation, annotationType),
                                     { annotationTypeId: this.Factory.stringNext() });
    expect(
      () => new this.Participant({}, study, [ annotation ])
    ).toThrowError(/annotation types not found/);
  });

  describe('when creating', function() {

    it('fails when creating from a non object', function() {
      expect(
        () => this.Participant.create(1)
      ).toThrowError(/Invalid type/);
    });

    it('fails when creating from an object with invalid keys', function() {
      const serverObj = { tmp: 1 };
      expect(
        ()  => this.Participant.create(serverObj)
      ).toThrowError(/Missing required property/);
    });

    it('fails when creating from an object and an annotation has invalid keys', function() {
      const study = this.Factory.study();
      const jsonParticipant = this.Factory.participant({ studyId: study.id });

      jsonParticipant.annotations = [{ tmp: 1 }];
      expect(
        () => this.Participant.create(jsonParticipant)
      ).toThrowError(/annotations.*Missing required property/);
    });

    it('fails when creating async from an object with invalid keys', function() {
      const serverObj = { tmp: 1 };
      let catchTriggered = false;

      this.Participant.asyncCreate(serverObj)
        .catch(err => {
          expect(err.message).toContain('Missing required property');
          catchTriggered = true;
        });
      this.$rootScope.$digest();
      expect(catchTriggered).toBeTrue();
    });

  });

  it('can retrieve a single participant', function() {
    const study = this.Factory.study();
    const participant = this.Factory.participant({ studyId: study.id });

    this.$httpBackend.whenGET(this.url('participants', participant.slug))
      .respond(this.reply(participant));

    this.Participant.get(participant.slug)
      .then((reply) => {
        expect(reply).toEqual(jasmine.any(this.Participant));
      });
    this.$httpBackend.flush();
  });

  it('can retrieve a single participant by uniqueId', function() {
    const study = this.Factory.study();
    const participant = this.Factory.participant({ studyId: study.id });

    this.$httpBackend.whenGET(this.url('participants', participant.slug))
      .respond(this.reply(participant));

    this.Participant.get(participant.slug).then((reply) => {
      expect(reply).toEqual(jasmine.any(this.Participant));
    });
    this.$httpBackend.flush();
  });

  it('can add a participant', function() {
    const study = this.Factory.study();
    const jsonParticipant = this.Factory.participant({ studyId: study.id });
    const participant = new this.Participant(_.omit(jsonParticipant, 'id'));
    const reqJson = addJson(participant);

    this.$httpBackend.expectPOST(this.url('participants', study.id), reqJson)
      .respond(this.reply(jsonParticipant));

    participant.add().then((reply) => {
      expect(reply).toEqual(jasmine.any(this.Participant));
    });
    this.$httpBackend.flush();
  });

  it('can add a participant with annotations', function() {
    const fixture = this.fixture(true);
    const reqJson = addJson(fixture.participant);

    this.$httpBackend.expectPOST(this.url('participants', fixture.jsonStudy.id), reqJson)
      .respond(this.reply(fixture.jsonParticipant));

    fixture.participant.add()
      .then((replyParticipant) => {
        expect(replyParticipant.id).toEqual(fixture.jsonParticipant.id);
      });
    this.$httpBackend.flush();
  });

  it('can not add a participant with empty required annotations', function() {
    const biobankApi = this.$injector.get('biobankApi');
    const jsonAnnotationTypes = this.Factory.allAnnotationTypes();
    const replyParticipant = this.Factory.participant();

    spyOn(biobankApi, 'post').and.returnValue(this.$q.when(replyParticipant));

    jsonAnnotationTypes.forEach((serverAnnotationType) => {
      const annotationType  = new this.AnnotationType(serverAnnotationType);
      const jsonStudy       = this.Factory.study({ annotationTypes: [ annotationType ]});
      const study           = this.Study.create(jsonStudy);
      const jsonParticipant = this.Factory.participant();

      jsonParticipant.annotations[0] = this.Factory.annotation({value: undefined}, annotationType);

      const participant = new this.Participant(_.omit(jsonParticipant, 'id'), study);

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
    const study = this.Factory.study();
    const jsonParticipant = this.Factory.participant({ studyId: study.id });
    const participant = new this.Participant(jsonParticipant);
    const newUniqueId = this.Factory.stringNext();
    const serverReply = Object.assign({}, jsonParticipant, { uniqueId: newUniqueId } );

    const updateFunc = () => {
      participant.updateUniqueId(newUniqueId)
        .then((updatedParticipant) => {
          this.expectParticipant(updatedParticipant);
          expect(updatedParticipant.uniqueId).toEqual(newUniqueId);
        })
        .catch(this.failTest);
    };

    this.updateEntityWithCallback(updateFunc,
                            this.url('participants/uniqueId', participant.id),
                            {
                              uniqueId: newUniqueId,
                              expectedVersion: participant.version
                            },
                            serverReply);
  });

  describe('updates to annotations', function () {

    const context = {};

    beforeEach(function () {
      const jsonAnnotationType = this.Factory.annotationType();
      const jsonStudy = this.Factory.study({ annotationTypes: [ jsonAnnotationType ]});
      const jsonParticipant = this.Factory.participant({
        studyId: jsonStudy.id,
        annotationTypes: [ jsonAnnotationType ]
      });
      const study = this.Study.create(jsonStudy);
      const participant = new this.Participant(jsonParticipant, study);

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

    annotationsSharedBehaviour(context);

  });

  function annotationsForCommand(participant) {
    if (!participant.annotations) { return []; }
    return participant.annotations.map((annotation) => annotation.getServerAnnotation());
  }

  function addJson(participant) {
    return Object.assign(_.pick(participant, 'uniqueId'),
                         { annotations: annotationsForCommand(participant) } );
  }

});
