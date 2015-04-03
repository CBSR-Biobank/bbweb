// Jasmine test suite
//
define(['angular', 'angularMocks', 'underscore', 'biobankApp'], function(angular, mocks, _) {
  'use strict';

  describe('Service: specimenGroupsService', function() {

    var specimenGroupsService, httpBackend, fakeEntities;
    var studyId = 'dummy-study-id';

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

    beforeEach(inject(function ($httpBackend,
                                _specimenGroupsService_,
                               fakeDomainEntities) {
      specimenGroupsService = _specimenGroupsService_;
      httpBackend = $httpBackend;
      fakeEntities = fakeDomainEntities;
    }));

    afterEach(function() {
      httpBackend.verifyNoOutstandingExpectation();
      httpBackend.verifyNoOutstandingRequest();
    });


    it('should retrieve specimen groups in use', function() {
      var specimenGroup = fakeEntities.
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
