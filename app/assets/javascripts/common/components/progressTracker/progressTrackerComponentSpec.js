/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
/* global angular */

import _ from 'lodash';

describe('progressTrackerComponent', function() {

  beforeEach(() => {
    angular.mock.module('biobankApp', 'biobank.test');
    angular.mock.inject(function(ComponentTestSuiteMixin) {
      _.extend(this, ComponentTestSuiteMixin.prototype);

      this.injectDependencies('$q', '$rootScope', '$compile', 'factory');

      this.createController = (items, current) => {
        ComponentTestSuiteMixin.prototype.createController.call(
          this,
          [
            '<progress-tracker',
            '  items="vm.progressInfo.items"',
            '  current="vm.progressInfo.current">',
            '</progress-tracker>'
          ].join(''),
          { progressInfo:  { items: items, current: current } },
          'progressTracker');
      };
    });
  });

  it('has valid scope', function() {
    var self = this,
        items = _.map(_.range(3), function () { return self.factory.stringNext(); }),
        current = items[0];
    self.createController(items, current);
    expect(self.controller.numSteps).toBe(items.length);
    expect(self.controller.steps).toBeArrayOfSize(items.length);
  });

  it('all steps can be marked as todo', function() {
    var self = this,
        items = _.map(_.range(3), function () { return self.factory.stringNext(); });
    self.createController(items, 0);
    expect(self.controller.numSteps).toBe(items.length);
    _.each(self.controller.steps, function (step) {
      expect(step.class).toBe('progtrckr-todo');
    });
  });

  it('all steps can be marked as done', function() {
    var self = this,
        items = _.map(_.range(3), function () { return self.factory.stringNext(); });
    self.createController(items, items.length);
    expect(self.controller.numSteps).toBe(items.length);
    _.each(self.controller.steps, function (step) {
      expect(step.class).toBe('progtrckr-done');
    });
  });

});
