/**
 * Jasmine test suite
 *
 */
/* global angular */

import { TestSuiteMixin } from 'test/mixins/TestSuiteMixin';
import ngModule from '../../index'
import sharedBehaviour from 'test/behaviours/labelServiceSharedBehaviour';

describe('shipmentStateLabelService', function() {

  beforeEach(() => {
    angular.mock.module(ngModule, 'biobank.test');
    angular.mock.inject(function() {
      Object.assign(this, TestSuiteMixin);
      this.injectDependencies('shipmentStateLabelService',
                              'ShipmentState');
    });
  });

  describe('shared behaviour', function() {
    var context = {};

    beforeEach(function() {
      context.labels = Object.values(this.ShipmentState);
      context.toLabelFunc =
        this.shipmentStateLabelService.stateToLabelFunc.bind(this.shipmentStateLabelService);
      context.expectedLabels = [];
      Object.values(this.ShipmentState).forEach((state) => {
        context.expectedLabels[state] = this.capitalizeFirstLetter(state);
      });
    });
    sharedBehaviour(context);
  });

});
