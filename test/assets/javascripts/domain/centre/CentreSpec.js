/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
/* global define */
define([
  'angular',
  'angularMocks',
  'underscore',
  'biobank.testUtils',
  'biobankApp'
], function(angular, mocks, _, testUtils) {
  'use strict';

  /**
   * For now these tests test the interaction between the class and the server.
   *
   * At the moment not sure if we need the service layer, or if the domain model objects call the rest API
   * directly. If the service layer is kept then these tests will have to be modified and only mock the
   * service methods in 'centresService'.
   */
  describe('Centre', function() {

    var httpBackend, Centre, CentreStatus, Location, funutils, fakeEntities;

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function($httpBackend,
                               _Centre_,
                               _CentreStatus_,
                               _Location_,
                               _funutils_,
                               fakeDomainEntities,
                               extendedDomainEntities) {
      httpBackend  = $httpBackend;
      Centre       = _Centre_;
      CentreStatus = _CentreStatus_;
      Location     = _Location_;
      funutils     = _funutils_;
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
      expect(centre.status).toBe(CentreStatus.DISABLED());
    });

    it('can retrieve centres', function(done) {
      var centres = [fakeEntities.centre()];
      var reply = fakeEntities.pagedResult(centres);
      httpBackend.whenGET(uri()).respond(serverReply(reply));

      Centre.list().then(function (pagedResult) {
        expect(pagedResult.items).toBeArrayOfSize(centres.length);
        _.each(pagedResult.items, function (item){
          expect(item).toEqual(jasmine.any(Centre));
          item.compareToServerEntity(centres[0]);
        });
        done();
      });
      httpBackend.flush();
    });

    it('can retrieve a single centre', function(done) {
      var centre = fakeEntities.centre();
      httpBackend.whenGET(uri(centre.id)).respond(serverReply(centre));

      Centre.get(centre.id).then(function (reply) {
        expect(reply).toEqual(jasmine.any(Centre));
        reply.compareToServerEntity(centre);
        done();
      });
      httpBackend.flush();
    });

    it('can add a centre', function(done) {
      var baseCentre = fakeEntities.centre();
      var centre = new Centre(_.omit(baseCentre, 'id'));
      var cmd = addCommand(centre);

      httpBackend.expectPOST(uri(), cmd).respond(201, serverReply(baseCentre));

      centre.addOrUpdate().then(function(replyCentre) {
        expect(replyCentre.id).toEqual(baseCentre.id);
        expect(replyCentre.version).toEqual(0);
        expect(replyCentre.name).toEqual(centre.name);
        expect(replyCentre.description).toEqual(centre.description);
        done();
      });
      httpBackend.flush();
    });

    it('can update a centre', function(done) {
      var baseCentre = fakeEntities.centre();
      var centre = new Centre(baseCentre);
      var command = updateCommand(centre);
      var reply = replyCentre(baseCentre);

      httpBackend.expectPUT(uri(centre.id), command).respond(201, serverReply(reply));

      centre.addOrUpdate().then(function(replyCentre) {
        expect(replyCentre.id).toEqual(centre.id);
        expect(replyCentre.version).toEqual(centre.version + 1);
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

    function addCommand(centre) {
      return  _.pick(centre, 'name', 'description');
    }

    function updateCommand(centre) {
      return _.extend(_.pick(centre, 'id', 'name', 'description'),
                      testUtils.expectedVersion(centre.version));
    }

    function changeStatusCommand(centre) {
      return _.extend(_.pick(centre, 'id'),
                      testUtils.expectedVersion(centre.version));
    }

    function replyCentre(centre, newValues) {
      newValues = newValues || {};
      return new Centre(_.extend({}, centre, newValues, {version: centre.version + 1}));
    }

    function serverReply(event) {
      return { status: 'success', data: event };
    }

    function changeStatusShared(action, status) {
      var baseCentre = fakeEntities.centre();
      var centre = new Centre(baseCentre);
      var command = changeStatusCommand(centre);
      var reply = replyCentre(baseCentre, { status: status });

      httpBackend.expectPOST(uri(centre.id) + '/' + action, command).respond(201, serverReply(reply));

      centre[action]().then(function(replyCentre) {
        expect(replyCentre.id).toEqual(centre.id);
        expect(replyCentre.version).toEqual(centre.version + 1);
        expect(replyCentre.status).toBe(status);
      });
      httpBackend.flush();
    }

    function uri(centreId) {
      var result = '/centres';
      if (arguments.length > 0) {
        result += '/' + centreId;
      }
      return result;
    }

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
        var location = fakeEntities.location(serverCentre);
        var locations = [ location ];

        httpBackend.whenGET(uri(centre.id) + '/locations').respond(serverReply(locations));

        centre.getLocations().then(function () {
          expect(centre.locations).toBeArrayOfSize(locations.length);
          expect(centre.locations[0]).toEqual(jasmine.any(Location));
          centre.locations[0].compareToServerEntity(locations[0]);
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
        var centre = new Centre(fakeEntities.centre());
        var location = fakeEntities.location(centre);
        var command = addLocationCommand(centre, location);
        var event = locationAddedEvent(location);

        var locationCount = centre.locations.length;

        httpBackend.expectPOST(uri(centre.id) + '/locations', command)
          .respond(201, serverReply(event));

        centre.addLocation(location).then(function () {
          var lastIndex = centre.locations.length - 1;
          expect(centre.locations).toBeArrayOfSize(locationCount + 1);
          expect(centre.locations[lastIndex]).toEqual(jasmine.any(Location));
          centre.locations[lastIndex].compareToServerEntity(location);
          done();
        });
        httpBackend.flush();
      });

      it('adding a location with existing ID removes previous one', function(done) {
        var centre = new Centre(fakeEntities.centre());
        var location = new Location(fakeEntities.location(centre));
        var command = _.pick(location,
                             'name', 'street', 'city', 'province', 'postalCode', 'poBoxNumber', 'countryIsoCode');
        var serverReply = { locationId: location.id };

        _.extend(command, { centreId: centre.id });
        _.extend(serverReply, command);

        centre.locations.push(location);

        httpBackend.expectDELETE(uri(centre.id) + '/locations/' + location.id)
          .respond(201, serverReply);

        httpBackend.expectPOST(uri(centre.id) + '/locations', command)
          .respond(201, location);

        centre.addLocation(location).then(function () {
          done();
        });
        httpBackend.flush();
      });

      it('can remove a location', function(done) {
        var serverCentre = fakeEntities.centre();
        var centre = new Centre(serverCentre);
        var location = new Location(fakeEntities.location(centre));
        var event = locationRemovedEvent(centre, location);
        var locationCount;

        centre.locations.push(location);
        locationCount = centre.locations.length;

        httpBackend.expectDELETE(uri(centre.id) + '/locations/' + location.id)
          .respond(201, serverReply(event));

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

      function addLocationCommand(centre, location) {
        return _.extend({ centreId: centre.id },
                        _.pick(location,
                               'name', 'street', 'city', 'province', 'postalCode',
                               'poBoxNumber', 'countryIsoCode'));
      }

      function locationAddedEvent(location) {
        return _.extend(addLocationCommand(location), { id: testUtils.uuid() });
      }

      function locationRemovedEvent(centre, location) {
        return { centreId: centre.id, locationId: location.id };
      }

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
        var command = addStudyCommand(centre, study);
        var event =studyAddedToCentreEvent(centre, study);
        var studyCount = centre.studyIds.length;

        httpBackend.expectPOST(uri(centre.id) + '/studies/' + study.id, command)
          .respond(201, serverReply(event));

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
        var event = studyRemovedFromCentreEvent(centre, study);
        var studyCount;

        centre.studyIds.push(study.id);
        studyCount = centre.studyIds.length;

        httpBackend.expectDELETE(uri(centre.id) + '/studies/' + study.id)
          .respond(201, serverReply(event));

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

      function addStudyCommand(centre, study) {
        return { centreId: centre.id, studyId: study.id };
      }

      function studyAddedToCentreEvent(centre, study) {
        return { centreId: centre.id, studyId: study.id };
      }

      function studyRemovedFromCentreEvent(centre, study) {
        return { centreId: centre.id, studyId: study.id };
      }

    });
  });

});

/* Local Variables:  */
/* mode: js          */
/* End:              */
