/*
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */
/* global angular */

import _ from 'lodash';
import ngModule from '../../index'

describe('Centre', function() {

  beforeEach(() => {
    angular.mock.module(ngModule, 'biobank.test');
    angular.mock.inject(function (ServerReplyMixin, EntityTestSuiteMixin) {
      _.extend(this, EntityTestSuiteMixin, ServerReplyMixin);

      this.injectDependencies('$httpBackend',
                              '$httpParamSerializer',
                              'Centre',
                              'CentreState',
                              'Location',
                              'TestUtils',
                              'Factory');

      // used by promise tests
      this.expectCentre = (entity) => {
        expect(entity).toEqual(jasmine.any(this.Centre));
      };

      this.changeStatusShared = (jsonCentre, action, state) => {
        /* jshint validthis:true */
        var centre     = new this.Centre(jsonCentre),
            json       = { expectedVersion: centre.version },
            reply      = replyCentre(jsonCentre, { state: state }),
            checkCentre = (reply) => {
              expect(reply).toEqual(jasmine.any(this.Centre));
              expect(reply.id).toEqual(centre.id);
              expect(reply.version).toEqual(centre.version + 1);
              expect(reply.state).toBe(state);
            };

        this.$httpBackend.expectPOST(this.url(action, centre.id), json).respond(this.reply(reply));
        expect(centre[action]).toBeFunction();
        centre[action]().then(checkCentre).catch(failTest);
        this.$httpBackend.flush();
      };

      this.url = url;

      //---

      function url() {
        const args = [ 'centres' ].concat(_.toArray(arguments));
        return EntityTestSuiteMixin.url.apply(null, args);
      }
    });
  });

  afterEach(function() {
    this.$httpBackend.verifyNoOutstandingExpectation();
    this.$httpBackend.verifyNoOutstandingRequest();
  });

  it('constructor with no parameters has default values', function() {
    var centre = new this.Centre();

    expect(centre.id).toBeNull();
    expect(centre.version).toBe(0);
    expect(centre.timeAdded).toBeUndefined();
    expect(centre.timeModified).toBeUndefined();
    expect(centre.name).toBeEmptyString();
    expect(centre.description).toBeNull();
    expect(centre.locations).toBeEmptyArray();
    expect(centre.studyNames).toBeEmptyArray();
    expect(centre.state).toBe(this.CentreState.DISABLED);
  });

  it('fails when creating from a non object', function() {
    var badCentreJson = _.omit(this.Factory.centre(), 'name');

    expect(() => { this.Centre.create(badCentreJson); })
      .toThrowError(/invalid object from server/);
  });

  it('can be created with a location', function() {
    var location = this.Factory.location(),
        rawCentre = this.Factory.centre({ locations: [ location ]}),
        centre = this.Centre.create(rawCentre);

    expect(centre.locations).toBeArrayOfSize(1);
  });

  it('fails when creating from a bad study ID', function() {
    var badCentreJson = this.Factory.centre({ studyNames: [ null, '' ] });

    expect(() => {
      this.Centre.create(badCentreJson);
    }).toThrowError(/Invalid type.*expected object/);
  });

  it('fails when creating from a bad location', function() {
    var badCentreJson = this.Factory.centre({ locations: [ 1 ] });

    expect(() => {
      this.Centre.create(badCentreJson);
    }).toThrowError(/invalid object from server/);
  });

  it('state predicates return valid results', function() {
    _.values(this.CentreState).forEach((state) => {
      var centre = new this.Centre(this.Factory.centre({ state: state }));
      expect(centre.isDisabled()).toBe(state === this.CentreState.DISABLED);
      expect(centre.isEnabled()).toBe(state === this.CentreState.ENABLED);
    });
  });

  it('can retrieve a single centre', function() {
    var centre = this.Factory.centre(),
        checkReply= (reply) => {
          expect(reply).toEqual(jasmine.any(this.Centre));
        };

    this.$httpBackend.whenGET(this.url(centre.slug)).respond(this.reply(centre));
    this.Centre.get(centre.slug).then(checkReply).catch(failTest);
    this.$httpBackend.flush();
  });

  it('fails when getting a centre and it has a bad format', function() {
    var centre = _.omit(this.Factory.centre(), 'name');
    this.$httpBackend.whenGET(this.url(centre.slug)).respond(this.reply(centre));
    this.Centre.get(centre.slug).then(shouldNotFail).catch(shouldFail);
    this.$httpBackend.flush();

    function shouldFail(error) {
      expect(error.message).toContain('Missing required property');
    }
  });

  it('fails when getting a centre and it has a bad study ID', function() {
    var centre = this.Factory.centre({ studyNames: [ '' ]});

    this.$httpBackend.whenGET(this.url(centre.slug)).respond(this.reply(centre));
    this.Centre.get(centre.slug).then(shouldNotFail).catch(shouldFail);
    this.$httpBackend.flush();

    function shouldFail(error) {
      expect(error.message).toMatch(/Invalid type/);
    }
  });

  it('fails when getting a centre and it has a bad location', function() {
    var location = _.omit(this.Factory.location(), 'name'),
        centre = this.Factory.centre({ locations: [ location ]});

    this.$httpBackend.whenGET(this.url(centre.slug)).respond(this.reply(centre));
    this.Centre.get(centre.slug).then(shouldNotFail).catch(shouldFail);
    this.$httpBackend.flush();

    function shouldFail(error) {
      expect(error.message).toContain('Missing required property');
    }
  });

  it('can retrieve centres', function() {
    var centres = [ this.Factory.centre() ],
        reply = this.Factory.pagedResult(centres),
        checkReply = (pagedResult) => {
          expect(pagedResult.items).toBeArrayOfSize(centres.length);
          pagedResult.items.forEach((item) => {
            expect(item).toEqual(jasmine.any(this.Centre));
          });
        };

    this.$httpBackend.whenGET(this.url('search')).respond(this.reply(reply));
    this.Centre.list().then(checkReply).catch(failTest);
    this.$httpBackend.flush();
  });

  it('can list centres using options', function() {
    var optionList = [
      { filter: 'name::test' },
      { sort: 'state' },
      { page: 2 },
      { limit: 10 }
    ],
        centres = [ this.Factory.centre() ],
        reply   = this.Factory.pagedResult(centres),
        testCentre = (pagedResult)  => {
          expect(pagedResult.items).toBeArrayOfSize(centres.length);
          pagedResult.items.forEach((centre) => {
            expect(centre).toEqual(jasmine.any(this.Centre));
          });
        };

    optionList.forEach((options) => {
      var url = this.url('search') + '?' + this.$httpParamSerializer(options);
      this.$httpBackend.whenGET(url).respond(this.reply(reply));
      this.Centre.list(options).then(testCentre).catch(failTest);
      this.$httpBackend.flush();
    });
  });

  it('fails when list returns an invalid centre', function() {
    var centres = [ _.omit(this.Factory.centre(), 'name') ],
        reply = this.Factory.pagedResult(centres);

    this.$httpBackend.whenGET(this.url('search')).respond(this.reply(reply));
    this.Centre.list().then(listFail).catch(shouldFail);
    this.$httpBackend.flush();

    function listFail() {
      fail('function should not be called');
    }

    function shouldFail(error) {
      expect(error).toStartWith('invalid centres from server');
    }
  });

 it('can add a centre', function() {
    var jsonCentre = this.Factory.centre(),
        centre = new this.Centre(_.omit(jsonCentre, 'id')),
        json = _.pick(centre, 'name', 'description'),
        checkReply = (replyCentre) => {
          expect(replyCentre).toEqual(jasmine.any(this.Centre));
        };

    this.$httpBackend.expectPOST(this.url(''), json).respond(this.reply(jsonCentre));
    centre.add().then(checkReply).catch(failTest);
    this.$httpBackend.flush();
  });

  it('can update the name on a centre', function() {
    var jsonCentre = this.Factory.centre(),
        centre     = new this.Centre(jsonCentre);

    this.updateEntity.call(this,
                           centre,
                           'updateName',
                           centre.name,
                           this.url('name', centre.id),
                           { name: centre.name },
                           jsonCentre,
                           this.expectCentre.bind(this),
                           failTest);
  });

  it('can update the description on a centre', function() {
    var jsonCentre = this.Factory.centre({ description: this.Factory.stringNext() }),
        centre     = new this.Centre(jsonCentre);

    this.updateEntity.call(this,
                           centre,
                           'updateDescription',
                           undefined,
                           this.url('description', centre.id),
                           { },
                           jsonCentre,
                           this.expectCentre.bind(this),
                           failTest);

    this.updateEntity.call(this,
                           centre,
                           'updateDescription',
                           centre.description,
                           this.url('description', centre.id),
                           { description: centre.description },
                           jsonCentre,
                           this.expectCentre.bind(this),
                           failTest);
  });

  it('can disable a centre', function() {
    var jsonCentre = this.Factory.centre({ state: this.CentreState.ENABLED });
    this.changeStatusShared(jsonCentre, 'disable', this.CentreState.DISABLED);
  });

  it('throws an error when disabling a centre and it is already disabled', function() {
    var centre = new this.Centre(this.Factory.centre({ state: this.CentreState.DISABLED }));
    expect(() => { centre.disable(); })
      .toThrowError('already disabled');
  });

  it('can enable a centre', function() {
    var jsonCentre = this.Factory.centre({ state: this.CentreState.DISABLED });
    this.changeStatusShared(jsonCentre, 'enable', this.CentreState.ENABLED);
  });

  it('throws an error when enabling a centre and it is already enabled', function() {
    var centre = new this.Centre(this.Factory.centre({ state: this.CentreState.ENABLED }));
    expect(() => { centre.enable(); })
      .toThrowError('already enabled');
  });

  describe('locations', function () {

    it('adds a location', function() {
      var jsonLocation = this.Factory.location(),
          jsonCentre   = this.Factory.centre(),
          centre       = new this.Centre(jsonCentre);

      this.updateEntity.call(this,
                             centre,
                             'addLocation',
                             _.omit(jsonLocation, 'id'),
                             this.url('locations', centre.id),
                             _.omit(jsonLocation, 'id'),
                             jsonCentre,
                             this.expectCentre.bind(this),
                             failTest);
    });

    it('updates a location', function() {
      var jsonLocation = this.Factory.location(),
          jsonCentre   = this.Factory.centre(),
          centre       = new this.Centre(jsonCentre);

      this.updateEntity.call(this,
                             centre,
                             'updateLocation',
                             jsonLocation,
                             this.url('locations', centre.id) + '/' + jsonLocation.id,
                             jsonLocation,
                             jsonCentre,
                             this.expectCentre.bind(this),
                             failTest);
    });

    it('throws an error when removing a location that does not exists', function() {
      var centre = new this.Centre();

      expect(() => { centre.removeLocation(new this.Location()); })
        .toThrowError(/location does not exist/);
    });

    it('can remove a location', function() {
      var jsonLocation = new this.Location(this.Factory.location()),
          jsonCentre   = this.Factory.centre({ locations: [ jsonLocation ]}),
          centre       = new this.Centre(jsonCentre),
          url          = this.url('locations', centre.id) + '/' + centre.version + '/' + jsonLocation.id,
          checkCentre = (reply) => {
            expect(reply).toEqual(jasmine.any(this.Centre));
          };

      this.$httpBackend.expectDELETE(url).respond(this.reply(jsonCentre));
      centre.removeLocation(jsonLocation).then(checkCentre).catch(failTest);
      this.$httpBackend.flush();
    });

  });

  describe('studies', function () {

    it('can add a study', function() {
      var jsonStudy  = this.Factory.study(),
          jsonCentre = this.Factory.centre(),
          centre     = new this.Centre(jsonCentre);

      this.updateEntity.call(this,
                             centre,
                             'addStudy',
                             jsonStudy,
                             this.url('studies', centre.id),
                             { studyId : jsonStudy.id },
                             jsonCentre,
                             this.expectCentre.bind(this),
                             failTest);
    });

    it('can remove a study', function() {
      var jsonStudy  = this.Factory.study(),
          jsonCentre = this.Factory.centre({ studyNames: [ this.Factory.studyNameDto(jsonStudy) ]}),
          centre     = new this.Centre(jsonCentre),
          url        = this.url('studies', centre.id) + '/' + centre.version + '/' + jsonStudy.id,
          checkCentre = (reply) => {
            expect(reply).toEqual(jasmine.any(this.Centre));
          };

      this.$httpBackend.expectDELETE(url).respond(this.reply(jsonCentre));
      centre.removeStudy(jsonStudy).then(checkCentre).catch(failTest);
      this.$httpBackend.flush();
    });

    it('should not remove a study that does not exist', function() {
      var study = this.Factory.study(),
          centre = new this.Centre(this.Factory.centre());

      expect(() => {
        centre.removeStudy(study);
      }).toThrow(new Error('study ID not present: ' + study.id));
    });

  });

  // used by promise tests
  function failTest(error) {
    expect(error).toBeUndefined();
  }

  function replyCentre(centre, newValues) {
    newValues = newValues || {};
    return _.extend({}, centre, newValues, {version: centre.version + 1});
  }

  function shouldNotFail() {
    fail('function should not be called');
  }

});
