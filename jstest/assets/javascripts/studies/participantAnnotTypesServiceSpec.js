// Jasmine test suite
//
define(['angular', 'angularMocks', 'underscore', 'biobankApp'], function(angular, mocks, _) {
  'use strict';

  describe('Service: participantAnnotTypesService', function() {

    var participantAnnotTypesService, httpBackend;
    var studyId = 'dummy-study-id';
    var annotTypeNoId = {
      studyId: studyId,
      version: 1,
      timeAdded: '2014-10-20T09:58:43-0600',
      name: 'PAT1',
      description: 'test',
      valueType: 'Text',
      options: [],
      required: true
    };
    var annotType = angular.extend({id: 'dummy-id'}, annotTypeNoId);

    function uri(annotTypeId, version) {
      var result = '/studies/' + studyId + '/pannottypes';
      if (arguments.length > 0) {
        result += '/' + annotTypeId;
      }
      if (arguments.length > 1) {
        result += '/' + version;
      }
      return result;
    }

    beforeEach(mocks.module('biobankApp'));

    beforeEach(inject(function (_participantAnnotTypesService_, $httpBackend) {
      participantAnnotTypesService = _participantAnnotTypesService_;
      httpBackend = $httpBackend;
    }));

    afterEach(function() {
      httpBackend.verifyNoOutstandingExpectation();
      httpBackend.verifyNoOutstandingRequest();
    });

    it('should have the following functions', function () {
      expect(angular.isFunction(participantAnnotTypesService.getAll)).toBe(true);
      expect(angular.isFunction(participantAnnotTypesService.get)).toBe(true);
      expect(angular.isFunction(participantAnnotTypesService.addOrUpdate)).toBe(true);
      expect(angular.isFunction(participantAnnotTypesService.remove)).toBe(true);
    });

    it('list should return a list of containing one annotTypes', function() {
      httpBackend.whenGET(uri()).respond({
        status: 'success',
        data: [annotType]
      });

      participantAnnotTypesService.getAll(studyId).then(function(data) {
        expect(data.length).toEqual(1);
        expect(_.isEqual(annotType, data[0]));
      });

      httpBackend.flush();
    });

    it('get should return valid object', function() {
      httpBackend.whenGET(uri() + '?annotTypeId=' + annotType.id).respond({
        status: 'success',
        data: annotType
      });

      participantAnnotTypesService.get(annotType.studyId, annotType.id).then(function(data) {
        expect(_.isEqual(annotType, data));
      });

      httpBackend.flush();
    });

    it('should allow adding a annotType', function() {
      var expectedResult = {status: 'success', data: 'success'};
      var cmd = {
        studyId:       annotType.studyId,
        name:          annotType.name,
        description:   annotType.description,
        valueType:     annotType.valueType,
        maxValueCount: annotType.maxValueCount,
        options:       annotType.options,
        required:      annotType.required
      };
      httpBackend.expectPOST(uri(), cmd).respond(201, expectedResult);
      participantAnnotTypesService.addOrUpdate(annotTypeNoId).then(function(reply) {
        expect(reply).toEqual('success');
      });
      httpBackend.flush();
    });

    it('should allow updating a annotType', function() {
      var expectedResult = {status: 'success', data: 'success'};
      var cmd = {
        id:              annotType.id,
        expectedVersion: annotType.version,
        studyId:         annotType.studyId,
        name:            annotType.name,
        description:     annotType.description,
        valueType:       annotType.valueType,
        maxValueCount:   annotType.maxValueCount,
        options:         annotType.options,
        required:        annotType.required
      };
      httpBackend.expectPUT(uri(annotType.id), cmd).respond(201, expectedResult);
      participantAnnotTypesService.addOrUpdate(annotType).then(function(reply) {
        expect(reply).toEqual('success');
      });
      httpBackend.flush();
    });

    it('should remove a annotType', function() {
      var expectedResult = {status: 'success', data: 'success'};
      httpBackend.expectDELETE(uri(annotType.id, annotType.version)).respond(201);
      participantAnnotTypesService.remove(annotType);
      httpBackend.flush();
    });

  });

});
