/**
 * Jasmine test suite
 */
define(['angular', 'angularMocks', 'underscore', 'biobankApp'], function(angular, mocks, _) {
  'use strict';

  describe('Service: centresLocationService', function() {

    var centreLocationsService, httpBackend, Location, fakeEntities;
    var centre, location, locationNoId;

    function uri(centreId) {
      var result = '/centres';
      if (arguments.length > 0) {
        result += '/' + centreId;
      }
      result += '/locations';
      return result;
    }

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function (_centreLocationsService_,
                                $httpBackend,
                                _Location_,
                                fakeDomainEntities,
                                extendDomainEntities) {
      centreLocationsService = _centreLocationsService_;
      httpBackend = $httpBackend;
      Location = _Location_;
      fakeEntities = fakeDomainEntities;

      centre = fakeEntities.centre();
      location = fakeEntities.location();
      locationNoId = _.omit(centre, 'id');
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
        data: [location]
      });

      centreLocationsService.list(centre.id).then(function(locations) {
        expect(locations.length).toEqual(1);
        _.each(locations, function(loc) {
          expect(loc).toEqual(jasmine.any(Location));
          loc.compareToServerEntity(location);
        });

      });

      httpBackend.flush();
    });

    it('get should return a valid object', function() {
      jasmine.getEnv().fail();
    });

    it('should allow adding an location to a centre', function() {
      jasmine.getEnv().fail();
    });

    it('should remove a location from a centre', function() {
      jasmine.getEnv().fail();
    });

  });

});
