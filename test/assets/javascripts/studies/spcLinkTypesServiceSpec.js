// Jasmine test suite
//
define(['angular', 'angularMocks', 'underscore', 'biobankApp'], function(angular, mocks, _) {
  'use strict';

  describe('Service: spcLinkTypesService', function() {

    var spcLinkTypesService, httpBackend;
    var processingTypeId = 'dummy-processing-type-id';
    var spcLinkTypeNoId = {
      processingTypeId:      processingTypeId,
      version:               1,
      timeAdded:             '2014-10-20T09:58:43-0600',
      expectedInputChange:   0.1,
      expectedOutputChange:  0.1,
      inputCount:            1,
      outputCount:           1,
      inputGroupId:          'dummy-specimen-group-id-1',
      outputGroupId:         'dummy-specimen-group-id-2',
      inputContainerTypeId:  null,
      outputContainerTypeId: null,
      annotationTypeData:    []
    };
    var spcLinkType = angular.extend({id: 'dummy-id'}, spcLinkTypeNoId);

    function uri(spcLinkTypeId, version) {
      var result = '/studies/' + processingTypeId + '/sltypes';
      if (arguments.length > 0) {
        result += '/' + spcLinkTypeId;
      }
      if (arguments.length > 1) {
        result += '/' + version;
      }
      return result;
    }

    beforeEach(mocks.module('biobankApp'));

    beforeEach(inject(function (_spcLinkTypesService_, $httpBackend) {
      spcLinkTypesService = _spcLinkTypesService_;
      httpBackend = $httpBackend;
    }));

    afterEach(function() {
      httpBackend.verifyNoOutstandingExpectation();
      httpBackend.verifyNoOutstandingRequest();
    });

    it('should have the following functions', function () {
      expect(angular.isFunction(spcLinkTypesService.getAll)).toBe(true);
      expect(angular.isFunction(spcLinkTypesService.get)).toBe(true);
      expect(angular.isFunction(spcLinkTypesService.addOrUpdate)).toBe(true);
      expect(angular.isFunction(spcLinkTypesService.remove)).toBe(true);
    });

    it('list should return a list containing one spcLink type', function() {
      httpBackend.whenGET(uri()).respond({
        status: 'success',
        data: [spcLinkType]
      });

      spcLinkTypesService.getAll(processingTypeId).then(function(data) {
        expect(data.length).toEqual(1);
        expect(_.isEqual(spcLinkType, data[0]));
      });

      httpBackend.flush();
    });

    it('get should return valid object', function() {
      httpBackend.whenGET(uri() + '?slTypeId=' + spcLinkType.id).respond({
        status: 'success',
        data: spcLinkType
      });

      spcLinkTypesService.get(spcLinkType.processingTypeId, spcLinkType.id).then(function(data) {
        expect(_.isEqual(spcLinkType, data));
      });

      httpBackend.flush();
    });

    it('should allow adding a spcLink type', function() {
      var expectedResult = {status: 'success', data: 'success'};
      var cmd = {
        processingTypeId:      spcLinkType.processingTypeId,
        expectedInputChange:   spcLinkType.expectedInputChange,
        expectedOutputChange:  spcLinkType.expectedOutputChange,
        inputCount:            spcLinkType.inputCount,
        outputCount:           spcLinkType.outputCount,
        inputGroupId:          spcLinkType.inputGroupId,
        outputGroupId:         spcLinkType.outputGroupId,
        inputContainerTypeId:  spcLinkType.inputContainerTypeId,
        outputContainerTypeId: spcLinkType.outputContainerTypeId,
        annotationTypeData:    spcLinkType.annotationTypeData
      };
      httpBackend.expectPOST(uri(), cmd).respond(201, expectedResult);
      spcLinkTypesService.addOrUpdate(spcLinkTypeNoId).then(function(reply) {
        expect(reply).toEqual('success');
      });
      httpBackend.flush();
    });

    it('should allow updating a spcLink type', function() {
      var expectedResult = {status: 'success', data: 'success'};
      var cmd = {
        id:                    spcLinkType.id,
        expectedVersion:       spcLinkType.version,
        processingTypeId:      spcLinkType.processingTypeId,
        expectedInputChange:   spcLinkType.expectedInputChange,
        expectedOutputChange:  spcLinkType.expectedOutputChange,
        inputCount:            spcLinkType.inputCount,
        outputCount:           spcLinkType.outputCount,
        inputGroupId:          spcLinkType.inputGroupId,
        outputGroupId:         spcLinkType.outputGroupId,
        inputContainerTypeId:  spcLinkType.inputContainerTypeId,
        outputContainerTypeId: spcLinkType.outputContainerTypeId,
        annotationTypeData:    spcLinkType.annotationTypeData
      };
      httpBackend.expectPUT(uri(spcLinkType.id), cmd).respond(201, expectedResult);
      spcLinkTypesService.addOrUpdate(spcLinkType).then(function(reply) {
        expect(reply).toEqual('success');
      });
      httpBackend.flush();
    });

    it('should remove a spcLink type', function() {
      httpBackend.expectDELETE(uri(spcLinkType.id, spcLinkType.version)).respond(201);
      spcLinkTypesService.remove(spcLinkType);
      httpBackend.flush();
    });

  });

});
