/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
// Jasmine test suite
//
define([
  'angular',
  'angularMocks',
  'underscore',
  'biobankApp'
], function(angular, mocks, _) {
  'use strict';

  describe('Service: specimenGroupsService', function() {

    var specimenGroupsService, httpBackend, fakeEntities;

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

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

    function uri(studyId) {
      return '/studies/' + studyId + '/sgroups';
    }

    function getValueType(uri, serviceFn) {
      var studyId = fakeEntities.stringNext();

      httpBackend.whenGET('/studies/' + uri).respond({
        status: 'success',
        data: 'success'
      });
      serviceFn(studyId).then(function(data) {
        expect(data).toEqual('success');
      });
      httpBackend.flush();
    }

    it('should retrieve specimen groups in use', function() {
      var studyId = fakeEntities.stringNext(),
          specimenGroupId = fakeEntities.stringNext();

      httpBackend.whenGET(uri(studyId) + '/inuse').respond({
        status: 'success',
        data: [specimenGroupId]
      });

      specimenGroupsService.specimenGroupIdsInUse(studyId)
        .then(function(data) {
          expect(data.length).toEqual(1);
          expect(data[0]).toBe(specimenGroupId);
        });
      httpBackend.flush();
    });

    it('should retrieve specimen group value types', function() {
      getValueType('anatomicalsrctypes', specimenGroupsService.anatomicalSourceTypes);
      getValueType('specimentypes',      specimenGroupsService.specimenTypes);
      getValueType('preservtypes',       specimenGroupsService.preservTypes);
      getValueType('preservtemptypes',   specimenGroupsService.preservTempTypes);
      getValueType('sgvaluetypes',       specimenGroupsService.specimenGroupValueTypes);
    });
  });

});
