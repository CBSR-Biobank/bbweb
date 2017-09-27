/**
 * Jasmine test suite
 *
 */
/* global angular */

import _ from 'lodash';
import sharedBehaviour from '../../test/labelServiceSharedBehaviour';

describe('specimenStateLabelService', function() {

  beforeEach(() => {
    angular.mock.module('biobankApp', 'biobank.test');
    angular.mock.inject(function(TestSuiteMixin) {
      _.extend(this, TestSuiteMixin.prototype);
      this.injectDependencies('specimenStateLabelService',
                              'SpecimenState');
    });
  });

  describe('shared behaviour', function() {
    var context = {};
    beforeEach(function() {
      var self = this;

      context.labels = _.values(this.SpecimenState);
      context.toLabelFunc = this.specimenStateLabelService.stateToLabelFunc;
      context.expectedLabels = [];
      _.values(this.SpecimenState).forEach(function (state) {
        context.expectedLabels[state] = self.capitalizeFirstLetter(state);
      });
    });
    sharedBehaviour(context);
  });

});
