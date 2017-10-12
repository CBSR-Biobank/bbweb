/**
 * Jasmine test suite
 *
 */
/* global angular */

import _ from 'lodash';

describe('createController', function() {

  beforeEach(() => {
    angular.mock.module('biobankApp', 'biobank.test');
    angular.mock.inject(function(ComponentTestSuiteMixin) {
      _.extend(this, ComponentTestSuiteMixin);

      this.injectDependencies('$q',
                              '$rootScope',
                              '$compile',
                              'Centre',
                              'Shipment',
                              'ShipmentState',
                              'Factory');

      this.createController = (centre) => {
        centre = centre || this.centre;

        if (!centre) {
          throw new Error('no centre to create component with');
        }

        ComponentTestSuiteMixin.createController.call(
          this,
          '<centre-shipments centre="vm.centre"></centre-shipments',
          { centre:  centre },
          'centreShipments');
      };
    });
  });

  it('should have valid scope', function() {
    this.createController(new this.Centre(this.Factory.centre()));
    expect(this.controller.tabs).toBeNonEmptyArray();
  });

});
