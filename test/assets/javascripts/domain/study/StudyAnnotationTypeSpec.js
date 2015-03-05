/**
 * Jasmine test suite
 */
define([
  'angular',
  'angularMocks',
  'underscore',
  'biobank.testUtils',
  'biobankApp'
], function(angular, mocks, _, testUtils) {
  'use strict';

  fdescribe('StudyAnnotationType', function() {

    var httpBackend, fakeEntities;

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function($httpBackend,
                               fakeDomainEntities) {
      httpBackend  = $httpBackend;
      fakeEntities = fakeDomainEntities;
    }));

    describe('CollectionEventAnnotationType', function() {

      var CollectionEventAnnotationType;

      beforeEach(inject(function(_CollectionEventAnnotationType_) {
        CollectionEventAnnotationType = _CollectionEventAnnotationType_;
      }));

      it('created correctly', function() {
        var annotationType = new CollectionEventAnnotationType();
        expect(annotationType._requiredKeys)
          .toEqual(['studyId', 'id', 'name', 'valueType', 'options']);
        expect(annotationType._addedEventRequiredKeys)
          .toEqual(['studyId', 'annotationTypeId', 'name', 'valueType', 'options']);
        expect(annotationType._updatedEventRequiredKeys)
          .toEqual(['studyId', 'annotationTypeId', 'name', 'valueType', 'options', 'version']);
      });

    });

    describe('ParticipantAnnotationType', function() {

      var ParticipantAnnotationType, participantAnnotTypesService;

      beforeEach(inject(function(_ParticipantAnnotationType_, _participantAnnotTypesService_) {
        ParticipantAnnotationType = _ParticipantAnnotationType_;
        participantAnnotTypesService = _participantAnnotTypesService_;
      }));

      it('created correctly', function() {
        var annotationType = new ParticipantAnnotationType();
        expect(annotationType._requiredKeys)
          .toEqual(['required', 'studyId', 'id', 'name', 'valueType', 'options']);
        expect(annotationType._addedEventRequiredKeys)
          .toEqual(['required', 'studyId', 'annotationTypeId', 'name', 'valueType', 'options']);
        expect(annotationType._updatedEventRequiredKeys)
          .toEqual(['required', 'studyId', 'annotationTypeId', 'name', 'valueType', 'options', 'version']);
      });

      it('can be added', function() {
        var study = fakeEntities.study();
        var serverAnnotType = _.omit(
          fakeEntities.studyAnnotationType(study, { required: true }),
          'id');
        var annotType = new ParticipantAnnotationType(serverAnnotType);
        var command = addCommand(serverAnnotType);
        var reply = serverReply(command);

        httpBackend.expectPOST(uri(study), command).respond(201, reply);

        annotType.addOrUpdate().then(function(reply) {
          expect(reply).toEqual(annotType);
        });
        httpBackend.flush();
      });

      it('can be updated', function() {
        var study = fakeEntities.study();
        var serverAnnotType = fakeEntities.studyAnnotationType(study, { required: true });
        var annotType = new ParticipantAnnotationType(serverAnnotType);
        var command = updateCommand(serverAnnotType);
        var reply = serverReply(command);

        httpBackend.expectPUT(uri(study, annotType.id), command).respond(201, reply);

        annotType.addOrUpdate().then(function(reply) {
          expect(reply).toEqual(annotType);
        });
        httpBackend.flush();
      });

      it('when adding fails for invalid response from server', function(done) {
        var study = fakeEntities.study();
        var serverAnnotType = _.omit(
          fakeEntities.studyAnnotationType(study, { required: true }),
          'id');
        var command = addCommand(serverAnnotType);
        var reply = serverReply(command);
        var annotType = new ParticipantAnnotationType(serverAnnotType);

        checkInvalidResponse(annotType._addedEventRequiredKeys,
                             uri(study),
                             serverAnnotType,
                             command,
                             reply,
                             httpBackend.expectPOST,
                             done);
      });

      it('when updating fails for invalid response from server', function(done) {
        var study = fakeEntities.study();
        var serverAnnotType = fakeEntities.studyAnnotationType(study, { required: true });
        var command = updateCommand(serverAnnotType);
        var reply = serverReply(command);
        var annotType = new ParticipantAnnotationType(serverAnnotType);

        checkInvalidResponse(annotType._updatedEventRequiredKeys,
                             uri(study, serverAnnotType.id),
                             serverAnnotType,
                             command,
                             reply,
                             httpBackend.expectPUT,
                             done);
      });

      function uri(/* study, annotTypeId, version */) {
        var args = _.toArray(arguments);
        var study, annotTypeId, version, result;

        if (args.length <= 0) {
          throw new Error('study id specified');
        }

        study = args.shift();
        result = '/studies/' + study.id + '/pannottypes';

        if (args.length > 0) {
          annotTypeId = args.shift();
          result += '/' + annotTypeId;
        }
        if (args.length > 0) {
          version = args.shift();
          result += '/' + version;
        }
        return result;
      }

      function addCommand(annotType) {
        return {
          studyId:     annotType.studyId,
          name:        annotType.name,
          description: annotType.description,
          valueType:   annotType.valueType,
          options:     annotType.options,
          required:    annotType.required
        };
      }

      function updateCommand(annotType) {
        return _.extend(addCommand(annotType), {
          id: annotType.id,
          expectedVersion: annotType.version
        });
      }

      function serverReply(command) {
        var event = _.extend({}, testUtils.renameKeys(command, {
          'id':              'annotationTypeId',
          'expectedVersion': 'version'
        }));

        // if command is add, it is missing the ID key, set a default one for the event
        event = _.defaults(event, { annotationTypeId: 'abc'});

        return { status: 'success', data: event };
      }

      function checkInvalidResponse(replyRequiredKeys, uri, serverAnnotType, command, reply, httpBackendExpectFn, done) {
        var lastReplyKey = _.last(replyRequiredKeys);

        _.each(replyRequiredKeys, function(key) {
          var annotType = new ParticipantAnnotationType(serverAnnotType);
          var badReply = { status: 'success', data: _.omit(reply.data, key) };

          httpBackendExpectFn(uri, command).respond(201, badReply);

          annotType.addOrUpdate().then(function (reply) {
            expect(reply).toEqual(jasmine.any(Error));

            if (key === lastReplyKey) {
              done();
            }
          });
        });

        httpBackend.flush();
      }

    });

    describe('SpecimenLinkAnnotationType', function() {

      var SpecimenLinkAnnotationType;

      beforeEach(inject(function(_SpecimenLinkAnnotationType_) {
        SpecimenLinkAnnotationType = _SpecimenLinkAnnotationType_;
      }));

      it('created correctly', function() {
        var annotationType = new SpecimenLinkAnnotationType();
        expect(annotationType._requiredKeys)
          .toEqual(['studyId', 'id', 'name', 'valueType', 'options']);
        expect(annotationType._addedEventRequiredKeys)
          .toEqual(['studyId', 'annotationTypeId', 'name', 'valueType', 'options']);
        expect(annotationType._updatedEventRequiredKeys)
          .toEqual(['studyId', 'annotationTypeId', 'name', 'valueType', 'options', 'version']);
      });

    });

  });

});
