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

    beforeEach(inject(function (ServerReplyMixin, EntityTestSuiteMixin, extendedDomainEntities) {
      var self = this;

      _.extend(self, EntityTestSuiteMixin.prototype, ServerReplyMixin.prototype);

      self.injectDependencies('$httpBackend',
                              'Shipment',
                              'ShipmentState',
                              'Specimen',
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
        var self = this,
            shipment = self.factory.shipment();

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

      describe('can change state on a shipment', function() {

        var context = {};

        beforeEach(inject(function () {
          context.jsonShipment = this.factory.shipment();
          context.expectedShipment = this.expectShipment;
        }));

        describe('to created state', function() {

          beforeEach(inject(function () {
            context.state = this.ShipmentState.CREATED;
          }));

          changeStateSharedBehaviour(context);

        });

        describe('to packed state', function() {

          beforeEach(inject(function () {
            context.state = this.ShipmentState.PACKED;
          }));

          changeStateSharedBehaviour(context);

        });

        describe('to sent state', function() {

          beforeEach(inject(function () {
            context.state = this.ShipmentState.SENT;
          }));

          changeStateSharedBehaviour(context);

        });

        describe('to received state', function() {

          beforeEach(inject(function () {
            context.state = this.ShipmentState.RECEIVED;
          }));

          changeStateSharedBehaviour(context);

        });

        describe('to unpacked state', function() {

          beforeEach(inject(function () {
            context.state = this.ShipmentState.UNPACKED;
          }));

          changeStateSharedBehaviour(context);

        });

        describe('to lost state', function() {

          beforeEach(inject(function () {
            context.state = this.ShipmentState.LOST;
          }));

          changeStateSharedBehaviour(context);

        });

      });

      describe('can change state on a shipment', function() {

        beforeEach(function() {
          this.jsonShipment = this.factory.shipment();
          this.time         = moment(faker.date.recent(10)).format();
          this.shipment     = new this.Shipment(this.jsonShipment);
        });

        it('can skip state to SENT', function() {
          this.updateEntity.call(this,
                                 this.shipment,
                                 'skipToStateSent',
                                 [ this.time, this.time ],
                                 uri('state/skip-to-sent', this.shipment.id ),
                                 { timePacked: this.time, timeSent: this.time },
                                 this.jsonShipment,
                                 this.expectShipment,
                                 failTest);
        });

        it('can skip state to UNPACKED', function() {
          this.updateEntity.call(this,
                                 this.shipment,
                                 'skipToStateUnpacked',
                                 [ this.time, this.time ],
                                 uri('state/skip-to-unpacked', this.shipment.id ),
                                 { timeReceived: this.time, timeUnpacked: this.time },
                                 this.jsonShipment,
                                 this.expectShipment,
                                 failTest);
        });


      });

    });

    describe('state predicates', function() {

      it('for CREATED state predicate', function() {
        var shipment = new this.Shipment(this.factory.shipment({ state: this.ShipmentState.CREATED }));
        expect(shipment.isCreated()).toBeTrue();
      });

      it('for PACKED state predicate', function() {
        var shipment = new this.Shipment(this.factory.shipment({ state: this.ShipmentState.PACKED }));
        expect(shipment.isPacked()).toBeTrue();
      });

      it('for SENT state predicate', function() {
        var shipment = new this.Shipment(this.factory.shipment({ state: this.ShipmentState.SENT }));
        expect(shipment.isSent()).toBeTrue();
      });

      it('for not CREATED nor UNPACKED predicate', function() {
        var self = this;

        _.forEach([
          self.ShipmentState.PACKED,
          self.ShipmentState.SENT,
          self.ShipmentState.RECEIVED,
          self.ShipmentState.LOST,
        ], function (state) {
          var shipment = new self.Shipment(self.factory.shipment({ state: state }));
          expect(shipment.isNotCreatedNorUnpacked()).toBeTrue();
        });
      });

    });

    it('can add specimen', function() {
      var self = this,
          jsonSpecimen = self.factory.specimen(),
          shipment = new self.Shipment(self.factory.shipment()),
          inventoryId = self.factory.stringNext();

      self.$httpBackend.whenGET(uri('specimens/canadd', shipment.id) + '/' + inventoryId)
        .respond(this.reply(jsonSpecimen));

      shipment.canAddInventoryId(inventoryId)
        .then(checkReply)
        .catch(failTest);
      self.$httpBackend.flush();

      function checkReply(reply) {
        expect(reply).toEqual(jasmine.any(self.Specimen));
      }
    });


    /**
     * @param {domain.centres.ShipmentState} context.state - the new state to change to.
     *
     * @param {Object} context.jsonShipment - A Json object representing the shipment.
     *
     * @param {domain.centres.Shipment} context.expectedShipment - The Shipment object that should be returned
     * from the update request.
     */
    function changeStateSharedBehaviour(context) {

      describe('shared state change behaviour', function () {

        it('can change state', function() {
          var time         = moment(faker.date.recent(10)).format(),
              shipment     = new this.Shipment(context.jsonShipment);

          this.updateEntity.call(this,
                                 shipment,
                                 'changeState',
                                 [ context.state, time ],
                                 uri('state', shipment.id ),
                                 { newState: context.state, datetime: time },
                                 context.jsonShipment,
                                 context.expectShipment,
                                 failTest);
        });

      });
    }

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
