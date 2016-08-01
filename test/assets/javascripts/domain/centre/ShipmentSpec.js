/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(function (require) {
  'use strict';

  var mocks   = require('angularMocks'),
      $       = require('jquery'),
      _       = require('lodash'),
      faker   = require('faker'),
      moment  = require('moment'),
      sprintf = require('sprintf').sprintf;

  require('angular');

  /**
   *
   */
  describe('Shipment domain object:', function() {

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function (serverReplyMixin, entityTestSuite, extendedDomainEntities) {
      var self = this;

      _.extend(self, entityTestSuite, serverReplyMixin);

      self.injectDependencies('$httpBackend',
                              'Shipment',
                              'ShipmentState',
                              'funutils',
                              'testUtils',
                              'factory');

      self.expectShipment = expectShipment;

      //---

      // used by promise tests
      function expectShipment(entity) {
        expect(entity).toEqual(jasmine.any(self.Shipment));
      }

    }));

    afterEach(function() {
      this.$httpBackend.verifyNoOutstandingExpectation();
      this.$httpBackend.verifyNoOutstandingRequest();
    });

    describe('for constructor', function() {

      it('constructor with no parameters has default values', function() {
        var shipment = new this.Shipment();

        expect(shipment.id).toBeNull();
        expect(shipment.version).toBe(0);
        expect(shipment.timeAdded).toBeNull();
        expect(shipment.timeModified).toBeNull();
        expect(shipment.state).toBe(this.ShipmentState.CREATED);
        expect(shipment.courierName).toBeEmptyString();
        expect(shipment.trackingNumber).toBeEmptyString();
        expect(shipment.trackingNumber).toBeEmptyString();
      });

      it('fails when creating from a non object', function() {
        var self = this,
            badJson = _.omit(self.factory.shipment(), 'courierName');

        expect(function () { self.Shipment.create(badJson); })
          .toThrowError(/invalid object from server/);
      });

      it('fails when creating from a bad from location ID', function() {
        var self = this,
            badJson = self.factory.shipment({ fromLocationInfo: undefined });

        expect(function () { self.Shipment.create(badJson); })
          .toThrowError(/invalid object from server.*fromLocationInfo/);
      });

      it('fails when creating from a bad to location ID', function() {
        var self = this,
            badJson = self.factory.shipment({ toLocationInfo: undefined });

        expect(function () { self.Shipment.create(badJson); })
          .toThrowError(/invalid object from server.*toLocationInfo/);
      });

    });

    describe('when getting a single shipment', function() {

      it('can retrieve a single shipment', function() {
        var self = this, shipment = self.factory.shipment();

        self.$httpBackend.whenGET(uri(shipment.id)).respond(this.reply(shipment));

        self.Shipment.get(shipment.id).then(checkReply).catch(failTest);
        self.$httpBackend.flush();

        function checkReply(reply) {
          expect(reply).toEqual(jasmine.any(self.Shipment));
          reply.compareToJsonEntity(shipment);
        }
      });

      it('fails when getting a shipment and it has a bad format', function() {
        var self = this,
            shipment = _.omit(self.factory.shipment(), 'courierName');

        self.$httpBackend.whenGET(uri(shipment.id)).respond(this.reply(shipment));

        self.Shipment.get(shipment.id).then(shouldNotFail).catch(shouldFail);
        self.$httpBackend.flush();

        function shouldNotFail(reply) {
          fail('function should not be called');
        }

        function shouldFail(error) {
          expect(error).toStartWith('invalid object from server');
        }
      });

      it('fails when getting a shipment and it has a bad from location', function() {
        var self = this,
            shipment = _.omit(self.factory.shipment(), 'fromLocationInfo');

        self.$httpBackend.whenGET(uri(shipment.id)).respond(this.reply(shipment));

        self.Shipment.get(shipment.id).then(shouldNotFail).catch(shouldFail);
        self.$httpBackend.flush();

        function shouldNotFail(reply) {
          fail('function should not be called');
        }

        function shouldFail(error) {
          expect(error).toStartWith('invalid object from server');
        }
      });

      it('fails when getting a shipment and it has a bad from location', function() {
        var self = this,
            shipment = _.omit(self.factory.shipment(), 'toLocationInfo');

        self.$httpBackend.whenGET(uri(shipment.id)).respond(this.reply(shipment));

        self.Shipment.get(shipment.id).then(shouldNotFail).catch(shouldFail);
        self.$httpBackend.flush();

        function shouldNotFail(reply) {
          fail('function should not be called');
        }

        function shouldFail(error) {
          expect(error).toStartWith('invalid object from server');
        }
      });

    });

    describe('when listing shipments', function() {

      it('can retrieve shipments', function() {
        var self = this,
            centre = self.factory.centre(),
            shipment = self.factory.shipment(),
            shipments = [ shipment ],
            reply = self.factory.pagedResult(shipments);

        self.$httpBackend.whenGET(listUri(centre.id)).respond(this.reply(reply));

        self.Shipment.list(centre.id).then(checkReply).catch(failTest);
        self.$httpBackend.flush();

        function checkReply(pagedResult) {
          expect(pagedResult.items).toBeArrayOfSize(shipments.length);
          _.each(pagedResult.items, function (item) {
            expect(item).toEqual(jasmine.any(self.Shipment));
            item.compareToJsonEntity(shipment);
          });
        }
      });

      it('can use options', function() {
        var self = this,
            centre = self.factory.centre(),
            optionList = [
              { courierFilter: 'Fedex' },
              { trackingNumberFilter: 'ABC' },
              { sort: 'state' },
              { page: 2 },
              { pageSize: 10 },
              { order: 'desc' }
            ];

        _.each(optionList, function (options) {
          var shipments = [ self.factory.shipment() ],
              reply   = self.factory.pagedResult(shipments),
              url     = sprintf('%s?%s', listUri(centre.id), $.param(options, true));

          self.$httpBackend.whenGET(url).respond(self.reply(reply));

          self.Shipment.list(centre.id, options).then(testShipment).catch(failTest);
          self.$httpBackend.flush();

          function testShipment(pagedResult) {
            expect(pagedResult.items).toBeArrayOfSize(shipments.length);
            _.each(pagedResult.items, function (study) {
              expect(study).toEqual(jasmine.any(self.Shipment));
            });
          }
        });
      });

      it('fails when list returns an invalid shipment', function() {
        var self = this,
            centre = self.factory.centre(),
            shipments = [ _.omit(self.factory.shipment(), 'courierName') ],
            reply = self.factory.pagedResult(shipments);

        self.$httpBackend.whenGET(listUri(centre.id)).respond(this.reply(reply));

        self.Shipment.list(centre.id).then(listFail).catch(shouldFail);
        self.$httpBackend.flush();

        function listFail(reply) {
          fail('function should not be called');
        }

        function shouldFail(error) {
          expect(error).toStartWith('invalid shipments from server');
        }
      });

    });

    describe('when adding a shipment', function() {

      it('can add a shipment', function() {
        var self = this,
            jsonShipment = self.factory.shipment(),
            shipment = new self.Shipment(_.omit(jsonShipment, 'id')),
            json = _.extend(_.pick(shipment, 'courierName', 'trackingNumber'),
                            {
                              fromLocationId: shipment.fromLocationInfo.locationId,
                              toLocationId:   shipment.toLocationInfo.locationId
                            });

        self.$httpBackend.expectPOST(uri(), json).respond(this.reply(jsonShipment));

        shipment.add().then(checkReply).catch(failTest);
        self.$httpBackend.flush();

        function checkReply(replyShipment) {
          expect(replyShipment).toEqual(jasmine.any(self.Shipment));
        }
      });

    });

    describe('when updating a shipment', function() {

      it('can update the courier name on a shipment', function() {
        var self         = this,
            jsonShipment = self.factory.shipment(),
            shipment     = new self.Shipment(jsonShipment);

        this.updateEntity.call(this,
                               shipment,
                               'updateCourierName',
                               shipment.courierName,
                               uri('courier', shipment.id),
                               { courierName: shipment.courierName },
                               jsonShipment,
                               this.expectShipment,
                               failTest);
      });

      it('can update the tracking number on a shipment', function() {
        var self         = this,
            jsonShipment = self.factory.shipment(),
            shipment     = new self.Shipment(jsonShipment);

        this.updateEntity.call(this,
                               shipment,
                               'updateTrackingNumber',
                               shipment.trackingNumber,
                               uri('trackingnumber', shipment.id),
                               { trackingNumber: shipment.trackingNumber },
                               jsonShipment,
                               this.expectShipment,
                               failTest);
      });

      it('can update the FROM location on a shipment', function() {
        var self         = this,
            jsonShipment = self.factory.shipment(),
            shipment     = new self.Shipment(jsonShipment);

        this.updateEntity.call(this,
                               shipment,
                               'updateFromLocation',
                               shipment.fromLocationId,
                               uri('fromlocation', shipment.id),
                               { locationId: shipment.fromLocationId },
                               jsonShipment,
                               this.expectShipment,
                               failTest);
      });

      it('can update the TO location on a shipment', function() {
        var self         = this,
            jsonShipment = self.factory.shipment(),
            shipment     = new self.Shipment(jsonShipment);

        this.updateEntity.call(this,
                               shipment,
                               'updateToLocation',
                               shipment.toLocationId,
                               uri('tolocation', shipment.id),
                               { locationId: shipment.toLocationId },
                               jsonShipment,
                               this.expectShipment,
                               failTest);
      });

      it('can update a shipment to packed state', function() {
        var self         = this,
            jsonShipment = self.factory.shipment(),
            time         = moment(faker.date.recent(10)).format(),
            shipment     = new self.Shipment(jsonShipment);

        this.updateEntity.call(this,
                               shipment,
                               'packed',
                               time,
                               uri('packed', shipment.id ),
                               { time: time },
                               jsonShipment,
                               this.expectShipment,
                               failTest);
      });

      it('can update a shipment to sent state', function() {
        var self         = this,
            jsonShipment = self.factory.shipment(),
            time         = moment(faker.date.recent(10)).format(),
            shipment     = new self.Shipment(jsonShipment);

        this.updateEntity.call(this,
                               shipment,
                               'sent',
                               time,
                               uri('sent', shipment.id ),
                               { time: time },
                               jsonShipment,
                               this.expectShipment,
                               failTest);
      });

      it('can update a shipment to received state', function() {
        var self         = this,
            jsonShipment = self.factory.shipment(),
            time         = moment(faker.date.recent(10)).format(),
            shipment     = new self.Shipment(jsonShipment);

        this.updateEntity.call(this,
                               shipment,
                               'received',
                               time,
                               uri('received', shipment.id ),
                               { time: time },
                               jsonShipment,
                               this.expectShipment,
                               failTest);
      });

      it('can update a shipment to unpacked state', function() {
        var self         = this,
            jsonShipment = self.factory.shipment(),
            time         = moment(faker.date.recent(10)).format(),
            shipment     = new self.Shipment(jsonShipment);

        this.updateEntity.call(this,
                               shipment,
                               'unpacked',
                               time,
                               uri('unpacked', shipment.id ),
                               { time: time },
                               jsonShipment,
                               this.expectShipment,
                               failTest);
      });

      it('can update a shipment to lost state', function() {
        var self         = this,
            jsonShipment = self.factory.shipment(),
            shipment     = new self.Shipment(jsonShipment);

        this.updateEntity.call(this,
                               shipment,
                               'lost',
                               undefined,
                               uri('lost', shipment.id ),
                               {},
                               jsonShipment,
                               this.expectShipment,
                               failTest);
      });

    });

    // used by promise tests
    function failTest(error) {
      expect(error).toBeUndefined();
    }

    function uri(/* path, shipmentId */) {
      var result = '/shipments',
          args = _.toArray(arguments),
          shipmentId,
          path;

      if (args.length > 0) {
        path = args.shift();
        result += '/' + path;
      }

      if (args.length > 0) {
        shipmentId = args.shift();
        result += '/' + shipmentId;
      }

      return result;
    }

    function listUri(centreId) {
      return uri() +'/list/' + centreId;
    }
  });

});
