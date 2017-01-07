/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define([
  'angular',
  'angularMocks',
  'lodash'
], function(angular, mocks, _) {
  'use strict';

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
      var specimenSpec = this.factory.collectionSpecimenSpec(),
          specimen = new this.Specimen({}, specimenSpec);
      expect(specimen.specimenSpecId).toBe(specimenSpec.uniqueId);
    });

    it('can list specimens for a collection event', function() {
      var self = this,
          specimenSpec,
          cevent,
          specimens,
          reply;

      self.factory.centre({ locations: [ self.factory.location() ]});

      specimenSpec = self.factory.collectionSpecimenSpec();
      self.factory.collectionEventType({ specimenSpecs: [ specimenSpec ]});

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

    it('can add specimens', function() {
      var self = this,
          specimenSpec,
          cevent,
          jsonCevent,
          jsonSpecimens,
          specimens,
          json;

      self.factory.centre({ locations: [ self.factory.location() ]});
      specimenSpec  = self.factory.collectionSpecimenSpec();
      this.factory.collectionEventType({ specimenSpecs: [ specimenSpec ]});
      jsonCevent    = self.factory.collectionEvent();
      jsonSpecimens = _.map(_.range(3), function () { return self.factory.specimen(); });
      specimens     = _.map(jsonSpecimens, function (json) { return new self.Specimen(json); });

      cevent = new this.CollectionEvent(jsonCevent);
      json   = addJson(cevent, jsonSpecimens);

      this.$httpBackend.expectPOST(uri(cevent.id), json).respond(this.reply(jsonCevent));

      self.Specimen.add(cevent.id, specimens);
      this.$httpBackend.flush();
    });

    it('fails when attempting add a specimens for an invalid collection event', function() {
    });

  });

  function addJson(collectionEvent, specimens) {
    var json = { collectionEventId: collectionEvent.id };
    json.specimenData = _.map(specimens, function (specimen) {
      return _.pick(specimen, 'inventoryId', 'specimenSpecId', 'timeCreated', 'locationId', 'amount');
    });
    return json;
  }

  function uri(collectionEventId) {
    return  '/participants/cevents/spcs/' + collectionEventId;
  }

});
