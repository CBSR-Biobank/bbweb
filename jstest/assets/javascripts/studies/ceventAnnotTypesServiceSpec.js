// Jasmine test suite
//
define(['angular', 'angularMocks', 'underscore', 'biobankApp'], function(angular, mocks, _) {
  'use strict';

  describe('Service: ceventAnnotTypesService', function() {

    var ceventAnnotTypesService, httpBackend;
    var studyId = 'dummy-study-id';
    var annotTypeNoId = {
      studyId: studyId,
      version: 1,
      timeAdded: '2014-10-20T09:58:43-0600',
      name: 'CEAT1',
      description: 'test',
      valueType: 'Text',
      maxValueCount: null
    };
    var annotType = angular.extend({id: 'dummy-id'}, annotTypeNoId);

    function uri(annotTypeId, version) {
      var result = '/studies/' + studyId + '/ceannottypes';
      if (arguments.length > 0) {
        result += '/' + annotTypeId;
      }
      if (arguments.length > 1) {
        result += '/' + version;
      }
      return result;
    }

    beforeEach(mocks.module('biobankApp'));

    beforeEach(inject(function (_ceventAnnotTypesService_, $httpBackend) {
      ceventAnnotTypesService = _ceventAnnotTypesService_;
      httpBackend = $httpBackend;
    }));

    afterEach(function() {
      httpBackend.verifyNoOutstandingExpectation();
      httpBackend.verifyNoOutstandingRequest();
    });

    it('should have the following functions', function () {
      expect(angular.isFunction(ceventAnnotTypesService.getAll)).toBe(true);
      expect(angular.isFunction(ceventAnnotTypesService.get)).toBe(true);
      expect(angular.isFunction(ceventAnnotTypesService.addOrUpdate)).toBe(true);
      expect(angular.isFunction(ceventAnnotTypesService.remove)).toBe(true);
    });

    it('list should return a list containing one annotTypes', function() {
      httpBackend.whenGET(uri()).respond({
        status: 'success',
        data: [annotType]
      });

      ceventAnnotTypesService.getAll(studyId).then(function(data) {
        //console.log(JSON.stringify(data));

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

      ceventAnnotTypesService.get(annotType.studyId, annotType.id).then(function(data) {
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
        options:       annotType.options
      };
      httpBackend.expectPOST(uri(), cmd).respond(201, expectedResult);
      ceventAnnotTypesService.addOrUpdate(annotTypeNoId).then(function(reply) {
        expect(reply).toEqual('success');
      });
      httpBackend.flush();
    });

    it('should allow updating a annotType', function() {
      var expectedResult = {status: 'success', data: 'success'};
      var cmd = {
        id:             annotType.id,
        expectedVersion: annotType.version,
        studyId:         annotType.studyId,
        name:            annotType.name,
        description:     annotType.description,
        valueType:       annotType.valueType,
        maxValueCount:   annotType.maxValueCount,
        options:         annotType.options
      };
      httpBackend.expectPUT(uri(annotType.id), cmd).respond(201, expectedResult);
      ceventAnnotTypesService.addOrUpdate(annotType).then(function(reply) {
        expect(reply).toEqual('success');
      });
      httpBackend.flush();
    });

    it('should remove a annotType', function() {
      var expectedResult = {status: 'success', data: 'success'};
      httpBackend.expectDELETE(uri(annotType.id, annotType.version)).respond(201);
      ceventAnnotTypesService.remove(annotType);
      httpBackend.flush();
    });

  });

});
