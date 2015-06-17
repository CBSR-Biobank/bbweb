/**
 * Jasmine test suite
 */
define([
  'angular',
  'angularMocks',
  'underscore',
  'faker',
  'moment',
  'biobank.testUtils',
  'biobankApp'
], function(angular, mocks, _, faker, moment, testUtils) {
  'use strict';

  describe('Participant', function() {

    var httpBackend,
        Participant,
        Study,
        Annotation,
        AnnotationValueType,
        ParticipantAnnotationType,
        bbwebConfig,
        fakeEntities;

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function($httpBackend,
                               _Participant_,
                               _Study_,
                               _Annotation_,
                               _AnnotationValueType_,
                               _ParticipantAnnotationType_,
                               _bbwebConfig_,
                               fakeDomainEntities,
                               extendedDomainEntities) {
      httpBackend               = $httpBackend;
      Participant               = _Participant_;
      Study                     = _Study_;
      Annotation                = _Annotation_;
      AnnotationValueType       = _AnnotationValueType_;
      ParticipantAnnotationType = _ParticipantAnnotationType_;
      bbwebConfig               = _bbwebConfig_;
      fakeEntities              = fakeDomainEntities;

      testUtils.addCustomMatchers();
    }));

    it('constructor with no parameters has default values', function() {
      var participant = new Participant();

      expect(participant.id).toBeNull();
      expect(participant.version).toBe(0);
      expect(participant.timeAdded).toBeNull();
      expect(participant.timeModified).toBeNull();
      expect(participant.uniqueId).toBeEmptyString();
    });

    it('constructor with annotation parameter has valid values', function() {
      var serverStudy = fakeEntities.study(),
          study = new Study(serverStudy),
          annotationData = generateAnnotationTypesAndServerAnnotations(serverStudy);

      var participant = new Participant({ annotations: _.pluck(annotationData, 'serverAnnotation') },
                                        study,
                                        _.pluck(annotationData, 'annotationType'));

      _.each(annotationData, function (annotationItem) {
        var annotation = _.findWhere(participant.annotations,
                                     { annotationTypeId: annotationItem.annotationType.id });
        annotation.compareToServerEntity(annotationItem.serverAnnotation);
      });
    });

    it('constructor with study parameter has valid values', function() {
      var study = new Study(fakeEntities.study());
      var participant = new Participant({}, study);

      expect(participant.study).toEqual(study);
      expect(participant.studyId).toBe(study.id);
    });

    it('constructor with annotation type parameters has valid values', function() {
      var serverStudy = fakeEntities.study(),
          study = new Study(serverStudy),
          annotationData = generateAnnotationTypesAndServerAnnotations(serverStudy);

      var participant = new Participant({}, study, _.pluck(annotationData, 'annotationType'));

      expect(participant.annotations).toBeArrayOfSize(annotationData.length);
      _.each(participant.annotations, function (annotation) {
        expect(annotation).toEqual(jasmine.any(Annotation));
      });
    });

    it('constructor with invalid annotation parameter throws error', function() {
      var serverStudy = fakeEntities.study(),
          study = new Study(serverStudy),
          serverAnnotation = {};

      var annotationType = new ParticipantAnnotationType(
        fakeEntities.studyAnnotationType(serverStudy, { valueType: AnnotationValueType.TEXT() }));

      // put an invalid value in serverAnnotation.annotationTypeId
      _.extend(
        serverAnnotation,
        fakeEntities.annotation(fakeEntities.valueForAnnotation(annotationType), annotationType),
        { annotationTypeId: fakeEntities.stringNext() });

      expect(function () {
        return new Participant({ annotations: [ serverAnnotation ] },
                               study,
                               [ annotationType ]);
      }).toThrow(new Error('annotation types not found: ' + serverAnnotation.annotationTypeId));
    });

    it('fails when creating from a non object', function() {
      expect(Participant.create(1))
        .toEqual(new Error('invalid object from server: has the correct keys'));
    });

    it('fails when creating from an object with invalid keys', function() {
      var serverObj = {
        tmp: 1
      };
      expect(Participant.create(serverObj))
        .toEqual(new Error('invalid object from server: has the correct keys'));
    });

    it('fails when creating from an object with annotation with invalid keys', function() {
      var study = fakeEntities.study(),
          serverParticipant = fakeEntities.participant(study);

      serverParticipant.annotations = [{ tmp: 1 }];
      expect(Participant.create(serverParticipant))
        .toEqual(new Error('invalid annotation object from server'));
    });

    it('has valid values when creating from a server response', function() {
      var study = fakeEntities.study(),
          serverParticipant = fakeEntities.participant(study);

      // TODO: add annotations to the server response
      var participant = Participant.create(serverParticipant);
      participant.compareToServerEntity(serverParticipant);
    });

    it('can retrieve a single participant', function(done) {
      var study = fakeEntities.study();
      var participant = fakeEntities.participant({ studyId: study.id });
      httpBackend.whenGET(uri(study.id, participant.id)).respond(serverReply(participant));

      Participant.get(study.id, participant.id).then(function (reply) {
        expect(reply).toEqual(jasmine.any(Participant));
        reply.compareToServerEntity(participant);
        done();
      });
      httpBackend.flush();
    });

    it('can retrieve a single participant by uniqueId', function(done) {
      var study = fakeEntities.study();
      var participant = fakeEntities.participant({ studyId: study.id });
      httpBackend.whenGET(uri(study.id) + '/uniqueId/' + participant.uniqueId)
        .respond(serverReply(participant));

      Participant.getByUniqueId(study.id, participant.uniqueId).then(function (reply) {
        expect(reply).toEqual(jasmine.any(Participant));
        reply.compareToServerEntity(participant);
        done();
      });
      httpBackend.flush();
    });

    it('can add a participant', function(done) {
      var study = fakeEntities.study();
      var baseParticipant = fakeEntities.participant({ studyId: study.id });
      var participant = new Participant(_.omit(baseParticipant, 'id'));
      var cmd = addCommand(participant);

      httpBackend.expectPOST(uri(study.id), cmd).respond(201, serverReply(baseParticipant));

      participant.addOrUpdate().then(function(replyParticipant) {
        expect(replyParticipant.id).toEqual(baseParticipant.id);
        expect(replyParticipant.version).toEqual(0);
        expect(replyParticipant.studyId).toEqual(study.id);
        expect(replyParticipant.uniqueId).toEqual(participant.uniqueId);
        expect(replyParticipant.annotations).toBeArrayOfSize(participant.annotations.length);
        done();
      });
      httpBackend.flush();
    });

    function getParticipantEntities(isNew) {
      var study,
          serverAnnotationTypes,
          serverParticipant,
          annotationTypes,
          participant;

      study = fakeEntities.study();
      serverAnnotationTypes = fakeEntities.allStudyAnnotationTypes(study);
      serverParticipant = fakeEntities.participant({
        studyId:         study.id,
        annotationTypes: serverAnnotationTypes
      });

      annotationTypes = _.map(serverAnnotationTypes, function (serverAnnotationType) {
        return new ParticipantAnnotationType(serverAnnotationType);
      });

      if (isNew) {
        participant = new Participant(_.omit(serverParticipant, 'id'), study, annotationTypes);
      } else {
        participant = new Participant(serverParticipant, study, annotationTypes);
      }

      return {
        serverStudy: study,
        serverAnnotationTypes: serverAnnotationTypes,
        serverParticipant: serverParticipant,
        annotationTypes: annotationTypes,
        participant: participant
      };
    }

    it('can add a participant with annotations', function(done) {
      var entities = getParticipantEntities(true), cmd;

      cmd = addCommand(entities.participant);

      httpBackend.expectPOST(uri(entities.serverStudy.id), cmd)
        .respond(201, serverReply(entities.serverParticipant));

      entities.participant.addOrUpdate().then(function(replyParticipant) {
        expect(replyParticipant.id).toEqual(entities.serverParticipant.id);
        expect(replyParticipant.version).toEqual(0);
        expect(replyParticipant.studyId).toEqual(entities.serverStudy.id);
        expect(replyParticipant.uniqueId).toEqual(entities.participant.uniqueId);
        expect(replyParticipant.annotations).toBeArrayOfSize(entities.participant.annotations.length);
        done();
      });
      httpBackend.flush();
    });

    it('can not add a participant with empty required annotations', function() {
      var study,
          serverAnnotationTypes,
          serverParticipant;

      study = fakeEntities.study();
      serverAnnotationTypes = fakeEntities.allStudyAnnotationTypes(study);

      _.each(serverAnnotationTypes, function (serverAnnotationType) {
        var annotationType = new ParticipantAnnotationType(serverAnnotationType),
            participant = new Participant(_.omit(serverParticipant, 'id'), study, [ annotationType ]);

        _.each(participant.annotations, function (annotation) {
          annotation.required = true;
          expect(annotation.getValue()).toBeFalsy();
        });

        expect(function () {
          participant.addOrUpdate();
        }).toThrow();
      });
    });

    it('can update a participant', function(done) {
      var study = fakeEntities.study();
      var baseParticipant = fakeEntities.participant({ studyId: study.id });
      var participant = new Participant(baseParticipant);
      var cmd = updateCommand(participant);
      var reply = replyParticipant(baseParticipant);

      httpBackend.expectPUT(uri(study.id, participant.id), cmd).respond(201, serverReply(reply));

      participant.addOrUpdate().then(function(replyParticipant) {
        expect(replyParticipant.id).toEqual(baseParticipant.id);
        expect(replyParticipant.version).toEqual(participant.version + 1);
        expect(replyParticipant.studyId).toEqual(study.id);
        expect(replyParticipant.uniqueId).toEqual(participant.uniqueId);
        expect(replyParticipant.annotations).toBeArrayOfSize(participant.annotations.length);
        done();
      });
      httpBackend.flush();
    });

    it('can update a participant with annotations', function(done) {
      var entities = getParticipantEntities(false),
          cmd = updateCommand(entities.participant),
          reply = replyParticipant(entities.serverParticipant);

      httpBackend.expectPUT(uri(entities.serverStudy.id, entities.participant.id), cmd)
        .respond(201, serverReply(reply));

      entities.participant.addOrUpdate().then(function(replyParticipant) {
        expect(replyParticipant.id).toEqual(entities.serverParticipant.id);
        expect(replyParticipant.version).toEqual(entities.participant.version + 1);
        expect(replyParticipant.studyId).toEqual(entities.serverStudy.id);
        expect(replyParticipant.uniqueId).toEqual(entities.participant.uniqueId);
        expect(replyParticipant.annotations).toBeArrayOfSize(entities.participant.annotations.length);
        done();
      });
      httpBackend.flush();
    });

    it('can not update a participant with empty required annotations', function() {
      var study,
          serverAnnotationTypes,
          serverParticipant;

      study = fakeEntities.study();
      serverAnnotationTypes = fakeEntities.allStudyAnnotationTypes(study);

      _.each(serverAnnotationTypes, function (serverAnnotationType) {
        var annotationType = new ParticipantAnnotationType(serverAnnotationType),
            participant = new Participant(serverParticipant, study, [ annotationType ]);

        _.each(participant.annotations, function (annotation) {
          annotation.required = true;
          expect(annotation.getValue()).toBeFalsy();
        });

        expect(function () {
          participant.addOrUpdate();
        }).toThrow();
      });
    });

    function generateAnnotationTypesAndServerAnnotations(serverStudy) {
      var annotationTypes = fakeEntities.allStudyAnnotationTypes(serverStudy);

      return _.map(annotationTypes, function (annotationType) {
        var value = fakeEntities.valueForAnnotation(annotationType);
        var serverAnnotation = fakeEntities.annotation(value, annotationType);

        return {
          annotationType: new ParticipantAnnotationType(annotationType),
          serverAnnotation: serverAnnotation
        };
      });
    }

    function annotationClearValue(annotation) {
      switch (annotation.getValueType()) {

      case AnnotationValueType.TEXT():
        annotation.stringValue = '';
        break;

      case AnnotationValueType.NUMBER():
        annotation.numberValue =  null;
        break;

      case AnnotationValueType.DATE_TIME():
        annotation.dateTimeValue =  {date: null, time: null};
        break;

      case AnnotationValueType.SELECT():
        if (annotation.annotationType.isSingleSelect()) {
          annotation.singleSelectValue = null;
        } else {
          annotation.multipleSelectValue = [];
        }
        break;
      }
    }

    function annotationsForCommand(participant) {
      return _.map(participant.annotations, function (annotation) {
        return annotation.getServerAnnotation();
      });
    }

    function addCommand(participant) {
      return _.extend(_.pick(participant, 'studyId', 'uniqueId'),
                      { annotations: annotationsForCommand(participant) } );
    }

    function updateCommand(participant) {
      return _.extend(_.pick(participant, 'id', 'studyId', 'uniqueId'),
                      { annotations: annotationsForCommand(participant) },
                      testUtils.expectedVersion(participant.version));
    }

    function replyParticipant(participant, newValues) {
      newValues = newValues || {};
      return new Participant(_.extend({}, participant, newValues, {version: participant.version + 1}));
    }

    function serverReply(event) {
      return { status: 'success', data: event };
    }

    function uri(/* studyId, participantId */) {
      var studyId, participantId, result = '/studies',
          args = _.toArray(arguments);

      if (args.length < 1) {
        throw new Error('study id not specified');
      }

      studyId = args.shift();
      result += '/' + studyId + '/participants';

      if (args.length > 0) {
        participantId = args.shift();
        result += '/' + participantId;
      }
      return result;
    }
  });

});
