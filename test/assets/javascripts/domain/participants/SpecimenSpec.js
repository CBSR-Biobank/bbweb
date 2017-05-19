/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(function (require) {
  'use strict';

  var mocks   = require('angularMocks'),
      _       = require('lodash'),
      sprintf = require('sprintf-js').sprintf;

  describe('Specimen', function() {

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function(EntityTestSuiteMixin,
                               ServerReplyMixin,
                               AnnotationsEntityTestSuiteMixin,
                               testUtils,
                               testDomainEntities) {
      var self = this;

      _.extend(self,
               EntityTestSuiteMixin.prototype,
               ServerReplyMixin.prototype,
               AnnotationsEntityTestSuiteMixin.prototype);

      self.injectDependencies('$httpBackend',
                              '$rootScope',
                              'Participant',
                              'CollectionEventType',
                              'CollectionEvent',
                              'Specimen',
                              'factory',
                              'testUtils');

      testUtils.addCustomMatchers();

      self.jsonSpecimen = self.factory.specimen();

      self.expectSpecimen = expectSpecimen;
      self.failTest = failTest;
      testDomainEntities.extend();

      //--

      // used by promise tests
      function expectSpecimen(entity) {
        expect(entity).toEqual(jasmine.any(self.CollectionEvent));
      }

      // used by promise tests
      function failTest(error) {
        expect(error).toBeUndefined();
      }
    }));

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
      var self = this,
          serverObj = { tmp: 1 };
      expect(function () {
        self.Specimen.create(serverObj);
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
        var self = this,
            jsonSpecimen = this.factory.specimen();

        this.$httpBackend.whenGET(uri(jsonSpecimen.id)).respond(this.reply(jsonSpecimen));

        self.Specimen.get(jsonSpecimen.id).then(function (reply) {
          expect(reply).toEqual(jasmine.any(self.Specimen));
          reply.compareToJsonEntity(jsonSpecimen);
        });
        self.$httpBackend.flush();
      });

      it('throws an error if id parameter is falsy', function() {
        var self = this;

        expect(function () {
          self.Specimen.get(undefined);
        }).toThrowError(/specimen id not specified/);
      });

    });

    describe('getting specimen by inventory ID', function() {

      it('can retrieve a single sepcimen by inventory ID', function() {
        var self = this,
            jsonSpecimen = this.factory.specimen();

        this.$httpBackend.whenGET(uri() + '/invid/' + jsonSpecimen.inventoryId)
          .respond(this.reply(jsonSpecimen));

        self.Specimen.getByInventoryId(jsonSpecimen.inventoryId).then(function (reply) {
          expect(reply).toEqual(jasmine.any(self.Specimen));
          reply.compareToJsonEntity(jsonSpecimen);
        });
        self.$httpBackend.flush();
      });

      it('throws an error if id parameter is falsy', function() {
        var self = this;

        expect(function () {
          self.Specimen.getByInventoryId(undefined);
        }).toThrowError(/specimen inventory id not specified/);
      });

    });

    describe('when listing specimens', function() {

      it('can list specimens for a collection event', function() {
        var self = this,
            specimenDescription,
            cevent,
            specimens,
            reply;

        self.factory.centre({ locations: [ self.factory.location() ]});

        specimenDescription = self.factory.collectionSpecimenDescription();
        self.factory.collectionEventType({ specimenDescriptions: [ specimenDescription ]});

        cevent       = self.factory.collectionEvent();
        specimens    = _.map(_.range(3), function () { return self.factory.specimen(); });
        reply        = self.factory.pagedResult(specimens);

        self.$httpBackend.whenGET(uri(cevent.id)).respond(this.reply(reply));

        self.Specimen.list(cevent.id).then(function (pagedResult) {
          expect(pagedResult.items).toBeArrayOfSize(specimens.length);
          _.each(pagedResult.items, function (specimen) {
            expect(specimen).toEqual(jasmine.any(self.Specimen));
          });
        });
        self.$httpBackend.flush();
      });

      it('can list specimens using sorting', function() {
        var self = this,
            cevent = self.factory.collectionEvent(),
            reply = self.factory.pagedResult([]),
            sortingTypes = [ 'id', '-timeCreated', 'state' ];

        _.each(sortingTypes, function (sortingType) {
          self.$httpBackend.whenGET(uri(cevent.id) + '?sort=' + sortingType)
            .respond(self.reply(reply));

          self.Specimen.list(cevent.id, { sort: sortingType }).then(function (pagedResult) {
            expect(pagedResult.items).toBeEmptyArray();
          });
          self.$httpBackend.flush();
        });
      });

      it('can list specimens using page number', function() {
        var self = this,
            cevent = self.factory.collectionEvent(),
            reply = self.factory.pagedResult([]),
            page = 2;

        self.$httpBackend.whenGET(uri(cevent.id) + '?page=' + page)
          .respond(this.reply(reply));

        self.Specimen.list(cevent.id, { page: page }).then(function (pagedResult) {
          expect(pagedResult.items).toBeEmptyArray();
        });
        self.$httpBackend.flush();
      });

      it('can list specimens using page size number', function() {
        var self = this,
            cevent = self.factory.collectionEvent(),
            reply = self.factory.pagedResult([]),
            limit = 2;

        self.$httpBackend.whenGET(uri(cevent.id) + '?limit=' + limit)
          .respond(this.reply(reply));

        self.Specimen.list(cevent.id, { limit: limit }).then(function (pagedResult) {
          expect(pagedResult.items).toBeEmptyArray();
        });
        self.$httpBackend.flush();
      });

      it('returns rejected promise if specimens have invalid format', function() {
        var cevent = this.factory.collectionEvent(),
            reply = this.factory.pagedResult([{ tmp: 1 }]),
            catchTriggered = false;

        this.$httpBackend.whenGET(uri(cevent.id)).respond(this.reply(reply));

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
      var self = this,
          specimenDescription,
          cevent,
          jsonCevent,
          jsonSpecimens,
          specimens,
          json;

      this.factory.collectionEventType({ specimenDescriptions: [ specimenDescription ]});
      specimenDescription  = this.factory.collectionSpecimenDescription();
      jsonCevent    = this.factory.collectionEvent();
      cevent        = new this.CollectionEvent(jsonCevent);
      jsonSpecimens = _.range(3).map(function () { return self.factory.specimen(); });
      specimens     = jsonSpecimens.map(function (json) { return new self.Specimen(json); });
      json          = addJson(cevent, jsonSpecimens);

      this.$httpBackend.expectPOST(uri(cevent.id), json).respond(this.reply(jsonCevent));

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
        expect(function () {
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
        expect(function () {
          specimen.defaultAmount();
        }).toThrowError(/specimen spec not assigned/);
      });
    });

    describe('for isDefaultAmount', function() {

      it('true when amount is the same as the default amount', function() {
        var specimenDescription  = this.factory.collectionSpecimenDescription(),
            specimen = new this.Specimen(this.factory.specimen());
        specimen.setSpecimenDescription(specimenDescription);
        specimen.amount = specimenDescription.amount;
        expect(specimen.isDefaultAmount()).toBeTrue();
      });

      it('false when amount is the same as the default amount', function() {
        var specimenDescription  = this.factory.collectionSpecimenDescription(),
            specimen = new this.Specimen(this.factory.specimen());
        specimen.setSpecimenDescription(specimenDescription);
        specimen.amount = specimenDescription.amount + 1;
        expect(specimen.isDefaultAmount()).toBeFalse();
      });

      it('throws a domain error if specimen spec not assigned', function() {
        var specimen = new this.Specimen(this.factory.specimen());
        expect(function () {
          specimen.isDefaultAmount();
        }).toThrowError(/specimen spec not assigned/);
      });

    });

    describe('for units', function() {

      it('can get the units', function() {
        var specimenDescription  = this.factory.collectionSpecimenDescription(),
            specimen = new this.Specimen(this.factory.specimen());
        specimen.setSpecimenDescription(specimenDescription);
        expect(specimen.units()).toBe(specimenDescription.units);
      });

      it('throws a domain error if specimen spec not assigned', function() {
        var specimen = new this.Specimen(this.factory.specimen());
        expect(function () {
          specimen.units();
        }).toThrowError(/specimen spec not assigned/);
      });

    });

    it('should be able to remove a specimen', function() {
      var cevent   = new this.CollectionEvent(this.factory.collectionEvent()),
          specimen = new this.Specimen(this.factory.specimen()),
          url      = sprintf('%s/%s/%d', uri(cevent.id), specimen.id, specimen.version);

      specimen.collectionEventId = cevent.id;
      this.$httpBackend.expectDELETE(url).respond(this.reply(true));
      specimen.remove(cevent.id);
      this.$httpBackend.flush();
    });

  });

  function addJson(collectionEvent, specimens) {
    var json = { collectionEventId: collectionEvent.id };
    json.specimenData = specimens.map(function (specimen) {
      var result = _.pick(specimen, 'inventoryId', 'specimenDescriptionId', 'timeCreated', 'amount');
      result.locationId = specimen.locationInfo.locationId;
      return result;
    });
    return json;
  }

  function uri(id) {
    var result = '/participants/cevents/spcs';
    if (id) {
      result += '/' + id;
    }
    return result;
  }

});
