/**
 * Jasmine test suite
 *
 */
/* global angular */

import { TestSuiteMixin } from 'test/mixins/TestSuiteMixin';
import ngModule from '../../index'
import sharedBehaviour from 'test/behaviours/labelServiceSharedBehaviour';

describe('centreStateLabelService', function() {

  beforeEach(() => {
    angular.mock.module(ngModule, 'biobank.test');
    angular.mock.inject(function() {
      Object.assign(this, TestSuiteMixin);
      this.injectDependencies('centreStateLabelService',
                              'CentreState');
    });
  });

  describe('shared behaviour', function() {
    var context = {};
    beforeEach(function() {
      context.labels = Object.values(this.CentreState);
      context.toLabelFunc = this.centreStateLabelService.stateToLabelFunc.bind(this.centreStateLabelService);
      context.expectedLabels = [];
      Object.values(this.CentreState).forEach((state) => {
        context.expectedLabels[state] = this.capitalizeFirstLetter(state);
      });
    });
    sharedBehaviour(context);
  });

});
