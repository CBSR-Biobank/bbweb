/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
/* global angular */

import ngModule from '../../index'

xdescribe('Service: specimenGroupsService', function() {

  var specimenGroupsService, httpBackend, Factory;

  beforeEach(() => {
    angular.mock.module(ngModule, 'biobank.test');
    angular.mock.inject(function ($httpBackend,
                                  _specimenGroupsService_,
                                  _Factory_) {
      specimenGroupsService = _specimenGroupsService_;
      httpBackend = $httpBackend;
      Factory = _Factory_;
    });
  });

  afterEach(function() {
    httpBackend.verifyNoOutstandingExpectation();
    httpBackend.verifyNoOutstandingRequest();
  });

  function uri(studyId) {
    return '/studies/' + studyId + '/sgroups';
  }

  function getValueType(uri, serviceFn) {
    var studyId = Factory.stringNext();

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
    var studyId = Factory.stringNext(),
        specimenGroupId = Factory.stringNext();

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
