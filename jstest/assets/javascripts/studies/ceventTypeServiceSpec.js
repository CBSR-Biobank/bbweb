// Jasmine test suite
//
define(['angular', 'angularMocks', 'underscore', 'biobankApp'], function(angular, mocks, _) {
  'use strict';

  describe('Service: ceventTypesService', function() {

    var ceventTypesService, httpBackend;
    var studyId = 'dummy-study-id';
    var ceventTypeNoId = {
      studyId: studyId,
      version: 1,
      timeAdded: '2014-10-20T09:58:43-0600',
      name: 'CET1',
      description: 'test',
      recurring: true,
      specimenGroupData: [],
      annotationTypeData: []
    };
    var ceventType = angular.extend({id: 'dummy-id'}, ceventTypeNoId);

    function uri(ceventTypeId, version) {
      var result = '/studies/' + studyId + '/cetypes';
      if (arguments.length > 0) {
        result += '/' + ceventTypeId;
      }
      if (arguments.length > 1) {
        result += '/' + version;
      }
      return result;
    }

    beforeEach(mocks.module('biobankApp'));

    beforeEach(inject(function (_ceventTypesService_, $httpBackend) {
      ceventTypesService = _ceventTypesService_;
      httpBackend = $httpBackend;
    }));

    afterEach(function() {
      httpBackend.verifyNoOutstandingExpectation();
      httpBackend.verifyNoOutstandingRequest();
    });

    it('should have the following functions', function () {
      expect(angular.isFunction(ceventTypesService.getAll)).toBe(true);
      expect(angular.isFunction(ceventTypesService.get)).toBe(true);
      expect(angular.isFunction(ceventTypesService.addOrUpdate)).toBe(true);
      expect(angular.isFunction(ceventTypesService.remove)).toBe(true);
    });

    it('list should return a list containing one cevent type', function() {
      httpBackend.whenGET(uri()).respond({
        status: 'success',
        data: [ceventType]
      });

      ceventTypesService.getAll(studyId).then(function(data) {
        //console.log(JSON.stringify(data));

        expect(data.length).toEqual(1);
        expect(_.isEqual(ceventType, data[0]));
      });

      httpBackend.flush();
    });

    it('get should return valid object', function() {
      httpBackend.whenGET(uri() + '?cetId=' + ceventType.id).respond({
        status: 'success',
        data: ceventType
      });

      ceventTypesService.get(ceventType.studyId, ceventType.id).then(function(data) {
        expect(_.isEqual(ceventType, data));
      });

      httpBackend.flush();
    });

    it('should allow adding a cevent type', function() {
      var expectedResult = {status: 'success', data: 'success'};
      var cmd = {
        studyId:            ceventType.studyId,
        name:               ceventType.name,
        description:        ceventType.description,
        recurring:          ceventType.recurring,
        specimenGroupData:  ceventType.specimenGroupData,
        annotationTypeData: ceventType.annotationTypeData
      };
      httpBackend.expectPOST(uri(), cmd).respond(201, expectedResult);
      ceventTypesService.addOrUpdate(ceventTypeNoId).then(function(reply) {
        expect(reply).toEqual('success');
      });
      httpBackend.flush();
    });

    it('should allow updating a cevent type', function() {
      var expectedResult = {status: 'success', data: 'success'};
      var cmd = {
        id:                 ceventType.id,
        expectedVersion:    ceventType.version,
        studyId:            ceventType.studyId,
        name:               ceventType.name,
        description:        ceventType.description,
        recurring:          ceventType.recurring,
        specimenGroupData:  ceventType.specimenGroupData,
        annotationTypeData: ceventType.annotationTypeData
      };
      httpBackend.expectPUT(uri(ceventType.id), cmd).respond(201, expectedResult);
      ceventTypesService.addOrUpdate(ceventType).then(function(reply) {
        expect(reply).toEqual('success');
      });
      httpBackend.flush();
    });

    it('should remove a cevent type', function() {
      var expectedResult = {status: 'success', data: 'success'};
      httpBackend.expectDELETE(uri(ceventType.id, ceventType.version)).respond(201);
      ceventTypesService.remove(ceventType);
      httpBackend.flush();
    });

  });

});
