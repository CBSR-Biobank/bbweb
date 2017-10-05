/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
/* global angular */

import _ from 'lodash';

describe('Specimen', function() {

  beforeEach(() => {
    angular.mock.module('biobankApp', 'biobank.test');
    angular.mock.inject(function(EntityTestSuite,
                                 ServerReplyMixin,
                                 AnnotationsEntityTestSuiteMixin,
                                 testUtils,
                                 testDomainEntities) {
      _.extend(this,
               EntityTestSuite.prototype,
               ServerReplyMixin.prototype,
               AnnotationsEntityTestSuiteMixin.prototype);

      this.injectDependencies('$httpBackend',
                              '$rootScope',
                              'Participant',
                              'CollectionEventType',
                              'CollectionEvent',
                              'Specimen',
                              'factory',
                              'testUtils');

      testUtils.addCustomMatchers();
      testDomainEntities.extend();

      this.jsonSpecimen = this.factory.specimen();

      // used by promise tests
      this.expectSpecimen = (entity) => {
        expect(entity).toEqual(jasmine.any(this.CollectionEvent));
      };

      // used by promise tests
      this.failTest = (error) => {
        expect(error).toBeUndefined();
      };

      this.url = url;

      //---

      function url() {
        const args = [ 'participants/cevents/spcs' ].concat(_.toArray(arguments));
        return EntityTestSuite.prototype.url.apply(null, args);
      }
    });
  });

  afterEach(function() {
    this.$httpBackend.verifyNoOutstandingExpectation();
    this.$httpBackend.verifyNoOutstandingRequest();
  });

  it('constructor with default parameters has default values', function() {
    var specimen = new this.Specimen();

    expect(specimen.id).toBeNull();
    expect(specimen.version).toBe(0);
    expect(specimen.originLocationInfo).toBeNull();
    expect(specimen.locationInfo).toBeNull();
    expect(specimen.containerId).toBeUndefined();
    expect(specimen.positionId).toBeUndefined();
    expect(specimen.timeAdded).toBeNull();
    expect(specimen.timeModified).toBeNull();
    expect(specimen.timeCreated).toBeNull();
    expect(specimen.amount).toBeNull();
    expect(specimen.state).toBeNull();
  });

  it('constructor with specimen spec has valid values', function() {
    var specimenDescription = this.factory.collectionSpecimenDescription(),
        specimen = new this.Specimen({}, specimenDescription);
    expect(specimen.specimenDescriptionId).toBe(specimenDescription.id);
  });

  it('fails when creating from an object with invalid keys', function() {
    var serverObj = { tmp: 1 };
    expect(() => {
      this.Specimen.create(serverObj);
    }).toThrowError(/invalid object from server/);
  });

  it('has valid values when creating from a server response', function() {
    var jsonSpecimen = this.factory.specimen();

    // TODO: add annotations to the server response
    var specimen = this.Specimen.create(jsonSpecimen);
    specimen.compareToJsonEntity(jsonSpecimen);
  });

  it('fails when creating async from an object with invalid keys', function() {
    var serverObj = { tmp: 1 },
        catchTriggered = false;
    this.Specimen.asyncCreate(serverObj)
      .catch(function (err) {
        expect(err.message).toContain('invalid object from server');
        catchTriggered = true;
      });
    this.$rootScope.$digest();
    expect(catchTriggered).toBeTrue();
  });

  describe('getting specimen by ID', function() {

    it('can retrieve a single sepcimen by ID', function() {
      var jsonSpecimen = this.factory.specimen();

      this.$httpBackend.whenGET(this.url(jsonSpecimen.id)).respond(this.reply(jsonSpecimen));

      this.Specimen.get(jsonSpecimen.id).then((reply) => {
        expect(reply).toEqual(jasmine.any(this.Specimen));
        reply.compareToJsonEntity(jsonSpecimen);
      });
      this.$httpBackend.flush();
    });

    it('throws an error if id parameter is falsy', function() {
      expect(() => {
        this.Specimen.get(undefined);
      }).toThrowError(/specimen id not specified/);
    });

  });

  describe('getting specimen by inventory ID', function() {

    it('can retrieve a single sepcimen by inventory ID', function() {
      var jsonSpecimen = this.factory.specimen();

      this.$httpBackend.whenGET(this.url() + '/invid/' + jsonSpecimen.inventoryId)
        .respond(this.reply(jsonSpecimen));

      this.Specimen.getByInventoryId(jsonSpecimen.inventoryId).then((reply) => {
        expect(reply).toEqual(jasmine.any(this.Specimen));
        reply.compareToJsonEntity(jsonSpecimen);
      });
      this.$httpBackend.flush();
    });

    it('throws an error if id parameter is falsy', function() {
      expect(() => {
        this.Specimen.getByInventoryId(undefined);
      }).toThrowError(/specimen inventory id not specified/);
    });

  });

  describe('when listing specimens', function() {

    it('can list specimens for a collection event', function() {
      var specimenDescription,
          cevent,
          specimens,
          reply;

      this.factory.centre({ locations: [ this.factory.location() ]});

      specimenDescription = this.factory.collectionSpecimenDescription();
      this.factory.collectionEventType({ specimenDescriptions: [ specimenDescription ]});

      cevent    = this.factory.collectionEvent();
      specimens = _.range(3).map(() => this.factory.specimen());
      reply     = this.factory.pagedResult(specimens);

      this.$httpBackend.whenGET(this.url(cevent.id)).respond(this.reply(reply));

      this.Specimen.list(cevent.id).then((pagedResult) => {
        expect(pagedResult.items).toBeArrayOfSize(specimens.length);
        pagedResult.items.forEach((specimen) => {
          expect(specimen).toEqual(jasmine.any(this.Specimen));
        });
      });
      this.$httpBackend.flush();
    });

    it('can list specimens using sorting', function() {
      var cevent = this.factory.collectionEvent(),
          reply = this.factory.pagedResult([]),
          sortingTypes = [ 'id', '-timeCreated', 'state' ];

      sortingTypes.forEach((sortingType) => {
        this.$httpBackend.whenGET(this.url(cevent.id) + '?sort=' + sortingType)
          .respond(this.reply(reply));

        this.Specimen.list(cevent.id, { sort: sortingType }).then((pagedResult) => {
          expect(pagedResult.items).toBeEmptyArray();
        });
        this.$httpBackend.flush();
      });
    });

    it('can list specimens using page number', function() {
      var cevent = this.factory.collectionEvent(),
          reply = this.factory.pagedResult([]),
          page = 2;

      this.$httpBackend.whenGET(this.url(cevent.id) + '?page=' + page)
        .respond(this.reply(reply));

      this.Specimen.list(cevent.id, { page: page }).then((pagedResult) => {
        expect(pagedResult.items).toBeEmptyArray();
      });
      this.$httpBackend.flush();
    });

    it('can list specimens using page size number', function() {
      var cevent = this.factory.collectionEvent(),
          reply = this.factory.pagedResult([]),
          limit = 2;

      this.$httpBackend.whenGET(this.url(cevent.id) + '?limit=' + limit)
        .respond(this.reply(reply));

      this.Specimen.list(cevent.id, { limit: limit }).then((pagedResult) => {
        expect(pagedResult.items).toBeEmptyArray();
      });
      this.$httpBackend.flush();
    });

    it('returns rejected promise if specimens have invalid format', function() {
      var cevent = this.factory.collectionEvent(),
          reply = this.factory.pagedResult([{ tmp: 1 }]),
          catchTriggered = false;

      this.$httpBackend.whenGET(this.url(cevent.id)).respond(this.reply(reply));

      this.Specimen.list(cevent.id)
        .catch(function (err) {
          expect(err.indexOf('invalid specimens from server')).not.toBeNull();
          catchTriggered = true;
        });
      this.$httpBackend.flush();
      expect(catchTriggered).toBeTrue();
    });

  });

  it('can add specimens', function() {
    var specimenDescription,
        cevent,
        jsonCevent,
        jsonSpecimens,
        specimens,
        json;

    this.factory.collectionEventType({ specimenDescriptions: [ specimenDescription ]});
    specimenDescription  = this.factory.collectionSpecimenDescription();
    jsonCevent    = this.factory.collectionEvent();
    cevent        = new this.CollectionEvent(jsonCevent);
    jsonSpecimens = _.range(3).map(() => this.factory.specimen());
    specimens     = jsonSpecimens.map((json) => this.Specimen.create(json));
    json          = addJson(cevent, jsonSpecimens);

    this.$httpBackend.expectPOST(this.url(cevent.id), json).respond(this.reply(jsonCevent));

    this.Specimen.add(cevent.id, specimens);
    this.$httpBackend.flush();
  });

  it('can assign the specimen spec', function() {
    var specimen = new this.Specimen(this.factory.specimen()),
        specimenDescription  = this.factory.collectionSpecimenDescription();
    expect(specimen.specimenDescriptionId).not.toBe(specimenDescription.id);
    expect(specimen.specimenDescription).not.toEqual(specimenDescription);
    specimen.setSpecimenDescription(specimenDescription);
    expect(specimen.specimenDescriptionId).toBe(specimenDescription.id);
    expect(specimen.specimenDescription).toEqual(specimenDescription);
  });

  describe('for name', function() {

    it('can get the specimen specs name', function() {
      var specimenDescription  = this.factory.collectionSpecimenDescription(),
          specimen = new this.Specimen(this.factory.specimen());
      specimen.setSpecimenDescription(specimenDescription);
      expect(specimen.name()).toBe(specimenDescription.name);
    });

    it('throws a domain error if specimen spec not assigned', function() {
      var specimen = new this.Specimen(this.factory.specimen());
      expect(() => {
        specimen.name();
      }).toThrowError(/specimen spec not assigned/);
    });
  });

  describe('for name', function() {

    it('can get the specimen specs default amount', function() {
      var specimenDescription  = this.factory.collectionSpecimenDescription(),
          specimen = new this.Specimen(this.factory.specimen());
      specimen.setSpecimenDescription(specimenDescription);
      expect(specimen.defaultAmount()).toBe(specimenDescription.amount);
    });

    it('throws a domain error if specimen spec not assigned', function() {
      var specimen = new this.Specimen(this.factory.specimen());
      expect(() => {
        specimen.defaultAmount();
      }).toThrowError(/specimen spec not assigned/);
    });
  });

  it('should be able to remove a specimen', function() {
    var cevent   = new this.CollectionEvent(this.factory.collectionEvent()),
        specimen = new this.Specimen(this.factory.specimen()),
        url      = this.url(cevent.id, specimen.id, specimen.version);

    specimen.collectionEventId = cevent.id;
    this.$httpBackend.expectDELETE(url).respond(this.reply(true));
    specimen.remove(cevent.id);
    this.$httpBackend.flush();
  });

  function addJson(collectionEvent, specimens) {
    var json = { collectionEventId: collectionEvent.id };
    json.specimenData = specimens.map((specimen) => {
      var result = _.pick(specimen, 'inventoryId', 'specimenDescriptionId', 'timeCreated', 'amount');
      result.locationId = specimen.locationInfo.locationId;
      return result;
    });
    return json;
  }

});