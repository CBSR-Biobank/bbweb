/**
 * Jasmine test suite
 *
 */
define(function (require) {
  'use strict';

  var mocks = require('angularMocks'),
      _     = require('lodash');

  describe('shipmentStateLabelService', function() {

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function(TestSuiteMixin) {
      _.extend(this, TestSuiteMixin.prototype);
      this.injectDependencies('shipmentStateLabelService',
                              'ShipmentState');
    }));

    it('has valid values', function() {
      var self = this;
      _.values(this.ShipmentState).forEach(function (state) {
        expect(self.shipmentStateLabelService.stateToLabel(state)).toBe(self.capitalizeFirstLetter(state));
      });
    });

    it('throws error when invalid state is used', function() {
      var self = this;
      this.injectDependencies('factory');

      expect(function () {
        self.shipmentStateLabelService.stateToLabel(self.factory.stringNext());
      }).toThrowError(/invalid shipment state/);
    });

  });

});
