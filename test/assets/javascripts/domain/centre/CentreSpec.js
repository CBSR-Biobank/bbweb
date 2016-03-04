/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define([
  'angular',
  'angularMocks',
  'underscore',
  'jquery',
  'sprintf',
  'biobankApp'
], function(angular, mocks, _, $, sprintf) {
  'use strict';

  /**
   * For now these tests test the interaction between the class and the server.
   *
   * At the moment not sure if we need the service layer, or if the domain model objects call the rest API
   * directly. If the service layer is kept then these tests will have to be modified and only mock the
   * service methods in 'centresService'.
   */
  describe('Centre', function() {

    var Centre;

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function (extendedDomainEntities) {
      this.httpBackend  = this.$injector.get('$httpBackend');
      this.Centre       = this.$injector.get('Centre');
      this.CentreStatus = this.$injector.get('CentreStatus');
      this.Location     = this.$injector.get('Location');
      this.funutils     = this.$injector.get('funutils');
      this.testUtils    = this.$injector.get('testUtils');
      this.jsonEntities = this.$injector.get('jsonEntities');

      Centre = this.Centre;
    }));

    afterEach(function() {
      this.httpBackend.verifyNoOutstandingExpectation();
      this.httpBackend.verifyNoOutstandingRequest();
    });

    it('constructor with no parameters has default values', function() {
      var centre = new this.Centre();

      expect(centre.id).toBeNull();
      expect(centre.version).toBe(0);
      expect(centre.timeAdded).toBeNull();
      expect(centre.timeModified).toBeNull();
      expect(centre.name).toBeEmptyString();
      expect(centre.description).toBeNull();
      expect(centre.locations).toBeEmptyArray();
      expect(centre.studyIds).toBeEmptyArray();
      expect(centre.status).toBe(this.CentreStatus.DISABLED());
    });

    it('fails when creating from a non object', function() {
      var self = this,
          badStudyJson = _.omit(self.jsonEntities.centre(), 'name');

      expect(function () { self.Centre.create(badStudyJson); }).toThrowErrorOfType('Error');
    });

    it('fails when creating from a bad study ID', function() {
      var self = this,
          badCentreJson = self.jsonEntities.centre({ studyIds: [ null, '' ] });

      expect(function () { self.Centre.create(badCentreJson); }).toThrowErrorOfType('Error');
    });

    it('fails when creating from a bad location', function() {
      var self = this,
          badCentreJson = self.jsonEntities.centre({ locations: [ 1 ] });

      expect(function () { self.Centre.create(badCentreJson); }).toThrowErrorOfType('Error');
    });

    it('status predicates return valid results', function() {
      var self = this;
      _.each(self.CentreStatus.values(), function(status) {
        var centre = new self.Centre(self.jsonEntities.centre({ status: status }));
        expect(centre.isDisabled()).toBe(status === self.CentreStatus.DISABLED());
        expect(centre.isEnabled()).toBe(status === self.CentreStatus.ENABLED());
      });
    });

    it('can retrieve a single centre', function() {
      var self = this, centre = self.jsonEntities.centre();

      self.httpBackend.whenGET(uri(centre.id)).respond(serverReply(centre));

      self.Centre.get(centre.id).then(checkReply).catch(failTest);
      self.httpBackend.flush();

      function checkReply(reply) {
        expect(reply).toEqual(jasmine.any(self.Centre));
        reply.compareToJsonEntity(centre);
      }
    });

    it('fails when getting a centre and it has a bad format', function() {
      var self = this,
          centre = _.omit(self.jsonEntities.centre(), 'name');
      self.httpBackend.whenGET(uri(centre.id)).respond(serverReply(centre));

      self.Centre.get(centre.id).then(shouldNotFail).catch(shouldFail);
      self.httpBackend.flush();

      function shouldNotFail(reply) {
        fail('function should not be called');
      }

      function shouldFail(error) {
        expect(error).toStartWith('invalid object from server');
      }
    });

    it('fails when getting a centre and it has a bad study ID', function() {
      var self = this,
          centre = self.jsonEntities.centre({ studyIds: [ '' ]});

      self.httpBackend.whenGET(uri(centre.id)).respond(serverReply(centre));

      self.Centre.get(centre.id).then(shouldNotFail).catch(shouldFail);
      self.httpBackend.flush();

      function shouldNotFail(reply) {
        fail('function should not be called');
      }

      function shouldFail(error) {
        expect(error).toStartWith('invalid study IDs from server');
      }
    });

    it('fails when getting a centre and it has a bad location', function() {
      var self = this,
          location = _.omit(self.jsonEntities.location(), 'name'),
          centre = self.jsonEntities.centre({ locations: [ location ]});

      self.httpBackend.whenGET(uri(centre.id)).respond(serverReply(centre));

      self.Centre.get(centre.id).then(shouldNotFail).catch(shouldFail);
      self.httpBackend.flush();

      function shouldNotFail(reply) {
        fail('function should not be called');
      }

      function shouldFail(error) {
        expect(error).toStartWith('invalid locations from server');
      }
    });

    it('can retrieve centres', function() {
      var self = this,
          centres = [ self.jsonEntities.centre() ],
          reply = self.jsonEntities.pagedResult(centres);

      self.httpBackend.whenGET(uri()).respond(serverReply(reply));

      self.Centre.list().then(checkReply).catch(failTest);
      self.httpBackend.flush();

      function checkReply(pagedResult) {
        expect(pagedResult.items).toBeArrayOfSize(centres.length);
        _.each(pagedResult.items, function (item) {
          expect(item).toEqual(jasmine.any(self.Centre));
          item.compareToJsonEntity(centres[0]);
        });
      }
    });

    it('can list centres using options', function() {
      var self = this,
          optionList = [
            { filter: 'name' },
            { status: 'DisabledCentre' },
            { sort: 'status' },
            { page: 2 },
            { pageSize: 10 },
            { order: 'desc' }
          ];

      _.each(optionList, function (options) {
        var centres = [ self.jsonEntities.centre() ],
            reply   = self.jsonEntities.pagedResult(centres),
            url     = sprintf.sprintf('%s?%s', uri(), $.param(options, true));

        self.httpBackend.whenGET(url).respond(serverReply(reply));

        self.Centre.list(options).then(testStudy).catch(failTest);
        self.httpBackend.flush();

        function testStudy(pagedResult) {
          expect(pagedResult.items).toBeArrayOfSize(centres.length);
          _.each(pagedResult.items, function (study) {
            expect(study).toEqual(jasmine.any(self.Centre));
          });
        }
      });
    });

    it('fails when list returns an invalid centre', function() {
      var self = this,
          centres = [ _.omit(self.jsonEntities.centre(), 'name') ],
          reply = self.jsonEntities.pagedResult(centres);

      self.httpBackend.whenGET(uri()).respond(serverReply(reply));

      self.Centre.list().then(listFail).catch(shouldFail);
      self.httpBackend.flush();

      function listFail(reply) {
        fail('function should not be called');
      }

      function shouldFail(error) {
        expect(error).toStartWith('invalid centres from server');
      }
    });

    it('can add a centre', function() {
      var self = this,
          baseCentre = self.jsonEntities.centre(),
          centre = new self.Centre(_.omit(baseCentre, 'id')),
          json = _.pick(centre, 'name', 'description');

      self.httpBackend.expectPOST(uri(), json).respond(201, serverReply(baseCentre));

      centre.add().then(checkReply).catch(failTest);
      self.httpBackend.flush();

      function checkReply(replyCentre) {
        expect(replyCentre.id).toEqual(baseCentre.id);
        expect(replyCentre.version).toEqual(0);
        expect(replyCentre.name).toEqual(centre.name);
        expect(replyCentre.description).toEqual(centre.description);
      }
    });

    it('can update the name on a centre', function() {
      var self       = this,
          baseCentre = self.jsonEntities.centre(),
          centre     = new self.Centre(baseCentre);

      this.testUtils.updateEntity.call(this,
                                       centre,
                                       'updateName',
                                       centre.name,
                                       uri('name', centre.id),
                                       { name: centre.name },
                                       baseCentre,
                                       expectCentre,
                                       failTest);
    });

    it('can update the description on a centre', function() {
      var self = this,
          baseCentre = self.jsonEntities.centre(),
          centre     = new self.Centre(baseCentre);

      this.testUtils.updateEntity.call(this,
                                       centre,
                                       'updateDescription',
                                       undefined,
                                       uri('description', centre.id),
                                       { },
                                       baseCentre,
                                       expectCentre,
                                       failTest);

      this.testUtils.updateEntity.call(this,
                                       centre,
                                       'updateDescription',
                                       centre.description,
                                       uri('description', centre.id),
                                       { description: centre.description },
                                       baseCentre,
                                       expectCentre,
                                       failTest);
    });

    it('can disable a centre', function() {
      var jsonCentre = this.jsonEntities.centre({ status: this.CentreStatus.ENABLED() });
      changeStatusShared.call(this, jsonCentre, 'disable', this.CentreStatus.DISABLED());
    });

    it('throws an error when disabling a centre and it is already disabled', function() {
      var centre = new this.Centre(this.jsonEntities.centre({ status: this.CentreStatus.DISABLED() }));
      expect(function () { centre.disable(); })
        .toThrowErrorOfType('Error');
    });

    it('can enable a centre', function() {
      var jsonCentre = this.jsonEntities.centre({ status: this.CentreStatus.DISABLED() });
      changeStatusShared.call(this, jsonCentre, 'enable', this.CentreStatus.ENABLED());
    });

    it('throws an error when enabling a centre and it is already enabled', function() {
      var centre = new this.Centre(this.jsonEntities.centre({ status: this.CentreStatus.ENABLED() }));
      expect(function () { centre.enable(); })
        .toThrowErrorOfType('Error');
    });

    describe('locations', function () {

      it('adds a location', function() {
        var self         = this,
            jsonLocation = this.jsonEntities.location(),
            jsonCentre   = this.jsonEntities.centre(),
            centre       = new self.Centre(jsonCentre);

        this.testUtils.updateEntity.call(this,
                                         centre,
                                         'addLocation',
                                         _.omit(jsonLocation, 'uniqueId'),
                                         uri('locations', centre.id),
                                         _.omit(jsonLocation, 'uniqueId'),
                                         jsonCentre,
                                         expectCentre,
                                         failTest);
      });

      it('throws an error when removing a location that does not exists', function() {
        var self = this,
            centre = new self.Centre();

        expect(function () { centre.removeLocation(new self.Location()); }).toThrowErrorOfType('Error');
      });

      it('can remove a location', function() {
        var self         = this,
            jsonLocation = new self.Location(self.jsonEntities.location()),
            jsonCentre   = self.jsonEntities.centre({ locations: [ jsonLocation ]}),
            centre       = new self.Centre(jsonCentre),
            url          = sprintf.sprintf('%s/%d/%s',
                                           uri('locations', centre.id),
                                           centre.version,
                                           jsonLocation.uniqueId);

        self.httpBackend.expectDELETE(url).respond(201, serverReply(true));
        centre.removeLocation(jsonLocation).then(checkCentre).catch(failTest);
        self.httpBackend.flush();

        function checkCentre(reply) {
          expect(reply).toEqual(jasmine.any(self.Centre));
          expect(reply.locations).toBeEmptyArray();
        }
      });

    });

    describe('studies', function () {

      it('can add a study', function() {
        var self       = this,
            jsonStudy  = self.jsonEntities.study(),
            jsonCentre = self.jsonEntities.centre(),
            centre     = new self.Centre(jsonCentre);

        this.testUtils.updateEntity.call(this,
                                         centre,
                                         'addStudy',
                                         jsonStudy,
                                         uri('studies', centre.id),
                                         { studyId : jsonStudy.id },
                                         jsonCentre,
                                         expectCentre,
                                         failTest);
      });

      it('can remove a study', function() {
        var self       = this,
            jsonStudy  = self.jsonEntities.study(),
            jsonCentre = self.jsonEntities.centre({ studyIds: [ jsonStudy.id ]}),
            centre     = new self.Centre(jsonCentre),
            url        = sprintf.sprintf('%s/%d/%s',
                                         uri('studies', centre.id),
                                         centre.version,
                                         jsonStudy.id);

        self.httpBackend.expectDELETE(url).respond(201, serverReply(true));
        centre.removeStudy(jsonStudy).then(checkCentre).catch(failTest);
        this.httpBackend.flush();

        function checkCentre(replyCentre) {
          expect(replyCentre).toEqual(jasmine.any(self.Centre));
          expect(replyCentre.studyIds).toBeEmptyArray();
        }
      });

      it('should not remove a study that does not exist', function() {
        var self = this,
            study = self.jsonEntities.study(),
            centre = new self.Centre(self.jsonEntities.centre());

        expect(function () {
          centre.removeStudy(study);
        }).toThrow(new Error('study ID not present: ' + study.id));
      });

    });

    // used by promise tests
    function expectCentre(entity) {
      expect(entity).toEqual(jasmine.any(Centre));
    }

    // used by promise tests
    function failTest(error) {
      expect(error).toBeUndefined();
    }

    function replyCentre(centre, newValues) {
      newValues = newValues || {};
      return _.extend({}, centre, newValues, {version: centre.version + 1});
    }

    function serverReply(event) {
      return { status: 'success', data: event };
    }

    function changeStatusShared(jsonCentre, action, status) {
      /* jshint validthis:true */
      var self       = this,
          centre     = new self.Centre(jsonCentre),
          json       = { expectedVersion: centre.version },
          reply      = replyCentre(jsonCentre, { status: status });

      self.httpBackend.expectPOST(uri(action, centre.id), json).respond(201, serverReply(reply));
      expect(centre[action]).toBeFunction();
      centre[action]().then(checkCentre).catch(failTest);
      this.httpBackend.flush();

      function checkCentre(reply) {
        expect(reply).toEqual(jasmine.any(self.Centre));
        expect(reply.id).toEqual(centre.id);
        expect(reply.version).toEqual(centre.version + 1);
        expect(reply.status).toBe(status);
      }
    }

    function uri(/* path, centreId */) {
      var args = _.toArray(arguments),
          centreId,
          path;

      var result = '/centres';

      if (args.length > 0) {
        path = args.shift();
        result += '/' + path;
      }

      if (args.length > 0) {
        centreId = args.shift();
        result += '/' + centreId;
      }

      return result;
    }

  });

});

/* Local Variables:  */
/* mode: js          */
/* End:              */
