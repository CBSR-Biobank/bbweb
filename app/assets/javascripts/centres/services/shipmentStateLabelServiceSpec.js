/**
 * Jasmine test suite
 *
 */
define(function (require) {
  'use strict';

  var mocks = require('angularMocks'),
      _     = require('lodash'),
      sharedBehaviour = require('../../test/labelServiceSharedBehaviour');

  describe('shipmentStateLabelService', function() {

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function(TestSuiteMixin) {
      _.extend(this, TestSuiteMixin.prototype);
      this.injectDependencies('shipmentStateLabelService',
                              'ShipmentState');
    }));

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

});
