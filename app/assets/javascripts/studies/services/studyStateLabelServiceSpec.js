/**
 * Jasmine test suite
 *
 */
/* global angular */

import _ from 'lodash';
import sharedBehaviour from '../../test/labelServiceSharedBehaviour';

describe('studyStateLabelService', function() {

  beforeEach(() => {
    angular.mock.module('biobankApp', 'biobank.test');
    angular.mock.inject(function(TestSuiteMixin) {
      _.extend(this, TestSuiteMixin);
      this.injectDependencies('studyStateLabelService',
                              'StudyState');
    });
  });

  describe('shared behaviour', function() {
    var context = {};
    beforeEach(function() {
      var self = this;

      context.labels = _.values(this.StudyState);
      context.toLabelFunc = this.studyStateLabelService.stateToLabelFunc;
      context.expectedLabels = [];
      _.values(this.StudyState).forEach(function (state) {
        context.expectedLabels[state] = self.capitalizeFirstLetter(state);
      });
    });
    sharedBehaviour(context);
  });

});
