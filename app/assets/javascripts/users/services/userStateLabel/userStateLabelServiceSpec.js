/**
 * Jasmine test suite
 *
 */
/* global angular */

import { TestSuiteMixin } from 'test/mixins/TestSuiteMixin';
import ngModule from '../../index'
import sharedBehaviour from 'test/behaviours/labelServiceSharedBehaviour';

describe('userStateLabelService', function() {

  beforeEach(() => {
    angular.mock.module(ngModule, 'biobank.test');
    angular.mock.inject(function() {
      Object.assign(this, TestSuiteMixin);
      this.injectDependencies('userStateLabelService',
                              'UserState');
    });
  });

  describe('shared behaviour', function() {
    var context = {};
    beforeEach(function() {
      context.labels = Object.values(this.UserState);
      context.toLabelFunc =
        this.userStateLabelService.stateToLabelFunc.bind(this.userStateLabelService);
      context.expectedLabels = [];
      Object.values(this.UserState).forEach((state) => {
        context.expectedLabels[state] = this.capitalizeFirstLetter(state);
      });
    });
    sharedBehaviour(context);
  });

});
