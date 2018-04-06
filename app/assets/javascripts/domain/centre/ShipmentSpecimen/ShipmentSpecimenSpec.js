/*
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */
/* global angular */

import { EntityTestSuiteMixin } from 'test/mixins/EntityTestSuiteMixin';
import { ServerReplyMixin } from 'test/mixins/ServerReplyMixin';
import _ from 'lodash';
import ngModule from '../../index'

describe('ShipmentSpecimen domain object:', function() {

  beforeEach(() => {
    angular.mock.module(ngModule, 'biobank.test');
    angular.mock.inject(function () {
      Object.assign(this, EntityTestSuiteMixin, ServerReplyMixin);

      this.injectDependencies('$httpBackend',
                              '$httpParamSerializer',
                              'ShipmentSpecimen',
                              'ShipmentItemState',
                              'TestUtils',
                              'Factory');
      // used by promise tests
      this.expectShipmentSpecimen = (entity) => {
        expect(entity).toEqual(jasmine.any(this.ShipmentSpecimen));
      };

      this.url = (...paths) => {
        const args = [ 'shipments' ].concat(paths);
        return EntityTestSuiteMixin.url(...args);
      }
    });
  });

  afterEach(function() {
    this.$httpBackend.verifyNoOutstandingExpectation();
    this.$httpBackend.verifyNoOutstandingRequest();
  });

  describe('for constructor', function() {

    it('constructor with no parameters has default values', function() {
      var ss = new this.ShipmentSpecimen();

      expect(ss.id).toBeNull();
      expect(ss.version).toBe(0);
      expect(ss.timeAdded).toBeUndefined();
      expect(ss.timeModified).toBeUndefined();
      expect(ss.state).toBe(this.ShipmentItemState.PRESENT);
      expect(ss.shipmentId).toBeNull();
      expect(ss.specimen).toBeNull();
    });

    it('fails when creating from a non object', function() {
      expect(() => { this.ShipmentSpecimen.create('test'); })
        .toThrowError(/invalid object from server/);
    });

    it('fails when creating from a bad shipment ID', function() {
      var badJson = this.Factory.shipmentSpecimen({ shipmentId: undefined });

      expect(() => { this.ShipmentSpecimen.create(badJson); })
        .toThrowError(/invalid object from server.*shipmentId/);
    });

    it('fails when creating from a bad specimen', function() {
      var specimen = this.Factory.specimen(),
          badJson = this.Factory.shipmentSpecimen({ specimen: _.omit(specimen, 'originLocationInfo') });

      expect(() => {
        this.ShipmentSpecimen.create(badJson);
      }).toThrowError(/invalid object from server.*originLocationInfo/);
    });

    it('fails when creating from a bad location ID', function() {
      var specimen = this.Factory.specimen(),
          badJson = this.Factory.shipmentSpecimen({ specimen: _.omit(specimen, 'locationInfo') });

      expect(() => { this.ShipmentSpecimen.create(badJson); })
        .toThrowError(/invalid object from server.*locationInfo/);
    });

    it('fails when creating from a bad state', function() {
      var badJson = this.Factory.shipmentSpecimen({ state: undefined });

      expect(() => { this.ShipmentSpecimen.create(badJson); })
        .toThrowError(/invalid object from server.*state/);
    });

  });

  describe('when getting a single shipment', function() {

    it('can retrieve a single shipment specimen', function() {
      var ss = this.Factory.shipmentSpecimen(),
          checkReply = (reply) => {
            expect(reply).toEqual(jasmine.any(this.ShipmentSpecimen));
          };

      this.$httpBackend.whenGET(this.url('specimens', ss.id)).respond(this.reply(ss));
      this.ShipmentSpecimen.get(ss.id).then(checkReply).catch(failTest);
      this.$httpBackend.flush();
    });

    it('fails when getting a shipment specimen and it has a missing required field', function() {
      var requiredProperties = [ 'version',
                                 'state',
                                 'shipmentId',
                                 'specimen'
                               ];

      requiredProperties.forEach((property) => {
        var ss = _.omit(this.Factory.shipmentSpecimen(), property);

        this.$httpBackend.whenGET(this.url('specimens', ss.id)).respond(this.reply(ss));
        this.ShipmentSpecimen.get(ss.id).then(shouldNotFail).catch(shouldFail);
        this.$httpBackend.flush();

        function shouldFail(error) {
          expect(error).toMatch('invalid object from server.*' + property);
        }
      });

      function shouldNotFail() {
        fail('function should not be called');
      }
    });

  });

  describe('when listing shipment specimens', function() {

    it('can retrieve shipment specimens', function() {
      var ssArray = [ this.Factory.shipmentSpecimen() ],
          shipmentId = ssArray[0].shipmentId,
          reply = this.Factory.pagedResult(ssArray),
          checkReply = (pagedResult) => {
            expect(pagedResult.items).toBeArrayOfSize(ssArray.length);
            pagedResult.items.forEach((item) => {
              expect(item).toEqual(jasmine.any(this.ShipmentSpecimen));
            });
          };

      this.$httpBackend.whenGET(this.url('specimens', shipmentId)).respond(this.reply(reply));
      this.ShipmentSpecimen.list(shipmentId).then(checkReply).catch(failTest);
      this.$httpBackend.flush();
    });

    it('can list shipment specimens using options', function() {
      var optionList = [
            { sort: 'inventoryId' },
            { page: 2 },
            { limit: 10 },
            { order: 'desc' }
          ];

      optionList.forEach((options) => {
        var ssArray    = [ this.Factory.shipmentSpecimen() ],
            shipmentId = ssArray[0].shipmentId,
            reply      = this.Factory.pagedResult(ssArray),
            url        = this.url('specimens', shipmentId) + '?' + this.$httpParamSerializer(options),
            testShipmentSpecimens = (pagedResult) => {
              expect(pagedResult.items).toBeArrayOfSize(ssArray.length);
              pagedResult.items.forEach((study) => {
                expect(study).toEqual(jasmine.any(this.ShipmentSpecimen));
              });
            };

        this.$httpBackend.whenGET(url).respond(this.reply(reply));
        this.ShipmentSpecimen.list(shipmentId, options).then(testShipmentSpecimens).catch(failTest);
        this.$httpBackend.flush();
      });
    });

    it('fails when list returns an invalid shipment', function() {
      var ssArray    = [ _.omit(this.Factory.shipmentSpecimen(), 'state') ],
          shipmentId = ssArray[0].shipmentId,
          reply      = this.Factory.pagedResult(ssArray);

      this.$httpBackend.whenGET(this.url('specimens', shipmentId)).respond(this.reply(reply));
      this.ShipmentSpecimen.list(shipmentId).then(listFail).catch(shouldFail);
      this.$httpBackend.flush();

      function listFail() {
        fail('function should not be called');
      }

      function shouldFail(error) {
        expect(error).toStartWith('invalid shipment specimens from server');
      }
    });

  });

  // used by promise tests
  function failTest(error) {
    expect(error).toBeUndefined();
  }

});
