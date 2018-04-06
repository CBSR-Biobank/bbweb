/**
 * Jasmine test suite
 *
 */
/* global angular */

import { TestSuiteMixin } from 'test/mixins/TestSuiteMixin';
import ngModule from '../../index'
import sharedBehaviour from 'test/behaviours/labelServiceSharedBehaviour';

describe('studyStateLabelService', function() {

  beforeEach(() => {
    angular.mock.module(ngModule, 'biobank.test');
    angular.mock.inject(function() {
      Object.assign(this, TestSuiteMixin);
      this.injectDependencies('studyStateLabelService',
                              'StudyState');
    });
  });

  describe('shared behaviour', function() {
    var context = {};
    beforeEach(function() {
      var self = this;

      context.labels = Object.values(this.StudyState);
      context.toLabelFunc =
        this.studyStateLabelService.stateToLabelFunc.bind(this.studyStateLabelService);
      context.expectedLabels = [];
      Object.values(this.StudyState).forEach(function (state) {
        context.expectedLabels[state] = self.capitalizeFirstLetter(state);
      });
    });
    sharedBehaviour(context);
  });

});
