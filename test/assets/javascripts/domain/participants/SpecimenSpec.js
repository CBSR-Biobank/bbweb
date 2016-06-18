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

    beforeEach(inject(function(entityTestSuite,
                               hasAnnotationsEntityTestSuite,
                               testUtils,
                               extendedDomainEntities) {
      var self = this;

      _.extend(self, entityTestSuite, hasAnnotationsEntityTestSuite);

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

      console.log(specimen);

      expect(specimen.id).toBeNull();
      expect(specimen.version).toBe(0);
      expect(specimen.originLocationId).toBeNull();
      expect(specimen.locationId).toBeNull();
      expect(specimen.containerId).toBeUndefined();
      expect(specimen.positionId).toBeUndefined();
      expect(specimen.timeAdded).toBeNull();
      expect(specimen.timeModified).toBeNull();
      expect(specimen.timeCreated).toBeNull();
      expect(specimen.amount).toBeNull();
      expect(specimen.status).toBeNull();
    });

    it('constructor with specimen spec has valid values', function() {
      var specimenSpec = this.factory.collectionSpecimenSpec(),
          specimen = new this.Specimen({}, specimenSpec);
      expect(specimen.specimenSpecId).toBe(specimenSpec.uniqueId);
    });

    it('can list specimens for a collection event', function() {
      var self = this,
          centre,
          specimenSpec,
          ceventType,
          cevent,
          specimens,
          reply;

      centre       = self.factory.centre({ locations: [ self.factory.location() ]});
      specimenSpec = self.factory.collectionSpecimenSpec();
      ceventType   = this.factory.collectionEventType({ specimenSpecs: [ specimenSpec ]});
      cevent       = self.factory.collectionEvent();
      specimens    = _.map(_.range(3), function () { return self.factory.specimen(); });
      reply        = self.factory.pagedResult(specimens);

      self.$httpBackend.whenGET(uri(cevent.id)).respond(serverReply(reply));

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
          sortingTypes = [ 'id', 'timeCreated', 'status' ];

      _.each(sortingTypes, function (sortingType) {
        self.$httpBackend.whenGET(uri(cevent.id) + '?sort=' + sortingType)
          .respond(serverReply(reply));

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
        .respond(serverReply(reply));

      self.Specimen.list(cevent.id, { page: page }).then(function (pagedResult) {
        expect(pagedResult.items).toBeEmptyArray();
      });
      self.$httpBackend.flush();
    });

    it('can list specimens using page size number', function() {
      var self = this,
          cevent = self.factory.collectionEvent(),
          reply = self.factory.pagedResult([]),
          pageSize = 2;

      self.$httpBackend.whenGET(uri(cevent.id) + '?pageSize=' + pageSize)
        .respond(serverReply(reply));

      self.Specimen.list(cevent.id, { pageSize: pageSize }).then(function (pagedResult) {
        expect(pagedResult.items).toBeEmptyArray();
      });
      self.$httpBackend.flush();
    });

    it('can list specimens using ordering', function() {
      var self = this,
          cevent = self.factory.collectionEvent(),
          reply = self.factory.pagedResult([]),
          orderingTypes = [ 'asc', 'desc'];

      _.each(orderingTypes, function (orderingType) {
        self.$httpBackend.whenGET(uri(cevent.id) + '?order=' + orderingType)
          .respond(serverReply(reply));

        self.Specimen.list(cevent.id, { order: orderingType }).then(function (pagedResult) {
          expect(pagedResult.items).toBeEmptyArray();
        });
        self.$httpBackend.flush();
      });
    });

    it('can add specimens', function() {
      var self = this,
          centre,
          specimenSpec,
          ceventType,
          cevent,
          jsonCevent,
          jsonSpecimens,
          specimens,
          json;

      centre        = self.factory.centre({ locations: [ self.factory.location() ]});
      specimenSpec  = self.factory.collectionSpecimenSpec();
      ceventType    = this.factory.collectionEventType({ specimenSpecs: [ specimenSpec ]});
      jsonCevent    = self.factory.collectionEvent();
      jsonSpecimens = _.map(_.range(3), function () { return self.factory.specimen(); });
      specimens     = _.map(jsonSpecimens, function (json) { return new self.Specimen(json); });

      cevent = new this.CollectionEvent(jsonCevent);
      json   = addJson(cevent, jsonSpecimens);

      this.$httpBackend.expectPOST(uri(cevent.id), json).respond(201, serverReply(jsonCevent));

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

  function serverReply(event) {
    return { status: 'success', data: event };
  }

  function uri(collectionEventId) {
    return  '/participants/cevents/spcs/' + collectionEventId;
  }

});
