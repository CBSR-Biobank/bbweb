// Jasmine test suite
//
define(['angular', 'angularMocks', 'underscore', 'biobankApp'], function(angular, mocks, _) {
  'use strict';

  ddescribe('Service: participantsService', function() {

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
      expect(angular.isFunction(participantsService.addOrUpdate)).toBe(true);
      expect(angular.isFunction(participantsService.checkUnique)).toBe(true);
    });

  });

});
