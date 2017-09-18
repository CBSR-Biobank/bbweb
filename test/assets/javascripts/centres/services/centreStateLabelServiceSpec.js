/**
 * Jasmine test suite
 *
 */
define(function (require) {
  'use strict';

  var mocks = require('angularMocks'),
      _     = require('lodash'),
      sharedBehaviour = require('../../test/labelServiceSharedBehaviour');

  describe('centreStateLabelService', function() {

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function(TestSuiteMixin) {
      _.extend(this, TestSuiteMixin.prototype);
      this.injectDependencies('centreStateLabelService',
                              'CentreState');
    }));

    describe('shared behaviour', function() {
      var context = {};
      beforeEach(function() {
        var self = this;

        context.labels = _.values(this.CentreState);
        context.toLabelFunc = this.centreStateLabelService.stateToLabelFunc;
        context.expectedLabels = [];
        _.values(this.CentreState).forEach(function (state) {
          context.expectedLabels[state] = self.capitalizeFirstLetter(state);
        });
      });
      sharedBehaviour(context);
    });


  });

});
