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

    it('can retrieve centres', function(done) {
      var centres = [fakeEntities.centre()];
      var serverReply = fakeEntities.pagedResult(centres);
      httpBackend.whenGET(uri()).respond({
        status: 'success',
        data: serverReply
      });

      Centre.list().then(function (pagedResult) {
        expect(pagedResult.items).toBeArrayOfSize(centres.length);
        expect(pagedResult.items[0]).toEqual(jasmine.any(Centre));
        pagedResult.items[0].compareToServerEntity(centres[0]);
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

    it('can add a centre', function(done) {
      var centre = new Centre(_.omit(fakeEntities.centre(), 'id'));
      var serverReply = {
        status: 'success',
        data: {
          id: 'abc',
          name: centre.name,
          description: centre.description
        }
      };
      httpBackend.expectPOST(uri(), {name: centre.name, description: centre.description})
        .respond(201, serverReply);

      centre.addOrUpdate(centre).then(function(replyCentre) {
        expect(replyCentre.id).toEqual(serverReply.data.id);
        expect(replyCentre.version).toEqual(0);
        expect(replyCentre.name).toEqual(centre.name);
        expect(replyCentre.description).toEqual(centre.description);
        done();
      });
      httpBackend.flush();
    });

    it('can update a centre', function(done) {
      var centre = new Centre(fakeEntities.centre());
      var command = {
        id:              centre.id,
        expectedVersion: centre.version,
        name:            centre.name,
        description:     centre.description
      };
      var expectedResult = {
        status: 'success',
        data: _.omit(command, 'expectedVersion')
      };
      expectedResult.data.version = centre.version;

      httpBackend.expectPUT(uri(centre.id), command)
        .respond(201, expectedResult);

      centre.addOrUpdate(centre).then(function(replyCentre) {
        expect(replyCentre.id).toEqual(expectedResult.data.id);
        expect(replyCentre.version).toEqual(centre.version);
        expect(replyCentre.name).toEqual(centre.name);
        expect(replyCentre.description).toEqual(centre.description);
        done();
      });
      httpBackend.flush();
    });

    it('can disable a centre', function() {
      var CentreStatus = this.$injector.get('CentreStatus');
      changeStatusShared('disable', CentreStatus.DISABLED());
    });

    it('can enable a centre', function() {
      var CentreStatus = this.$injector.get('CentreStatus');
      changeStatusShared('enable', CentreStatus.ENABLED());
    });

    function changeStatusShared(action, status) {
      var centre = new Centre(fakeEntities.centre());
      var changeStatusFn = action === 'disable' ? centre.disable : centre.enable;
      var command = { id: centre.id, expectedVersion: centre.version};
      var serverReply = {
        status: 'success',
        data: {
          id: centre.id,
          version: centre.version
        }
      };
      httpBackend.expectPOST(uri(centre.id) + '/' + action, command).respond(201, serverReply);
      _.bind(changeStatusFn, centre)().then(function(replyCentre) {
        expect(replyCentre.status).toBe(status);
      });
      httpBackend.flush();
    };


    describe('locations', function () {

      it('cannot get locations with new centre', function() {
        var centre = new Centre();
        expect(function () {
          centre.getLocations();
        }).toThrow(new Error('id is null'));
      });

      it('cannot get a location on a new centre', function() {
        var centre = new Centre();
        var location = new Location(fakeEntities.location(centre));
        expect(function () {
          centre.getLocation(location.id);
        }).toThrow(new Error('id is null'));
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

      it('can retrieve a single location', function(done) {
        var serverCentre = fakeEntities.centre();
        var centre = new Centre(serverCentre);
        var location = fakeEntities.location(serverCentre);

        httpBackend.whenGET(uri(centre.id) + '/locations?locationId=' + location.id).respond({
          status: 'success',
          data: location
        });

        centre.getLocation(location.id).then(function (replyCentre) {
          expect(centre.locations).toBeArrayOfSize(1);
          centre.locations[0].compareToServerEntity(location);
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

      it('cannot get study IDs from a new centre', function() {
        var centre = new Centre();
        expect(function () {
          centre.getStudyIds();
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
