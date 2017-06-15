/**
 * Jasmine test suite
 *
 */
define(function (require) {
  'use strict';

  /*eslint no-unused-vars: ["error", { "varsIgnorePattern": "angular" }]*/

  var angular         = require('angular'),
      mocks           = require('angularMocks'),
      _               = require('lodash');

  function SuiteMixinFactory(TestSuiteMixin) {

    function SuiteMixin() {
    }

    SuiteMixin.prototype = Object.create(TestSuiteMixin.prototype);
    SuiteMixin.prototype.constructor = SuiteMixin;

    SuiteMixin.prototype.createScope = function () {
      this.element = angular.element('<shipment-add centre="vm.centre"></shipment-add');
      this.scope = this.$rootScope.$new();
      this.$compile(this.element)(this.scope);
      this.scope.$digest();
      this.controller = this.element.controller('shipmentAdd');
    };

    SuiteMixin.prototype.createPagedResultsSpy = function (shipments) {
      var reply = this.factory.pagedResult(shipments);
      spyOn(this.Shipment, 'list').and.returnValue(this.$q.when(reply));
    };

    SuiteMixin.prototype.createCentreLocationsSpy = function (locations) {
      var locationsCopy = locations.slice();
      spyOn(this.Centre, 'locationsSearch').and.returnValue(this.$q.when(locationsCopy));
    };

    SuiteMixin.prototype.centreLocations = function (numCentres) {
      var self = this;
      return _.map(_.range(numCentres), function () {
        var centre = self.factory.centre({ locations: [ self.factory.location() ]});
        return self.factory.centreLocationDto(centre);
      });
    };

    return SuiteMixin;
  }

  describe('shipmentAddComponent', function() {

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function(TestSuiteMixin, testUtils) {
      var SuiteMixin = new SuiteMixinFactory(TestSuiteMixin);
      _.extend(this, SuiteMixin.prototype);
      this.putHtmlTemplates(
        '/assets/javascripts/centres/components/shipmentAdd/shipmentAdd.html',
        '/assets/javascripts/common/components/progressTracker/progressTracker.html',
        '/assets/javascripts/common/components/breadcrumbs/breadcrumbs.html');

      this.injectDependencies('$q',
                              '$rootScope',
                              '$compile',
                              '$state',
                              'Centre',
                              'Shipment',
                              'ShipmentState',
                              'SHIPMENT_SEND_PROGRESS_ITEMS',
                              'domainNotificationService',
                              'factory');

      testUtils.addCustomMatchers();
    }));

    it('should have valid scope', function() {
      this.createCentreLocationsSpy([]);
      this.createScope();
      expect(this.controller.progressInfo).toBeDefined();
      expect(this.controller.progressInfo.items).toBeArrayOfSize(this.SHIPMENT_SEND_PROGRESS_ITEMS.length);
      expect(this.controller.progressInfo.items).toContainAll(this.SHIPMENT_SEND_PROGRESS_ITEMS);
      expect(this.controller.progressInfo.current).toBe(1);
      expect(this.controller.hasValidCentres).toBeFalse();
      expect(this.controller.shipment).toEqual(jasmine.any(this.Shipment));
      expect(this.controller.submit).toBeFunction();
      expect(this.controller.cancel).toBeFunction();
    });

    it('checks for valid centres', function() {
      var centreLocs = this.centreLocations(2);
      this.createCentreLocationsSpy(centreLocs);
      this.createScope();
      expect(this.controller.hasValidCentres).toBeTrue();
    });

    it('on submit button pressed should go to next state', function() {
      var shipment = new this.Shipment(this.factory.shipment());

      spyOn(this.$state, 'go').and.returnValue(null);
      spyOn(this.Shipment.prototype, 'add').and.returnValue(this.$q.when(shipment));

      this.createCentreLocationsSpy([]);
      this.createScope();
      this.controller.submit();
      this.scope.$digest();
      expect(this.$state.go).toHaveBeenCalledWith('home.shipping.addItems', { shipmentId: shipment.id});
    });

    it('submit button pressed and shipment could not be added', function() {
      spyOn(this.Shipment.prototype, 'add').and.returnValue(this.$q.reject('simulated error'));
      spyOn(this.domainNotificationService, 'updateErrorModal').and.returnValue(this.$q.when('OK'));
      this.createCentreLocationsSpy([]);
      this.createScope();
      this.controller.submit();
      this.scope.$digest();
      expect(this.domainNotificationService.updateErrorModal).toHaveBeenCalled();
    });

    it('when cancel button is pressed', function() {
      spyOn(this.$state, 'go').and.returnValue(null);
      this.createCentreLocationsSpy([]);
      this.createScope();
      this.controller.cancel();
      this.scope.$digest();
      expect(this.$state.go).toHaveBeenCalledWith('home.shipping');
    });

    it('to centres omits selected FROM centre', function() {
      var centreLocs = this.centreLocations(2);

      this.createCentreLocationsSpy(centreLocs);
      this.createScope();
      this.controller.shipment.fromLocationInfo = centreLocs[0];
      this.controller.getToCentreLocationInfo().then(function (toLocationInfos) {
        expect(toLocationInfos).toBeArrayOfSize(1);
        expect(toLocationInfos[0]).toBe(centreLocs[1]);
      });
      this.scope.$digest();
    });

    it('to centres does not omit if FROM centre not selected', function() {
      var centreLocs = this.centreLocations(2);

      this.createCentreLocationsSpy(centreLocs);
      this.createScope();
      this.controller.getToCentreLocationInfo().then(function (toLocationInfos) {
        expect(toLocationInfos).toBeArrayOfSize(2);
      });
      this.scope.$digest();
    });

    it('from centres omits selected TO centre', function() {
      var centreLocs = this.centreLocations(2);

      this.createCentreLocationsSpy(centreLocs);
      this.createScope();
      this.controller.shipment.toLocationInfo = centreLocs[0];
      this.controller.getFromCentreLocationInfo().then(function (fromLocationInfos) {
        expect(fromLocationInfos).toBeArrayOfSize(1);
        expect(fromLocationInfos[0]).toBe(centreLocs[1]);
      });
      this.scope.$digest();
    });

    it('from centres does not omit if TO centre not selected', function() {
      var centreLocs = this.centreLocations(2);

      this.createCentreLocationsSpy(centreLocs);
      this.createScope();
      this.controller.getFromCentreLocationInfo().then(function (fromLocationInfos) {
        expect(fromLocationInfos).toBeArrayOfSize(2);
      });
      this.scope.$digest();
    });

  });

});
