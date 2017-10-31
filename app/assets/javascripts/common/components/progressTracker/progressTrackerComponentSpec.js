/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
/* global angular */

import _ from 'lodash';
import ngModule from '../../index'

describe('progressTrackerComponent', function() {

  beforeEach(() => {
    angular.mock.module(ngModule, 'biobank.test');
    angular.mock.inject(function(ComponentTestSuiteMixin) {
      _.extend(this, ComponentTestSuiteMixin);

      this.injectDependencies('$q', '$rootScope', '$compile', 'Factory');

      this.createController = (items, current) => {
        ComponentTestSuiteMixin.createController.call(
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
    var items = _.range(3).map(() => this.Factory.stringNext()),
        current = items[0];
    this.createController(items, current);
    expect(this.controller.numSteps).toBe(items.length);
    expect(this.controller.steps).toBeArrayOfSize(items.length);
  });

  it('all steps can be marked as todo', function() {
    var items = _.range(3).map(() => this.Factory.stringNext());
    this.createController(items, 0);
    expect(this.controller.numSteps).toBe(items.length);
    this.controller.steps.forEach((step) => {
      expect(step.class).toBe('progtrckr-todo');
    });
  });

  it('all steps can be marked as done', function() {
    var items = _.range(3).map(() => this.Factory.stringNext());
    this.createController(items, items.length);
    expect(this.controller.numSteps).toBe(items.length);
    this.controller.steps.forEach((step) => {
      expect(step.class).toBe('progtrckr-done');
    });
  });

});
