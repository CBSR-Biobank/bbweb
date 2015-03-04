/**
 * Jasmine test suite
 */
define(['angular', 'angularMocks', 'underscore', 'biobankApp'], function(angular, mocks, _) {
  'use strict';

  function uri(centreId) {
    var result = '/centres';
    if (arguments.length > 0) {
      result += '/' + centreId;
    }
    return result;
  }

  /**
   * For now these tests test the interaction between the class and the server.
   *
   * At the moment not sure if we need the service layer if it is decided that the domain model objects
   * encapuslate all the behaviour. If the service layer is kept then these tests will have to be modified
   * and only mock the service methods in 'centresService'.
   */
  describe('Centre', function() {

    var httpBackend, Centre, Location, fakeEntities;

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function($httpBackend,
                               _Centre_,
                               _Location_,
                               fakeDomainEntities,
                               extendedDomainEntities) {
      httpBackend  = $httpBackend;
      Centre       = _Centre_;
      Location     = _Location_;
      fakeEntities = fakeDomainEntities;
    }));

    it('constructor with no parameters has default values', function() {
      var centre = new Centre();
      expect(centre.id).toBeNull();
      expect(centre.version).toBe(0);
      expect(centre.timeAdded).toBeNull();
      expect(centre.timeModified).toBeNull();
      expect(centre.name).toBeEmptyString();
      expect(centre.description).toBeNull();
      expect(centre.locations).toBeEmptyArray();
      expect(centre.studyIds).toBeEmptyArray();
    });

    it('cannot add a location to a new centre', function() {
      var centre = new Centre();
      expect(function () {
        centre.addLocation(new Location());
      }).toThrow(new Error('id is null'));
    });

    it('cannot remove a location from a new centre', function() {
      var centre = new Centre();
      expect(function () {
        centre.removeLocation(new Location());
      }).toThrow(new Error('id is null'));
    });

    it('cannot add a study to a new centre', function() {
      var centre = new Centre();
      expect(function () {
        centre.addStudy({});
      }).toThrow(new Error('id is null'));
    });

    it('cannot remove a study from a new centre', function() {
      var centre = new Centre();
      expect(function () {
        centre.removeStudy({});
      }).toThrow(new Error('id is null'));
    });

    it('can retrieve centres', function(done) {
      var serverReply = [fakeEntities.centre()];
      httpBackend.whenGET(uri()).respond({
        status: 'success',
        data: serverReply
      });

      Centre.list().then(function (centres) {
        expect(centres).toBeArrayOfSize(serverReply.length);
        expect(centres[0]).toEqual(jasmine.any(Centre));
        centres[0].compareToServerEntity(serverReply[0]);
        done();
      });
      httpBackend.flush();
    });

    it('can retrieve a single centre', function(done) {
      var serverReply = fakeEntities.centre();
      var centreId = serverReply.id;
      httpBackend.whenGET(uri(centreId)).respond({
        status: 'success',
        data: serverReply
      });

      Centre.get(centreId).then(function (centre) {
        expect(centre).toEqual(jasmine.any(Centre));
        centre.compareToServerEntity(serverReply);
        done();
      });
      httpBackend.flush();
    });

    describe('locations', function () {

      it('can retrieve centre locations', function(done) {
        var serverCentre = fakeEntities.centre();
        var centre = new Centre(serverCentre);
        var serverReply = [fakeEntities.location(serverCentre)];

        httpBackend.whenGET(uri(centre.id) + '/locations').respond({
          status: 'success',
          data: serverReply
        });

        centre.getLocations().then(function () {
          expect(centre.locations).toBeArrayOfSize(serverReply.length);
          expect(centre.locations[0]).toEqual(jasmine.any(Location));
          centre.locations[0].compareToServerEntity(serverReply[0]);
          done();
        });
        httpBackend.flush();
      });

      it('can add a location', function(done) {
        var serverCentre = fakeEntities.centre();
        var centre = new Centre(serverCentre);
        var locationCount = centre.locations.length;
        var location = fakeEntities.location(centre);
        var command = _.pick(location,
                        'name', 'street', 'city', 'province', 'postalCode', 'poBoxNumber', 'countryIsoCode');
        var serverReply = { locationId: location.id };

        _.extend(command, { centreId: centre.id });
        _.extend(serverReply, command);

        httpBackend.expectPOST(uri(centre.id) + '/locations', command)
          .respond(201, serverReply);

        centre.addLocation(location).then(function () {
          var lastIndex = centre.locations.length - 1;
          expect(centre.locations).toBeArrayOfSize(locationCount + 1);
          expect(centre.locations[lastIndex]).toEqual(jasmine.any(Location));
          centre.locations[lastIndex].compareToServerEntity(location);
          done();
        });
        httpBackend.flush();
      });

      it('fail adding a duplicate location', function() {
        var centre = new Centre(fakeEntities.centre());
        var location = new Location(fakeEntities.location(centre));

        centre.locations.push(location);

        expect(function () {
          centre.addLocation(location);
        }).toThrow(new Error('location already present: ' + location.id));
      });

      it('can remove a location', function(done) {
        var serverCentre = fakeEntities.centre();
        var centre = new Centre(serverCentre);
        var location = new Location(fakeEntities.location(centre));
        var serverReply, locationCount;

        centre.locations.push(location);
        serverReply = { centreId: centre.id, locationId: location.id };
        locationCount = centre.locations.length;

        httpBackend.expectDELETE(uri(centre.id) + '/locations/' + location.id)
          .respond(201, serverReply);

        centre.removeLocation(location).then(function () {
          var lastIndex = centre.locations.length - 1;
          expect(centre.locations).toBeArrayOfSize(locationCount - 1);
          expect(centre.locations).not.toContain(location);
          done();
        });
        httpBackend.flush();
      });

      it('fail removing a location that does not exist', function() {
        var centre = new Centre(fakeEntities.centre());
        var location = new Location(fakeEntities.location(centre));

        expect(function () {
          centre.removeLocation(location);
        }).toThrow(new Error('location not present: ' + location.id));
      });

    });

    describe('studies', function () {

      it('can retrieve studies linked to a centre', function(done) {
        var study = fakeEntities.study();
        var centre = new Centre(fakeEntities.centre());
        var serverReply = [study.id];

        httpBackend.whenGET(uri(centre.id) + '/studies').respond({
          status: 'success',
          data: serverReply
        });

        centre.getStudyIds().then(function () {
          expect(centre.studyIds).toBeArrayOfSize(serverReply.length);
          expect(centre.studyIds[0]).toBe(study.id);
          done();
        });
        httpBackend.flush();
      });

      it('can add a study', function(done) {
        var study = fakeEntities.study();
        var centre = new Centre(fakeEntities.centre());
        var command = { centreId: centre.id, studyId: study.id };
        var serverReply = _.clone(command);
        var studyCount = centre.studyIds.length;

        httpBackend.expectPOST(uri(centre.id) + '/studies/' + study.id, command)
          .respond(201, serverReply);

        centre.addStudy(study).then(function () {
          var lastIndex = centre.studyIds.length - 1;
          expect(centre.studyIds).toBeArrayOfSize(studyCount + 1);
          expect(centre.studyIds[lastIndex]).toBe(study.id);
          done();
        });
        httpBackend.flush();
      });

      it('should not add a study that already exists', function() {
        var study = fakeEntities.study();
        var centre = new Centre(fakeEntities.centre());

        centre.studyIds.push(study.id);

        expect(function () {
          centre.addStudy(study);
        }).toThrow(new Error('study ID already present: ' + study.id));
      });

      it('can remove a study', function(done) {
        var study = fakeEntities.study();
        var centre = new Centre(fakeEntities.centre());
        var serverReply = { centreId: centre.id, studyId: study.id };
        var studyCount;

        centre.studyIds.push(study.id);
        studyCount = centre.studyIds.length;

        httpBackend.expectDELETE(uri(centre.id) + '/studies/' + study.id)
          .respond(201, serverReply);

        centre.removeStudy(study).then(function () {
          expect(centre.studyIds).toBeArrayOfSize(studyCount - 1);
          expect(centre.studyIds).not.toContain(study.id);
          done();
        });
        httpBackend.flush();
      });

      it('should not remove a study that does not exist', function() {
        var study = fakeEntities.study();
        var centre = new Centre(fakeEntities.centre());

        expect(function () {
          centre.removeStudy(study);
        }).toThrow(new Error('study ID not present: ' + study.id));
      });

    });
  });

});
