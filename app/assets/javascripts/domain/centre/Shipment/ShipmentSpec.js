/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
/* global angular, inject */

import _      from 'lodash';
import faker  from 'faker';
import moment from 'moment';
import ngModule from '../../index'

/**
 * Test suite for shipment domain entity.
 */
describe('Shipment domain object:', function() {

  beforeEach(() => {
    angular.mock.module(ngModule, 'biobank.test');
    angular.mock.inject(function (ServerReplyMixin, EntityTestSuiteMixin) {
      _.extend(this, EntityTestSuiteMixin, ServerReplyMixin);

      this.injectDependencies('$httpBackend',
                              '$httpParamSerializer',
                              'Shipment',
                              'ShipmentSpecimen',
                              'ShipmentState',
                              'Specimen',
                              'TestUtils',
                              'Factory');
      // used by promise tests
      this.expectShipment = (entity) => {
        expect(entity).toEqual(jasmine.any(this.Shipment));
      };

      this.url = url;

      //---

      function url() {
        const args = [ 'shipments' ].concat(_.toArray(arguments));
        return EntityTestSuiteMixin.url.apply(null, args);
      }
    });
  });

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
      var badJson = _.omit(this.Factory.shipment(), 'courierName');

      expect(() => { this.Shipment.create(badJson); })
        .toThrowError(/invalid object from server/);
    });

    it('fails when creating from a bad from location ID', function() {
      var badJson = this.Factory.shipment({ fromLocationInfo: undefined });

      expect(() => this.Shipment.create(badJson))
        .toThrowError(/invalid object from server.*fromLocationInfo/);
    });

    it('fails when creating from a bad to location ID', function() {
      var badJson = this.Factory.shipment({ toLocationInfo: undefined });

      expect(() => this.Shipment.create(badJson))
        .toThrowError(/invalid object from server.*toLocationInfo/);
    });

  });

  describe('when getting a single shipment', function() {

    function shouldNotFail() {
      fail('function should not be called');
    }

    function shouldFail(error) {
      expect(error.message).toContain('Missing required property');
    }

    it('can retrieve a single shipment', function() {
      var shipment = this.Factory.shipment(),
          checkReply = (reply) => {
            expect(reply).toEqual(jasmine.any(this.Shipment));
          };

      this.$httpBackend.whenGET(this.url(shipment.id)).respond(this.reply(shipment));
      this.Shipment.get(shipment.id).then(checkReply).catch(failTest);
      this.$httpBackend.flush();
    });

    it('fails when getting a shipment and it has a bad format', function() {
      var shipment = _.omit(this.Factory.shipment(), 'courierName');

      this.$httpBackend.whenGET(this.url(shipment.id)).respond(this.reply(shipment));
      this.Shipment.get(shipment.id).then(shouldNotFail).catch(shouldFail);
      this.$httpBackend.flush();
    });

    it('fails when getting a shipment and it has a bad from location', function() {
      var shipment = _.omit(this.Factory.shipment(), 'fromLocationInfo');

      this.$httpBackend.whenGET(this.url(shipment.id)).respond(this.reply(shipment));
      this.Shipment.get(shipment.id).then(shouldNotFail).catch(shouldFail);
      this.$httpBackend.flush();
    });

    it('fails when getting a shipment and it has a bad to location', function() {
      var shipment = _.omit(this.Factory.shipment(), 'toLocationInfo');

      this.$httpBackend.whenGET(this.url(shipment.id)).respond(this.reply(shipment));
      this.Shipment.get(shipment.id).then(shouldNotFail).catch(shouldFail);
      this.$httpBackend.flush();
    });

    it('throws an exception if id is falsy', function() {
      expect(() => {
        this.Shipment.get();
      }).toThrowError(/shipment id not specified/);
    });

  });

  describe('when listing shipments', function() {

    it('can retrieve shipments', function() {
      var shipment     = this.Factory.shipment(),
          shipments    = [ shipment ],
          reply        = this.Factory.pagedResult(shipments),
          checkReply = (pagedResult) => {
            expect(pagedResult.items).toBeArrayOfSize(shipments.length);
            pagedResult.items.forEach((item) => {
              expect(item).toEqual(jasmine.any(this.Shipment));
            });
          };

      this.$httpBackend.whenGET(this.url('list')).respond(this.reply(reply));
      this.Shipment.list().then(checkReply).catch(failTest);
      this.$httpBackend.flush();
    });

    it('can use options', function() {
      var centre = this.Factory.centre(),
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

      optionList.forEach((options) => {
        var shipments = [ this.Factory.shipment() ],
            reply     = this.Factory.pagedResult(shipments),
            testShipment = (pagedResult) => {
              expect(pagedResult.items).toBeArrayOfSize(shipments.length);
              pagedResult.items.forEach((study) => {
                expect(study).toEqual(jasmine.any(this.Shipment));
              });
            },
            url = this.url('list') + '?' + this.$httpParamSerializer(options);
        this.$httpBackend.whenGET(url).respond(this.reply(reply));
        this.Shipment.list(options).then(testShipment).catch(failTest);
        this.$httpBackend.flush();
      });
    });

    it('fails when list returns an invalid shipment', function() {
      var shipments = [ _.omit(this.Factory.shipment(), 'courierName') ],
          reply = this.Factory.pagedResult(shipments);

      this.$httpBackend.whenGET(this.url('list')).respond(this.reply(reply));
      this.Shipment.list().then(listFail).catch(shouldFail);
      this.$httpBackend.flush();
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
      var jsonShipment = this.Factory.shipment(),
          shipment = new this.Shipment(_.omit(jsonShipment, 'id')),
          json = _.extend(_.pick(shipment, 'courierName', 'trackingNumber'),
                          {
                            fromLocationId: shipment.fromLocationInfo.locationId,
                            toLocationId:   shipment.toLocationInfo.locationId
                          }),
          checkReply = (replyShipment) => {
            expect(replyShipment).toEqual(jasmine.any(this.Shipment));
          };

      this.$httpBackend.expectPOST(this.url(''), json).respond(this.reply(jsonShipment));
      shipment.add().then(checkReply).catch(failTest);
      this.$httpBackend.flush();
    });

  });

  describe('when updating a shipment', function() {

    it('can update the courier name on a shipment', function() {
      var jsonShipment = this.Factory.shipment(),
          shipment     = new this.Shipment(jsonShipment);

      this.updateEntity(shipment,
                        'updateCourierName',
                        shipment.courierName,
                        this.url('courier', shipment.id),
                        { courierName: shipment.courierName },
                        jsonShipment,
                        this.expectShipment,
                        failTest);
    });

    it('can update the tracking number on a shipment', function() {
      var jsonShipment = this.Factory.shipment(),
          shipment     = new this.Shipment(jsonShipment);

      this.updateEntity(shipment,
                        'updateTrackingNumber',
                        shipment.trackingNumber,
                        this.url('trackingnumber', shipment.id),
                        { trackingNumber: shipment.trackingNumber },
                        jsonShipment,
                        this.expectShipment,
                        failTest);
    });

    it('can update the FROM location on a shipment', function() {
      var jsonShipment = this.Factory.shipment(),
          shipment     = new this.Shipment(jsonShipment);

      this.updateEntity(shipment,
                        'updateFromLocation',
                        shipment.fromLocationId,
                        this.url('fromlocation', shipment.id),
                        { locationId: shipment.fromLocationId },
                        jsonShipment,
                        this.expectShipment,
                        failTest);
    });

    it('can update the TO location on a shipment', function() {
      var jsonShipment = this.Factory.shipment(),
          shipment     = new this.Shipment(jsonShipment);

      this.updateEntity(shipment,
                        'updateToLocation',
                        shipment.toLocationId,
                        this.url('tolocation', shipment.id),
                        { locationId: shipment.toLocationId },
                        jsonShipment,
                        this.expectShipment,
                        failTest);
    });

    describe('can change state on a shipment', function() {

      var context = {};

      beforeEach(function () {
        context.jsonShipment = this.Factory.shipment();
        context.expectedShipment = this.expectShipment;
        context.stateChangeTime = undefined;
      });

      describe('to created state', function() {

        beforeEach(function () {
          context.stateChangeFuncName = 'created';
          context.state = this.ShipmentState.CREATED;
        });

        changeStateSharedBehaviour(context);

      });

      describe('to packed state', function() {

        beforeEach(function () {
          context.stateChangeFuncName = 'pack';
          context.state = this.ShipmentState.PACKED;
          context.stateChangeTime = moment(faker.date.recent(10)).format();
        });

        changeStateSharedBehaviour(context);

      });

      describe('to sent state', function() {

        beforeEach(function () {
          context.stateChangeFuncName = 'send';
          context.state = this.ShipmentState.SENT;
          context.stateChangeTime = moment(faker.date.recent(10)).format();
        });

        changeStateSharedBehaviour(context);

      });

      describe('to received state', function() {

        beforeEach(function () {
          context.stateChangeFuncName = 'receive';
          context.state = this.ShipmentState.RECEIVED;
          context.stateChangeTime = moment(faker.date.recent(10)).format();
        });

        changeStateSharedBehaviour(context);

      });

      describe('to unpacked state', function() {

        beforeEach(function () {
          context.stateChangeFuncName = 'unpack';
          context.state = this.ShipmentState.UNPACKED;
          context.stateChangeTime = moment(faker.date.recent(10)).format();
        });

        changeStateSharedBehaviour(context);

      });

      describe('to completed state', function() {

        beforeEach(function () {
          context.stateChangeFuncName = 'complete';
          context.state = this.ShipmentState.COMPLETED;
          context.stateChangeTime = moment(faker.date.recent(10)).format();
        });

        changeStateSharedBehaviour(context);

      });

      describe('to lost state', function() {

        beforeEach(function () {
          context.stateChangeFuncName = 'lost';
          context.state = this.ShipmentState.LOST;
        });

        changeStateSharedBehaviour(context);

      });

    });

    describe('can change state on a shipment', function() {

      beforeEach(function() {
        this.jsonShipment = this.Factory.shipment();
        this.time         = moment(faker.date.recent(10)).format();
        this.shipment     = new this.Shipment(this.jsonShipment);
      });

      it('can skip state to SENT', function() {
        this.updateEntity(this.shipment,
                          'skipToStateSent',
                          [ this.time, this.time ],
                          this.url('state/skip-to-sent', this.shipment.id ),
                          { timePacked: this.time, timeSent: this.time },
                          this.jsonShipment,
                          this.expectShipment,
                          failTest);
      });

      it('can skip state to UNPACKED', function() {
        this.updateEntity(this.shipment,
                          'skipToStateUnpacked',
                          [ this.time, this.time ],
                          this.url('state/skip-to-unpacked', this.shipment.id ),
                          { timeReceived: this.time, timeUnpacked: this.time },
                          this.jsonShipment,
                          this.expectShipment,
                          failTest);
      });


    });

  });

  describe('state predicates', function() {

    it('for CREATED state predicate', function() {
      var shipment = new this.Shipment(this.Factory.shipment({ state: this.ShipmentState.CREATED }));
      expect(shipment.isCreated()).toBeTrue();
    });

    it('for PACKED state predicate', function() {
      var shipment = new this.Shipment(this.Factory.shipment({ state: this.ShipmentState.PACKED }));
      expect(shipment.isPacked()).toBeTrue();
    });

    it('for SENT state predicate', function() {
      var shipment = new this.Shipment(this.Factory.shipment({ state: this.ShipmentState.SENT }));
      expect(shipment.isSent()).toBeTrue();
    });

    it('for UNPACKED state predicate', function() {
      var shipment = new this.Shipment(this.Factory.shipment({ state: this.ShipmentState.UNPACKED }));
      expect(shipment.isUnpacked()).toBeTrue();
    });

    it('for not CREATED nor UNPACKED predicate', function() {
      [
        this.ShipmentState.PACKED,
        this.ShipmentState.SENT,
        this.ShipmentState.RECEIVED,
        this.ShipmentState.LOST,
      ].forEach((state) => {
        var shipment = new this.Shipment(this.Factory.shipment({ state: state }));
        expect(shipment.isNotCreatedNorUnpacked()).toBeTrue();
      });
    });

  });

  describe('for canAddInventoryId', function() {

    it('can add specimen', function() {
      var jsonSpecimen = this.Factory.specimen(),
          shipment = new this.Shipment(this.Factory.shipment()),
          inventoryId = this.Factory.stringNext(),
          checkReply = (reply) => {
            expect(reply).toEqual(jasmine.any(this.Specimen));
          };

      this.$httpBackend.whenGET(this.url('specimens/canadd', shipment.id) + '/' + inventoryId)
        .respond(this.reply(jsonSpecimen));

      shipment.canAddInventoryId(inventoryId)
        .then(checkReply)
        .catch(failTest);
      this.$httpBackend.flush();
    });

    it('throws an exception if id is falsy', function() {
      var shipment = new this.Shipment(this.Factory.shipment());

      expect(() => {
        shipment.canAddInventoryId();
      }).toThrowError(/specimen inventory id not specified/);
    });

  });

  describe('when adding shipment specimens', function() {

    it('can add a shipment', function() {
      var jsonSpecimen = this.Factory.specimen(),
          jsonShipment = this.Factory.shipment(),
          shipment = new this.Shipment(jsonShipment),
          checkReply = (reply) => {
            expect(reply).toEqual(jasmine.any(this.Shipment));
          };

      this.$httpBackend.expectPOST(this.url('specimens', shipment.id)).respond(this.reply(jsonShipment));
      shipment.addSpecimens([ jsonSpecimen.id ]).then(checkReply).catch(failTest);
      this.$httpBackend.flush();
    });

    xit('can add specimens in containers to shipments', () => {
      fail('needs to be implemented');
    });

    it('addSpecimens throws an exception if id is falsy', function() {
      var shipment = new this.Shipment(this.Factory.shipment());

      expect(() => {
        shipment.addSpecimens();
      }).toThrowError(/specimenInventoryIds should be an array/);
    });

  });

  describe('when updating a shipment specimen', function() {

    it('can update the shipment container on a shipment specimen', function() {
      var jsonSs       = this.Factory.shipmentSpecimen(),
          ss           = new this.ShipmentSpecimen(jsonSs),
          jsonShipment = this.Factory.shipment(),
          shipment     = new this.Shipment(jsonShipment),
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
                        this.url('specimens/container', shipment.id),
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
    var jsonShipment = this.Factory.shipment(),
        shipment     = new this.Shipment(jsonShipment),
        url          = this.url(shipment.id, shipment.version);

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
                          this.url('state/' + context.state, shipment.id ),
                          json,
                          context.jsonShipment,
                          this.expectShipment,
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
      var jsonSs       = this.Factory.shipmentSpecimen(),
          ss           = new this.ShipmentSpecimen(jsonSs),
          jsonShipment = this.Factory.shipment(),
          shipment     = new this.Shipment(jsonShipment),
          inventoryIds = [ ss.specimen.inventoryId ],
          reqJson      = { specimenInventoryIds: inventoryIds };

      expect(context.shipmentSepcimenState).toBeDefined();

      this.updateEntity(shipment,
                        context.stateChangeFuncName,
                        [ inventoryIds ],
                        this.url('specimens/' + context.shipmentSepcimenState, shipment.id),
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

});
