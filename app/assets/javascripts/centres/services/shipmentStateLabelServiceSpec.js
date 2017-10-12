/**
 * Jasmine test suite
 *
 */
/* global angular */

import _ from 'lodash';
import sharedBehaviour from '../../test/behaviours/labelServiceSharedBehaviour';

describe('shipmentStateLabelService', function() {

  beforeEach(() => {
    angular.mock.module('biobankApp', 'biobank.test');
    angular.mock.inject(function(TestSuiteMixin) {
      _.extend(this, TestSuiteMixin);
      this.injectDependencies('shipmentStateLabelService',
                              'ShipmentState');
    });
  });

  describe('shared behaviour', function() {
    var context = {};

    beforeEach(function() {
      var self = this;

      context.labels = _.values(this.ShipmentState);
      context.toLabelFunc = this.shipmentStateLabelService.stateToLabelFunc;
      context.expectedLabels = [];
      _.values(this.ShipmentState).forEach(function (state) {
        context.expectedLabels[state] = self.capitalizeFirstLetter(state);
      });
    });
    sharedBehaviour(context);
  });

});
