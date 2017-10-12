/**
 * Jasmine test suite
 *
 */
/* global angular */

import _ from 'lodash';

describe('unpackedShipmentInfoComponent', function() {

  beforeEach(() => {
    angular.mock.module('biobankApp', 'biobank.test');
    angular.mock.inject(function(ShippingComponentTestSuiteMixin, ServerReplyMixin) {
      _.extend(this, ShippingComponentTestSuiteMixin, ServerReplyMixin);

      this.injectDependencies('$q',
                              '$rootScope',
                              '$compile',
                              'Factory');

      this.createController = (shipment) =>
        ShippingComponentTestSuiteMixin.createController.call(
          this,
          '<unpacked-shipment-info shipment="vm.shipment"><unpacked-shipment-info>',
          { shipment: shipment },
          'unpackedShipmentInfo');
    });
  });

  it('emits event when created', function() {
    var shipment = this.createShipment(),
        eventEmitted = false;

    this.$rootScope.$on('tabbed-page-update', function (event, arg) {
      expect(arg).toBe('tab-selected');
      eventEmitted = true;
    });

    this.createController(shipment);
    expect(eventEmitted).toBeTrue();
  });

});
