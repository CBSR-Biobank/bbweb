/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define([
  'angular',
  'angularMocks',
  'jquery',
  'lodash',
  'sprintf',
  'faker',
  'moment'
], function(angular, mocks, $, _, sprintf, faker, moment) {
  'use strict';

  /**
   *
   */
  describe('Shipment', function() {

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function (serverReplyMixin, entityTestSuite, extendedDomainEntities) {
      var self = this;

      _.extend(self, entityTestSuite, serverReplyMixin);

      self.injectDependencies('$httpBackend',
                              'ShipmentSpecimen',
                              'ShipmentItemState',
                              'funutils',
                              'testUtils',
                              'factory');

      self.expectShipmentSpecimen = expectShipmentSpecimen;

      //---

      // used by promise tests
      function expectShipmentSpecimen(entity) {
        expect(entity).toEqual(jasmine.any(self.ShipmentSpecimen));
      }

    }));

    afterEach(function() {
      this.$httpBackend.verifyNoOutstandingExpectation();
      this.$httpBackend.verifyNoOutstandingRequest();
    });

    describe('for constructor', function() {

      it('constructor with no parameters has default values', function() {
        var ss = new this.ShipmentSpecimen();

        expect(ss.id).toBeNull();
        expect(ss.version).toBe(0);
        expect(ss.timeAdded).toBeNull();
        expect(ss.timeModified).toBeNull();
        expect(ss.state).toBe(this.ShipmentItemState.PRESENT);
        expect(ss.shipmentId).toBeNull();
        expect(ss.specimenId).toBeNull();
      });

      it('fails when creating from a non object', function() {
        var self = this;

        expect(function () { self.ShipmentSpecimen.create('test'); })
          .toThrowError(/invalid object from server/);
      });

      it('fails when creating from a bad shipment ID', function() {
        var self = this,
            badJson = self.factory.shipmentSpecimen({ shipmentId: undefined });

        expect(function () { self.ShipmentSpecimen.create(badJson); })
          .toThrowError(/invalid object from server.*shipmentId/);
      });

      it('fails when creating from a bad specimen ID', function() {
        var self = this,
            badJson = self.factory.shipmentSpecimen({ specimenId: undefined });

        expect(function () { self.ShipmentSpecimen.create(badJson); })
          .toThrowError(/invalid object from server.*specimenId/);
      });

      it('fails when creating from a bad location ID', function() {
        var self = this,
            badJson = self.factory.shipmentSpecimen({ locationId: undefined });

        expect(function () { self.ShipmentSpecimen.create(badJson); })
          .toThrowError(/invalid object from server.*locationId/);
      });

      it('fails when creating from a bad location name', function() {
        var self = this,
            badJson = self.factory.shipmentSpecimen({ locationName: undefined });

        expect(function () { self.ShipmentSpecimen.create(badJson); })
          .toThrowError(/invalid object from server.*locationName/);
      });

      it('fails when creating from a bad time created', function() {
        var self = this,
            badJson = self.factory.shipmentSpecimen({ timeCreated: undefined });

        expect(function () { self.ShipmentSpecimen.create(badJson); })
          .toThrowError(/invalid object from server.*timeCreated/);
      });

      it('fails when creating from a bad amount', function() {
        var self = this,
            badJson = self.factory.shipmentSpecimen({ amount: undefined });

        expect(function () { self.ShipmentSpecimen.create(badJson); })
          .toThrowError(/invalid object from server.*amount/);
      });

      it('fails when creating from a bad units', function() {
        var self = this,
            badJson = self.factory.shipmentSpecimen({ units: undefined });

        expect(function () { self.ShipmentSpecimen.create(badJson); })
          .toThrowError(/invalid object from server.*units/);
      });

      it('fails when creating from a bad status', function() {
        var self = this,
            badJson = self.factory.shipmentSpecimen({ status: undefined });

        expect(function () { self.ShipmentSpecimen.create(badJson); })
          .toThrowError(/invalid object from server.*status/);
      });

    });

    describe('when getting a single shipment', function() {

      it('can retrieve a single shipment specimen', function() {
        var self = this,
            ss = self.factory.shipmentSpecimen();

        self.$httpBackend.whenGET(uri(ss.id)).respond(this.reply(ss));

        self.ShipmentSpecimen.get(ss.id).then(checkReply).catch(failTest);
        self.$httpBackend.flush();

        function checkReply(reply) {
          expect(reply).toEqual(jasmine.any(self.ShipmentSpecimen));
          reply.compareToJsonEntity(ss);
        }
      });

      it('fails when getting a shipment specimen and it has a missing required field', function() {
        var self = this,
            requiredProperties = [ 'version',
                                   'state',
                                   'shipmentId',
                                   'specimenId',
                                   'locationId',
                                   'locationName',
                                   'timeCreated',
                                   'amount',
                                   'units',
                                   'status'];

        _(requiredProperties).forEach(function (property) {
          var ss = _.omit(self.factory.shipmentSpecimen(), property);

          self.$httpBackend.whenGET(uri(ss.id)).respond(self.reply(ss));

          self.ShipmentSpecimen.get(ss.id).then(shouldNotFail).catch(shouldFail);
          self.$httpBackend.flush();

          function shouldFail(error) {
            expect(error).toMatch('invalid object from server.*' + property);
          }
        });

        function shouldNotFail(reply) {
          fail('function should not be called');
        }
      });

    });

    describe('when listing shipment specimens', function() {

      it('can retrieve shipment specimens', function() {
        var self = this,
            ssArray = [ self.factory.shipmentSpecimen() ],
            shipmentId = ssArray[0].shipmentId,
            reply = self.factory.pagedResult(ssArray);

        self.$httpBackend.whenGET(uri(shipmentId)).respond(this.reply(reply));

        self.ShipmentSpecimen.list(shipmentId).then(checkReply).catch(failTest);
        self.$httpBackend.flush();

        function checkReply(pagedResult) {
          expect(pagedResult.items).toBeArrayOfSize(ssArray.length);
          _.each(pagedResult.items, function (item) {
            expect(item).toEqual(jasmine.any(self.ShipmentSpecimen));
            item.compareToJsonEntity(ssArray[0]);
          });
        }
      });

      it('can list shipment specimens using options', function() {
        var self = this,
            optionList = [
              { sort: 'inventoryId' },
              { page: 2 },
              { pageSize: 10 },
              { order: 'desc' }
            ];

        _.each(optionList, function (options) {
          var ssArray    = [ self.factory.shipmentSpecimen() ],
              shipmentId = ssArray[0].shipmentId,
              reply      = self.factory.pagedResult(ssArray),
              url        = sprintf.sprintf('%s?%s', uri(shipmentId), $.param(options, true));

          self.$httpBackend.whenGET(url).respond(self.reply(reply));

          self.ShipmentSpecimen.list(shipmentId, options).then(testShipmentSpecimens).catch(failTest);
          self.$httpBackend.flush();

          function testShipmentSpecimens(pagedResult) {
            expect(pagedResult.items).toBeArrayOfSize(ssArray.length);
            _.each(pagedResult.items, function (study) {
              expect(study).toEqual(jasmine.any(self.ShipmentSpecimen));
            });
          }
        });
      });

      it('fails when list returns an invalid shipment', function() {
        var self       = this,
            ssArray    = [ _.omit(self.factory.shipmentSpecimen(), 'state') ],
            shipmentId = ssArray[0].shipmentId,
            reply      = self.factory.pagedResult(ssArray);

        self.$httpBackend.whenGET(uri(shipmentId)).respond(this.reply(reply));

        self.ShipmentSpecimen.list(shipmentId).then(listFail).catch(shouldFail);
        self.$httpBackend.flush();

        function listFail(reply) {
          fail('function should not be called');
        }

        function shouldFail(error) {
          expect(error).toStartWith('invalid shipment specimens from server');
        }
      });

    });

    describe('when adding a shipment specimen', function() {

      it('can add a shipment', function() {
        var self   = this,
            jsonSs = self.factory.shipmentSpecimen(),
            ss     = new self.ShipmentSpecimen(_.omit(jsonSs, 'id'));

        self.$httpBackend.expectPOST(uri(ss.shipmentId)).respond(this.reply(jsonSs));

        ss.add(ss.shipmentId, ss.specimenId).then(checkReply).catch(failTest);
        self.$httpBackend.flush();

        function checkReply(reply) {
          expect(reply).toEqual(jasmine.any(self.ShipmentSpecimen));
        }
      });

    });

    describe('when updating a shipment specimen', function() {

      it('can update the shipment container on a shipment specimen', function() {
        var self   = this,
            jsonSs = self.factory.shipmentSpecimen(),
            ss     = new self.ShipmentSpecimen(jsonSs);

        this.updateEntity.call(this,
                               ss,
                               'updateShipmentContainer',
                               ss.shipmentContainerId,
                               uri('container', ss.shipmentId, ss.id),
                               { shipmentContainerId: ss.shipmentContainerId },
                               jsonSs,
                               this.expectShipment,
                               failTest);
      });

      it('can update the shipment specimen to received state', function() {
        var self   = this,
            jsonSs = self.factory.shipmentSpecimen(),
            ss     = new self.ShipmentSpecimen(jsonSs);

        this.updateEntity.call(this,
                               ss,
                               'received',
                               ss.state,
                               uri('received', ss.shipmentId, ss.id),
                               { },
                               jsonSs,
                               this.expectShipment,
                               failTest);
      });

      it('can update the shipment specimen to missing state', function() {
        var self   = this,
            jsonSs = self.factory.shipmentSpecimen(),
            ss     = new self.ShipmentSpecimen(jsonSs);

        this.updateEntity.call(this,
                               ss,
                               'missing',
                               ss.state,
                               uri('missing', ss.shipmentId, ss.id),
                               { },
                               jsonSs,
                               this.expectShipment,
                               failTest);
      });

      it('can update the shipment specimen to extra state', function() {
        var self   = this,
            jsonSs = self.factory.shipmentSpecimen(),
            ss     = new self.ShipmentSpecimen(jsonSs);

        this.updateEntity.call(this,
                               ss,
                               'extra',
                               ss.state,
                               uri('extra', ss.shipmentId, ss.id),
                               { },
                               jsonSs,
                               this.expectShipment,
                               failTest);
      });

    });

    // used by promise tests
    function failTest(error) {
      expect(error).toBeUndefined();
    }

    function uri(/* path, shipmentId, shipmentSpecimenId */) {
      var path,
          shipmentId,
          shipmentSpecimenId,
          result = '/shipments/specimens',
          args = _.toArray(arguments);

      if (args.length > 0) {
        path = args.shift();
        result += '/' + path;
      }

      if (args.length > 0) {
        shipmentId = args.shift();
        result += '/' + shipmentId;
      }

      if (args.length > 0) {
        shipmentSpecimenId = args.shift();
        result += '/' + shipmentSpecimenId;
      }

      return result;
    }

  });


});
