// Jasmine test suite
//
define(['angular', 'angularMocks', 'underscore', 'biobankApp'], function(angular, mocks, _) {
  'use strict';

  describe('Service: participantsService', function() {

    var participantsService, httpBackend;
    var studyId = 'dummy-study-id';
    var participantNoId = {
      studyId: studyId,
      version: 1,
      timeAdded: '2014-10-20T09:58:43-0600',
      uniqueId: 'participant1',
      annotations: []
    };
    var participant = angular.extend({id: 'dummy-id'}, participantNoId);

    function uri(participantId) {
      var result = '/studies/' + studyId + '/participants';
      if (arguments.length > 0) {
        result += '/' + participantId;
      }
      return result;
    }

    beforeEach(mocks.module('biobankApp'));

    beforeEach(inject(function (_participantsService_, $httpBackend) {
      participantsService = _participantsService_;
      httpBackend = $httpBackend;
    }));

    afterEach(function() {
      httpBackend.verifyNoOutstandingExpectation();
      httpBackend.verifyNoOutstandingRequest();
    });

    it('should have the following functions', function () {
      expect(angular.isFunction(participantsService.get)).toBe(true);
      expect(angular.isFunction(participantsService.getByUniqueId)).toBe(true);
      expect(angular.isFunction(participantsService.addOrUpdate)).toBe(true);
    });

    it('get should return valid object', function() {
      httpBackend.whenGET(uri(participant.id)).respond({
        status: 'success',
        data: participant
      });

      participantsService.get(participant.studyId, participant.id).then(function(data) {
        expect(_.isEqual(participant, data));
      });

      httpBackend.flush();
    });

    it('should allow getting a participant by unique ID', function() {
      httpBackend.whenGET('/studies/' + studyId + '/participants/uniqueId/' + participant.uniqueId).respond({
        status: 'success',
        data: participant
      });

      participantsService.getByUniqueId(studyId, participant.uniqueId).then(function(data) {
        expect(_.isEqual(participant, data));
      });

      httpBackend.flush();
    });

    it('should allow adding a participant', function() {
      var cmd = {
        studyId:     participant.studyId,
        uniqueId:    participant.uniqueId,
        annotations: participant.annotations
      };
      var expectedResult = {status: 'success', data: 'success'};
      httpBackend.expectPOST(uri(), cmd).respond(201, expectedResult);
      participantsService.addOrUpdate(participantNoId).then(function(reply) {
        expect(reply).toEqual('success');
      });
      httpBackend.flush();
    });

    it('should allow updating a participant', function() {
      var cmd = {
        id:              participant.id,
        expectedVersion: participant.version,
        studyId:         participant.studyId,
        uniqueId:        participant.uniqueId,
        annotations:     participant.annotations
      };
      var expectedResult = {status: 'success', data: 'success'};
      httpBackend.expectPUT(uri(participant.id), cmd).respond(201, expectedResult);
      participantsService.addOrUpdate(participant).then(function(reply) {
        expect(reply).toEqual('success');
      });
      httpBackend.flush();
    });

  });

});
