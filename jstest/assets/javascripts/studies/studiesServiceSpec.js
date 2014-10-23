// Jasmine test suite
//
define(['angular', 'angularMocks', 'underscore', 'biobankApp'], function(angular, mocks, _) {
  'use strict';

  describe('Service: studiesService', function() {

    var studiesService, httpBackend;
    var studyId = 'dummy-study-id';
    var studyNoId = {
      version:     1,
      timeAdded:   '2014-10-20T09:58:43-0600',
      name:        'ST1',
      description: 'test'
    };
    var study = angular.extend({id: 'dummy-id'}, studyNoId);

    function uri(studyId) {
      var result = '/studies';
      if (arguments.length > 0) {
        result += '/' + studyId;
      }
      return result;
    }

    beforeEach(mocks.module('biobankApp'));

    beforeEach(inject(function (_studiesService_, $httpBackend) {
      studiesService = _studiesService_;
      httpBackend = $httpBackend;
    }));

    afterEach(function() {
      httpBackend.verifyNoOutstandingExpectation();
      httpBackend.verifyNoOutstandingRequest();
    });

    it('should have the following functions', function () {
      expect(angular.isFunction(studiesService.getAll)).toBe(true);
      expect(angular.isFunction(studiesService.get)).toBe(true);
      expect(angular.isFunction(studiesService.addOrUpdate)).toBe(true);
      expect(angular.isFunction(studiesService.enable)).toBe(true);
      expect(angular.isFunction(studiesService.disable)).toBe(true);
      expect(angular.isFunction(studiesService.retire)).toBe(true);
      expect(angular.isFunction(studiesService.unretire)).toBe(true);
      expect(angular.isFunction(studiesService.processingDto)).toBe(true);
    });

    it('list should return a list containing one study', function() {
      httpBackend.whenGET(uri()).respond({
        status: 'success',
        data: [study]
      });

      studiesService.getAll().then(function(data) {
        //console.log(JSON.stringify(data));

        expect(data.length).toEqual(1);
        expect(_.isEqual(study, data[0]));
      });

      httpBackend.flush();
    });

    it('get should return valid object', function() {
      httpBackend.whenGET(uri(study.id)).respond({
        status: 'success',
        data: study
      });

      studiesService.get(study.id).then(function(data) {
        expect(_.isEqual(study, data));
      });

      httpBackend.flush();
    });

    it('should allow adding a study', function() {
      var expectedResult = {status: 'success', data: 'success'};
      var cmd = {
        studyId:            study.studyId,
        name:               study.name,
        description:        study.description
      };
      httpBackend.expectPOST(uri(), cmd).respond(201, expectedResult);
      studiesService.addOrUpdate(studyNoId).then(function(reply) {
        expect(reply).toEqual('success');
      });
      httpBackend.flush();
    });

    it('should allow updating a study', function() {
      var expectedResult = {status: 'success', data: 'success'};
      var cmd = {
        id:                 study.id,
        expectedVersion:    study.version,
        studyId:            study.studyId,
        name:               study.name,
        description:        study.description
      };
      httpBackend.expectPUT(uri(study.id), cmd).respond(201, expectedResult);
      studiesService.addOrUpdate(study).then(function(reply) {
        expect(reply).toEqual('success');
      });
      httpBackend.flush();
    });

    function studyStatusChange(status, serviceFn) {
      var expectedCmd = { id: study.id, expectedVersion: study.version};
      var expectedResult = {status: 'success', data: 'success'};;
      httpBackend.expectPOST(uri(study.id) + '/' + status, expectedCmd).respond(201, expectedResult);
      serviceFn(study).then(function(reply) {
        expect(reply).toEqual('success');
      });
      httpBackend.flush();
    }

    it('should allow enabling a study', function() {
      studyStatusChange('enable', studiesService.enable);
    });

    it('should allow disabling a study', function() {
      studyStatusChange('disable', studiesService.disable);
    });

    it('should allow retiring a study', function() {
      studyStatusChange('retire', studiesService.retire);
    });

    it('should allow unretiring a study', function() {
      studyStatusChange('unretire', studiesService.unretire);
    });

    it('processingDto should return valid object', function() {
      httpBackend.whenGET(uri(study.id) + '/dto/processing/').respond({
        status: 'success',
        data: 'success'
      });

      studiesService.processingDto(study.id).then(function(data) {
        expect(data).toBe('success');
      });

      httpBackend.flush();
    });

  });

});
