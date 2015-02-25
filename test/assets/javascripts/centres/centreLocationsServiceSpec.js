/**
 * Jasmine test suite
 */
define(['angular', 'angularMocks', 'underscore', 'biobankApp'], function(angular, mocks, _) {
  'use strict';

  describe('Service: centreLocationsService', function() {

    var centreLocationsService, httpBackend, fakeEntities;
    var centre, serverLocation, serverLocationNoId;

    function uri(centreId, locationId) {
      var result = '/centres';
      if (arguments.length > 0) {
        result += '/' + centreId;
      }
      result += '/locations';
      if (arguments.length > 1) {
        result += '/' + locationId;
      }
      return result;
    }

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function (_centreLocationsService_,
                                $httpBackend,
                                fakeDomainEntities,
                                extendedDomainEntities) {
      centreLocationsService = _centreLocationsService_;
      httpBackend = $httpBackend;
      fakeEntities = fakeDomainEntities;

      centre = fakeEntities.centre();
      serverLocation = fakeEntities.location();
      serverLocationNoId = _.omit(centre, 'id');
    }));

    afterEach(function() {
      httpBackend.verifyNoOutstandingExpectation();
      httpBackend.verifyNoOutstandingRequest();
    });

    it('should have the following functions', function () {
      expect(centreLocationsService.list).toBeFunction();
      expect(centreLocationsService.query).toBeFunction();
      expect(centreLocationsService.add).toBeFunction();
      expect(centreLocationsService.remove).toBeFunction();
    });

    it('list should return a list containing one location', function() {
      httpBackend.whenGET(uri(centre.id)).respond({
        status: 'success',
        data: [serverLocation]
      });

      centreLocationsService.list(centre.id).then(function(locations) {
        expect(locations.length).toEqual(1);
        _.each(locations, function(loc) {
          expect(loc).toEqual(serverLocation);
        });

      });

      httpBackend.flush();
    });

    it('get should return a valid object', function() {
      httpBackend.whenGET(uri(centre.id) + '?locationId=' + serverLocation.id).respond({
        status: 'success',
        data: serverLocation
      });

      centreLocationsService.query(centre.id, serverLocation.id).then(function(loc) {
        expect(loc).toEqual(serverLocation);
      });

      httpBackend.flush();
    });

    it('should allow adding an location to a centre', function() {
      var cmd = {centreId: centre.id, locationId: serverLocation.id};
      var expectedResult = {status: 'success', data: 'success'};
      httpBackend.expectPOST(uri(centre.id), cmd).respond(201, expectedResult);
      centreLocationsService.add(centre, serverLocation).then(function(reply) {
        expect(reply).toEqual('success');
      });
      httpBackend.flush();
    });

    it('should remove a location from a centre', function() {
      httpBackend.expectDELETE(uri(centre.id, serverLocation.id)).respond(201);
      centreLocationsService.remove(centre.id, serverLocation.id);
      httpBackend.flush();
    });

  });

});
