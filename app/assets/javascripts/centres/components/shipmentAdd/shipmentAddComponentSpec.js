/**
 * Jasmine test suite
 *
 */
/* global angular */

import { ComponentTestSuiteMixin } from 'test/mixins/ComponentTestSuiteMixin';
import _ from 'lodash';
import ngModule from '../../index'

describe('shipmentAddComponent', function() {

  beforeEach(() => {
    angular.mock.module(ngModule, 'biobank.test');
    angular.mock.inject(function(TestUtils) {
      Object.assign(this, ComponentTestSuiteMixin);

      this.injectDependencies('$q',
                              '$rootScope',
                              '$compile',
                              '$state',
                              'Centre',
                              'Shipment',
                              'ShipmentState',
                              'shipmentSendTasksService',
                              'domainNotificationService',
                              'Factory');

      TestUtils.addCustomMatchers();
      this.createController = () =>
        ComponentTestSuiteMixin.createController.call(
          this,
          '<shipment-add centre="vm.centre"></shipment-add',
          undefined,
          'shipmentAdd');

      this.createPagedResultsSpy = (shipments) => {
        var reply = this.Factory.pagedResult(shipments);
        spyOn(this.Shipment, 'list').and.returnValue(this.$q.when(reply));
      };

      this.createCentreLocationsSpy = (locations) => {
        var locationsCopy = locations.slice();
        spyOn(this.Centre, 'locationsSearch').and.returnValue(this.$q.when(locationsCopy));
      };

      this.centreLocations = (numCentres) => {
        var self = this;
        return _.range(numCentres).map(() => {
          var centre = self.Factory.centre({ locations: [ self.Factory.location() ]});
          return self.Factory.centreLocationDto(centre);
        });
      };
    });
  });

  it('should have valid scope', function() {
    this.createCentreLocationsSpy([]);
    this.createController();
    expect(this.controller.progressInfo).toBeDefined();

    const taskData = this.shipmentSendTasksService.getTaskData();
    taskData[0].status = true;
    expect(this.controller.progressInfo).toBeArrayOfSize(Object.keys(taskData).length);
    taskData.forEach(taskInfo => {
      expect(this.controller.progressInfo).toContain(taskInfo);
    });

    expect(this.controller.hasValidCentres).toBeFalse();
    expect(this.controller.shipment).toEqual(jasmine.any(this.Shipment));
    expect(this.controller.submit).toBeFunction();
    expect(this.controller.cancel).toBeFunction();
  });

  it('checks for valid centres', function() {
    var centreLocs = this.centreLocations(2);
    this.createCentreLocationsSpy(centreLocs);
    this.createController();
    expect(this.controller.hasValidCentres).toBeTrue();
  });

  it('on submit button pressed should go to next state', function() {
    var shipment = new this.Shipment(this.Factory.shipment());

    spyOn(this.$state, 'go').and.returnValue(null);
    spyOn(this.Shipment.prototype, 'add').and.returnValue(this.$q.when(shipment));

    this.createCentreLocationsSpy([]);
    this.createController();
    this.controller.submit();
    this.scope.$digest();
    expect(this.$state.go).toHaveBeenCalledWith('home.shipping.addItems', { shipmentId: shipment.id});
  });

  it('submit button pressed and shipment could not be added', function() {
    spyOn(this.domainNotificationService, 'updateErrorModal').and.returnValue(this.$q.when('OK'));
    this.createCentreLocationsSpy([]);

    this.createController();
    spyOn(this.Shipment.prototype, 'add').and.returnValue(this.$q.reject('simulated error'));
    this.controller.submit();
    this.scope.$digest();
    expect(this.domainNotificationService.updateErrorModal).toHaveBeenCalled();
  });

  it('when cancel button is pressed', function() {
    spyOn(this.$state, 'go').and.returnValue(null);
    this.createCentreLocationsSpy([]);
    this.createController();
    this.controller.cancel();
    this.scope.$digest();
    expect(this.$state.go).toHaveBeenCalledWith('home.shipping');
  });

  it('to centres omits selected FROM centre', function() {
    var centreLocs = this.centreLocations(2);

    this.createCentreLocationsSpy(centreLocs);
    this.createController();
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
    this.createController();
    this.controller.getToCentreLocationInfo().then(function (toLocationInfos) {
      expect(toLocationInfos).toBeArrayOfSize(2);
    });
    this.scope.$digest();
  });

  it('from centres omits selected TO centre', function() {
    var centreLocs = this.centreLocations(2);

    this.createCentreLocationsSpy(centreLocs);
    this.createController();
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
    this.createController();
    this.controller.getFromCentreLocationInfo().then(function (fromLocationInfos) {
      expect(fromLocationInfos).toBeArrayOfSize(2);
    });
    this.scope.$digest();
  });

});
