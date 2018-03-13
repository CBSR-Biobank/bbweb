/**
 * Jasmine test suite
 *
 */
/* global angular */

import _ from 'lodash';
import ngModule from '../../index'
import sharedBehaviour from '../../../test/behaviours/labelServiceSharedBehaviour';

describe('centreStateLabelService', function() {

  beforeEach(() => {
    angular.mock.module(ngModule, 'biobank.test');
    angular.mock.inject(function(TestSuiteMixin) {
      _.extend(this, TestSuiteMixin);
      this.injectDependencies('centreStateLabelService',
                              'CentreState');
    });
  });

  describe('shared behaviour', function() {
    var context = {};
    beforeEach(function() {
      var self = this;

      context.labels = _.values(this.CentreState);
      context.toLabelFunc = this.centreStateLabelService.stateToLabelFunc.bind(this.centreStateLabelService);
      context.expectedLabels = [];
      _.values(this.CentreState).forEach(function (state) {
        context.expectedLabels[state] = self.capitalizeFirstLetter(state);
      });
    });
    sharedBehaviour(context);
  });

});
