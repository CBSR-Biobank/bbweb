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

  describe('Participant', function() {

    var httpBackend, Participant, fakeEntities;

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function($httpBackend,
                               _Participant_,
                               fakeDomainEntities,
                               extendedDomainEntities) {
      httpBackend  = $httpBackend;
      Participant = _Participant_;
      fakeEntities = fakeDomainEntities;
    }));

    it('constructor with no parameters has default values', function() {
      var participant = new Participant();

      expect(participant.id).toBeNull();
      expect(participant.version).toBe(0);
      expect(participant.timeAdded).toBeNull();
      expect(participant.timeModified).toBeNull();
      expect(participant.uniqueId).toBeEmptyString();
    });

    it('fails when creating from a non object', function() {
      expect(Participant.create(1))
        .toEqual(new Error('invalid object from server: has the correct keys'));
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

    it('can updatecentrecmd a participant', function(done) {
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

    function addCommand(participant) {
      return  _.pick(participant, 'studyId', 'uniqueId', 'annotations');
    }

    function updateCommand(participant) {
      return _.extend(_.pick(participant, 'id', 'studyId', 'uniqueId', 'annotations'),
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
