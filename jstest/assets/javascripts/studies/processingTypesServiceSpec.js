// Jasmine test suite
//
define(['angular', 'angularMocks', 'underscore', 'biobankApp'], function(angular, mocks, _) {
  'use strict';

  describe('Service: processingTypesService', function() {

    var processingTypesService, httpBackend;
    var studyId = 'dummy-study-id';
    var processingTypeNoId = {
      studyId:     studyId,
      version:     1,
      timeAdded:   '2014-10-20T09:58:43-0600',
      name:        'PT1',
      description: 'test',
      enabled:      true
    };
    var processingType = angular.extend({id: 'dummy-id'}, processingTypeNoId);

    function uri(processingTypeId, version) {
      var result = '/studies/' + studyId + '/proctypes';
      if (arguments.length > 0) {
        result += '/' + processingTypeId;
      }
      if (arguments.length > 1) {
        result += '/' + version;
      }
      return result;
    }

    beforeEach(mocks.module('biobankApp'));

    beforeEach(inject(function (_processingTypesService_, $httpBackend) {
      processingTypesService = _processingTypesService_;
      httpBackend = $httpBackend;
    }));

    afterEach(function() {
      httpBackend.verifyNoOutstandingExpectation();
      httpBackend.verifyNoOutstandingRequest();
    });

    it('should have the following functions', function () {
      expect(angular.isFunction(processingTypesService.getAll)).toBe(true);
      expect(angular.isFunction(processingTypesService.get)).toBe(true);
      expect(angular.isFunction(processingTypesService.addOrUpdate)).toBe(true);
      expect(angular.isFunction(processingTypesService.remove)).toBe(true);
    });

    it('list should return a list containing one processing type', function() {
      httpBackend.whenGET(uri()).respond({
        status: 'success',
        data: [processingType]
      });

      processingTypesService.getAll(studyId).then(function(data) {
        //console.log(JSON.stringify(data));

        expect(data.length).toEqual(1);
        expect(_.isEqual(processingType, data[0]));
      });

      httpBackend.flush();
    });

    it('get should return valid object', function() {
      httpBackend.whenGET(uri() + '?procTypeId=' + processingType.id).respond({
        status: 'success',
        data: processingType
      });

      processingTypesService.get(processingType.studyId, processingType.id).then(function(data) {
        expect(_.isEqual(processingType, data));
      });

      httpBackend.flush();
    });

    it('should allow adding a processing type', function() {
      var expectedResult = {status: 'success', data: 'success'};
      var cmd = {
        studyId:            processingType.studyId,
        name:               processingType.name,
        description:        processingType.description,
        enabled:            processingType.enabled
      };
      httpBackend.expectPOST(uri(), cmd).respond(201, expectedResult);
      processingTypesService.addOrUpdate(processingTypeNoId).then(function(reply) {
        expect(reply).toEqual('success');
      });
      httpBackend.flush();
    });

    it('should allow updating a processing type', function() {
      var expectedResult = {status: 'success', data: 'success'};
      var cmd = {
        id:                 processingType.id,
        expectedVersion:    processingType.version,
        studyId:            processingType.studyId,
        name:               processingType.name,
        description:        processingType.description,
        enabled:            processingType.enabled
      };
      httpBackend.expectPUT(uri(processingType.id), cmd).respond(201, expectedResult);
      processingTypesService.addOrUpdate(processingType).then(function(reply) {
        expect(reply).toEqual('success');
      });
      httpBackend.flush();
    });

    it('should remove a processing type', function() {
      var expectedResult = {status: 'success', data: 'success'};
      httpBackend.expectDELETE(uri(processingType.id, processingType.version)).respond(201);
      processingTypesService.remove(processingType);
      httpBackend.flush();
    });

  });

});
