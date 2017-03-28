/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(function (require) {
  'use strict';

  var mocks   = require('angularMocks'),
      _       = require('lodash'),
      faker   = require('faker'),
      moment  = require('moment'),
      sprintf = require('sprintf-js').sprintf;

  require('angular');

  /**
   * Test suite for shipment domain entity.
   */
  describe('Shipment domain object:', function() {

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function (ServerReplyMixin, EntityTestSuiteMixin, testDomainEntities) {
      var self = this;

      _.extend(this, EntityTestSuiteMixin.prototype, ServerReplyMixin.prototype);

      this.injectDependencies('$httpBackend',
                              '$httpParamSerializer',
                              'Shipment',
                              'ShipmentSpecimen',
                              'ShipmentState',
                              'Specimen',
                              'funutils',
                              'testUtils',
                              'factory');

      this.expectShipment = expectShipment;
      testDomainEntities.extend();

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

        function shouldNotFail() {
          fail('function should not be called');
        }

        function shouldFail(error) {
          expect(error.message).toContain('Missing required property');
        }
      });

      it('fails when getting a shipment and it has a bad from location', function() {
        var self = this,
            shipment = _.omit(self.factory.shipment(), 'fromLocationInfo');

        self.$httpBackend.whenGET(uri(shipment.id)).respond(this.reply(shipment));

        self.Shipment.get(shipment.id).then(shouldNotFail).catch(shouldFail);
        self.$httpBackend.flush();

        function shouldNotFail() {
          fail('function should not be called');
        }

        function shouldFail(error) {
          expect(error.message).toContain('Missing required property');
        }
      });

      it('fails when getting a shipment and it has a bad from location', function() {
        var self = this,
            shipment = _.omit(self.factory.shipment(), 'toLocationInfo');

        self.$httpBackend.whenGET(uri(shipment.id)).respond(this.reply(shipment));

        self.Shipment.get(shipment.id).then(shouldNotFail).catch(shouldFail);
        self.$httpBackend.flush();

        function shouldNotFail() {
          fail('function should not be called');
        }

        function shouldFail(error) {
          expect(error.message).toContain('Missing required property');
        }
      });

      it('throws an exception if id is falsy', function() {
        var self = this;

        expect(function () {
          self.Shipment.get();
        }).toThrowError(/shipment id not specified/);
      });

    });

    describe('when listing shipments', function() {

      it('can retrieve shipments', function() {
        var self         = this,
            shipment     = self.factory.shipment(),
            shipments    = [ shipment ],
            reply        = self.factory.pagedResult(shipments);

        self.$httpBackend.whenGET(uri('list')).respond(self.reply(reply));

        this.Shipment.list().then(checkReply).catch(failTest);
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
              { filter: 'fromCentre:eq:' + centre.name },
              { filter: 'toCentre:ne:' + centre.name },
              { filter: 'withCentre::' + centre.name },
              { filter: 'courierName:like:Fedex' },
              { filter: 'trackingNumber::ABC' },
              { sort: 'state' },
              { page: 2 },
              { limit: 10 },
              { order: 'desc' }
            ];

        optionList.forEach(function (options) {
          var shipments = [ self.factory.shipment() ],
              reply     = self.factory.pagedResult(shipments);

          var url = sprintf('%s?%s', uri('list'), self.$httpParamSerializer(options));

          self.$httpBackend.whenGET(url).respond(self.reply(reply));

          self.Shipment.list(options).then(testShipment).catch(failTest);
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
            shipments = [ _.omit(self.factory.shipment(), 'courierName') ],
            reply = self.factory.pagedResult(shipments);

        this.$httpBackend.whenGET(uri('list')).respond(self.reply(reply));
        this.Shipment.list().then(listFail).catch(shouldFail);
        self.$httpBackend.flush();
      });

      function listFail() {
        fail('function should not be called');
      }

      function shouldFail(error) {
        expect(error).toStartWith('invalid shipments from server');
      }
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

        this.updateEntity(shipment,
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

        this.updateEntity(shipment,
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

        this.updateEntity(shipment,
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

        this.updateEntity(shipment,
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
          context.stateChangeTime = undefined;
        }));

        describe('to created state', function() {

          beforeEach(inject(function () {
            context.stateChangeFuncName = 'created';
            context.state = this.ShipmentState.CREATED;
          }));

          changeStateSharedBehaviour(context);

        });

        describe('to packed state', function() {

          beforeEach(inject(function () {
            context.stateChangeFuncName = 'pack';
            context.state = this.ShipmentState.PACKED;
            context.stateChangeTime = moment(faker.date.recent(10)).format();
          }));

          changeStateSharedBehaviour(context);

        });

        describe('to sent state', function() {

          beforeEach(inject(function () {
            context.stateChangeFuncName = 'send';
            context.state = this.ShipmentState.SENT;
            context.stateChangeTime = moment(faker.date.recent(10)).format();
          }));

          changeStateSharedBehaviour(context);

        });

        describe('to received state', function() {

          beforeEach(inject(function () {
            context.stateChangeFuncName = 'receive';
            context.state = this.ShipmentState.RECEIVED;
            context.stateChangeTime = moment(faker.date.recent(10)).format();
          }));

          changeStateSharedBehaviour(context);

        });

        describe('to unpacked state', function() {

          beforeEach(inject(function () {
            context.stateChangeFuncName = 'unpack';
            context.state = this.ShipmentState.UNPACKED;
            context.stateChangeTime = moment(faker.date.recent(10)).format();
          }));

          changeStateSharedBehaviour(context);

        });

        describe('to completed state', function() {

          beforeEach(inject(function () {
            context.stateChangeFuncName = 'complete';
            context.state = this.ShipmentState.COMPLETED;
            context.stateChangeTime = moment(faker.date.recent(10)).format();
          }));

          changeStateSharedBehaviour(context);

        });

        describe('to lost state', function() {

          beforeEach(inject(function () {
            context.stateChangeFuncName = 'lost';
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
          this.updateEntity(this.shipment,
                            'skipToStateSent',
                            [ this.time, this.time ],
                            uri('state/skip-to-sent', this.shipment.id ),
                            { timePacked: this.time, timeSent: this.time },
                            this.jsonShipment,
                            this.expectShipment,
                            failTest);
        });

        it('can skip state to UNPACKED', function() {
          this.updateEntity(this.shipment,
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

      it('for UNPACKED state predicate', function() {
        var shipment = new this.Shipment(this.factory.shipment({ state: this.ShipmentState.UNPACKED }));
        expect(shipment.isUnpacked()).toBeTrue();
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

    describe('for canAddInventoryId', function() {

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

      it('throws an exception if id is falsy', function() {
        var shipment = new this.Shipment(this.factory.shipment());

        expect(function () {
          shipment.canAddInventoryId();
        }).toThrowError(/specimen inventory id not specified/);
      });

    });

    describe('when adding shipment specimens', function() {

      it('can add a shipment', function() {
        var self   = this,
        jsonSpecimen = self.factory.specimen(),
        jsonShipment = self.factory.shipment(),
        shipment = new self.Shipment(jsonShipment);


        self.$httpBackend.expectPOST(uri('specimens', shipment.id)).respond(this.reply(jsonShipment));

        shipment.addSpecimens([ jsonSpecimen.id ]).then(checkReply).catch(failTest);
        self.$httpBackend.flush();

        function checkReply(reply) {
          expect(reply).toEqual(jasmine.any(self.Shipment));
        }
      });

      xit('can add specimens in containers to shipments', function () {
        fail('needs to be implemented');
      });

      it('addSpecimens throws an exception if id is falsy', function() {
        var shipment = new this.Shipment(this.factory.shipment());

        expect(function () {
          shipment.addSpecimens();
        }).toThrowError(/specimenInventoryIds should be an array/);
      });

    });

    describe('when updating a shipment specimen', function() {

      it('can update the shipment container on a shipment specimen', function() {
        var self         = this,
            jsonSs       = self.factory.shipmentSpecimen(),
            ss           = new self.ShipmentSpecimen(jsonSs),
            jsonShipment = self.factory.shipment(),
            shipment     = new self.Shipment(jsonShipment),
            reqJson      = {
              shipmentContainerId: ss.shipmentContainerId,
              shipmentSpecimenData: [{
                shipmentSpecimenId: ss.id,
                expectedVersion: ss.version
              }]
            };

        this.updateEntity(shipment,
                          'updateShipmentContainerOnSpecimens',
                          [ [ ss ], ss.shipmentContainerId],
                          uri('specimens/container', shipment.id),
                          reqJson,
                          jsonShipment,
                          this.expectShipment,
                          failTest,
                          false);
      });

      describe('can update the shipment specimen to PRESENT state', function() {

        var context = {};

        beforeEach(inject(function (ShipmentItemState) {
          context.stateChangeFuncName = 'tagSpecimensAsPresent';
          context.shipmentSepcimenState = ShipmentItemState.PRESENT;
        }));

        changeShipmentSpecimenStateSharedBehaviour(context);

      });

      describe('can update the shipment specimen to RECEIVED state', function() {

        var context = {};

        beforeEach(inject(function (ShipmentItemState) {
          context.stateChangeFuncName = 'tagSpecimensAsReceived';
          context.shipmentSepcimenState = ShipmentItemState.RECEIVED;
        }));

        changeShipmentSpecimenStateSharedBehaviour(context);

      });

      describe('can update the shipment specimen to MISSING state', function() {

        var context = {};

        beforeEach(inject(function (ShipmentItemState) {
          context.stateChangeFuncName = 'tagSpecimensAsMissing';
          context.shipmentSepcimenState = ShipmentItemState.MISSING;
        }));

         changeShipmentSpecimenStateSharedBehaviour(context);

      });

      describe('can update the shipment specimen to EXTRA state', function() {

        var context = {};

        beforeEach(inject(function (ShipmentItemState) {
          context.stateChangeFuncName = 'tagSpecimensAsExtra';
          context.shipmentSepcimenState = ShipmentItemState.EXTRA;
        }));

        changeShipmentSpecimenStateSharedBehaviour(context);

      });

    });

    it('can remove a shipment', function() {
        var self         = this,
            jsonShipment = self.factory.shipment(),
            shipment     = new self.Shipment(jsonShipment),
            url          = sprintf('%s/%d', uri(shipment.id), shipment.version);

      this.$httpBackend.expectDELETE(url).respond(this.reply(true));
      shipment.remove();
      this.$httpBackend.flush();
    });


    /**
     * @param {object} context - The configuration for this shared behaviour. See below.
     *
     * @param {string} context.stateChangeFuncName - the function to call to change the state.
     *
     * @param {string} context.state - the new state to change to.
     *
     * @param {Object} context.jsonShipment - A Json object representing the shipment.
     *
     * @param {Object} [context.stateChangeTime] - An optional time for when the state change happened.
     *
     * @param {domain.centres.Shipment} context.expectedShipment - The Shipment object that should be returned
     * from the update request.
     *
     * @returns {undefined}
     */
    function changeStateSharedBehaviour(context) {

      describe('shared state change behaviour', function () {

        it('can change state', function() {
          var updateParams = [],
          json    = {},
          shipment = new this.Shipment(context.jsonShipment);

          if (context.stateChangeTime) {
            updateParams.push(context.stateChangeTime);
            json = _.extend(json, { datetime: context.stateChangeTime });
          }

          this.updateEntity(shipment,
                            context.stateChangeFuncName,
                            updateParams,
                            uri('state/' + context.state, shipment.id ),
                            json,
                            context.jsonShipment,
                            context.expectShipment,
                            failTest);
        });

      });
    }

    /**
     * @param {object} context - The configuration for this shared behaviour. See below.
     *
     * @param {string} context.stateChangeFuncName - the function to call to change the state.
     *
     * @param {string} context.shipmentSepcimenState - the new state to change to.
     *
     * @returns {undefined}
     */
    function changeShipmentSpecimenStateSharedBehaviour(context) {

      it('can change state on a shipment specimen', function() {
        var self         = this,
            jsonSs       = self.factory.shipmentSpecimen(),
            ss           = new self.ShipmentSpecimen(jsonSs),
            jsonShipment = self.factory.shipment(),
            shipment     = new self.Shipment(jsonShipment),
            inventoryIds = [ ss.specimen.inventoryId ],
            reqJson      = { specimenInventoryIds: inventoryIds };

        expect(context.shipmentSepcimenState).toBeDefined();

        this.updateEntity(shipment,
                          context.stateChangeFuncName,
                          [ inventoryIds ],
                          uri('specimens/' + context.shipmentSepcimenState, shipment.id),
                          reqJson,
                          jsonShipment,
                          this.expectShipment,
                          failTest,
                          false);

      });

    }

    // used by promise tests
    function failTest(error) {
      expect(error).toBeUndefined();
    }

    function uri(/* path, shipmentId */) {
      var result = '/shipments/',
      args = _.toArray(arguments),
      shipmentId,
      path;

      if (args.length > 0) {
        path = args.shift();
        result += path;
      }

      if (args.length > 0) {
        shipmentId = args.shift();
        result += '/' + shipmentId;
      }

      return result;
    }
  });

});
