// Jasmine test suite
//
define(['angular', 'angularMocks', 'underscore', 'biobankApp'], function(angular, mocks, _) {
  'use strict';

  describe('Service: specimenGroupsService', function() {

    var specimenGroupsService, httpBackend;
    var studyId = 'dummy-study-id';
    var specimenGroupNoId = {
      studyId:                     studyId,
      version:                     1,
      timeAdded:                   '2014-10-20T09:58:43-0600',
      name:                        'CET1',
      description:                 'test',
      units:                       'mL',
      anatomicalSourceType:        'Blood',
      preservationType:            'Frozen Specimen',
      preservationTemperatureType: '-80 C',
      specimenType:                'Buffy coat'
    };
    var specimenGroup = angular.extend({id: 'dummy-id'}, specimenGroupNoId);

    function uri(specimenGroupId, version) {
      var result = '/studies/' + studyId + '/sgroups';
      if (arguments.length > 0) {
        result += '/' + specimenGroupId;
      }
      if (arguments.length > 1) {
        result += '/' + version;
      }
      return result;
    }

    beforeEach(mocks.module('biobankApp'));

    beforeEach(inject(function (_specimenGroupsService_, $httpBackend) {
      specimenGroupsService = _specimenGroupsService_;
      httpBackend = $httpBackend;
    }));

    afterEach(function() {
      httpBackend.verifyNoOutstandingExpectation();
      httpBackend.verifyNoOutstandingRequest();
    });

    it('should have the following functions', function () {
      expect(angular.isFunction(specimenGroupsService.getAll)).toBe(true);
      expect(angular.isFunction(specimenGroupsService.get)).toBe(true);
      expect(angular.isFunction(specimenGroupsService.addOrUpdate)).toBe(true);
      expect(angular.isFunction(specimenGroupsService.remove)).toBe(true);
      expect(angular.isFunction(specimenGroupsService.anatomicalSourceTypes)).toBe(true);
      expect(angular.isFunction(specimenGroupsService.specimenTypes)).toBe(true);
      expect(angular.isFunction(specimenGroupsService.preservTypes)).toBe(true);
      expect(angular.isFunction(specimenGroupsService.preservTempTypes)).toBe(true);
      expect(angular.isFunction(specimenGroupsService.specimenGroupValueTypes)).toBe(true);
      expect(angular.isFunction(specimenGroupsService.specimenGroupIdsInUse)).toBe(true);

    });

    it('list should return a list containing one specimen group', function() {
      httpBackend.whenGET(uri()).respond({
        status: 'success',
        data: [specimenGroup]
      });

      specimenGroupsService.getAll(studyId).then(function(data) {
        //console.log(JSON.stringify(data));

        expect(data.length).toEqual(1);
        expect(_.isEqual(specimenGroup, data[0]));
      });

      httpBackend.flush();
    });

    it('get should return valid object', function() {
      httpBackend.whenGET(uri() + '?sgId=' + specimenGroup.id).respond({
        status: 'success',
        data: specimenGroup
      });

      specimenGroupsService.get(specimenGroup.studyId, specimenGroup.id).then(function(data) {
        expect(_.isEqual(specimenGroup, data));
      });

      httpBackend.flush();
    });

    it('should allow adding a specimen group', function() {
      var expectedResult = {status: 'success', data: 'success'};
      var cmd = {
        studyId:                     specimenGroup.studyId,
        name:                        specimenGroup.name,
        description:                 specimenGroup.description,
        units:                       specimenGroup.units,
        anatomicalSourceType:        specimenGroup.anatomicalSourceType,
        preservationType:            specimenGroup.preservationType,
        preservationTemperatureType: specimenGroup.preservationTemperatureType,
        specimenType:                specimenGroup.specimenType
      };
      httpBackend.expectPOST(uri(), cmd).respond(201, expectedResult);
      specimenGroupsService.addOrUpdate(specimenGroupNoId).then(function(reply) {
        expect(reply).toEqual('success');
      });
      httpBackend.flush();
    });

    it('should allow updating a specimen group', function() {
      var expectedResult = {status: 'success', data: 'success'};
      var cmd = {
        id:                          specimenGroup.id,
        expectedVersion:             specimenGroup.version,
        studyId:                     specimenGroup.studyId,
        name:                        specimenGroup.name,
        description:                 specimenGroup.description,
        units:                       specimenGroup.units,
        anatomicalSourceType:        specimenGroup.anatomicalSourceType,
        preservationType:            specimenGroup.preservationType,
        preservationTemperatureType: specimenGroup.preservationTemperatureType,
        specimenType:                specimenGroup.specimenType
      };
      httpBackend.expectPUT(uri(specimenGroup.id), cmd).respond(201, expectedResult);
      specimenGroupsService.addOrUpdate(specimenGroup).then(function(reply) {
        expect(reply).toEqual('success');
      });
      httpBackend.flush();
    });

    it('should remove a specimen group', function() {
      var expectedResult = {status: 'success', data: 'success'};
      httpBackend.expectDELETE(uri(specimenGroup.id, specimenGroup.version)).respond(201);
      specimenGroupsService.remove(specimenGroup);
      httpBackend.flush();
    });

    it('should retrieve specimen groups in use', function() {
      httpBackend.whenGET(uri() + '/inuse').respond({
        status: 'success',
        data: [specimenGroup]
      });

      specimenGroupsService.specimenGroupIdsInUse(studyId).then(function(data) {
        expect(data.length).toEqual(1);
        expect(_.isEqual(specimenGroup, data[0]));
      });

      httpBackend.flush();
    });

    function getValueType(uri, serviceFn) {
      httpBackend.whenGET('/studies/' + uri).respond({
        status: 'success',
        data: 'success'
      });
      serviceFn(studyId).then(function(data) {
        expect(data).toEqual('success');
      });
      httpBackend.flush();
    }

    it('should retrieve specimen group value types', function() {
      getValueType('anatomicalsrctypes', specimenGroupsService.anatomicalSourceTypes);
      getValueType('specimentypes',      specimenGroupsService.specimenTypes);
      getValueType('preservtypes',       specimenGroupsService.preservTypes);
      getValueType('preservtemptypes',   specimenGroupsService.preservTempTypes);
      getValueType('sgvaluetypes',       specimenGroupsService.specimenGroupValueTypes);
    });
  });

});
