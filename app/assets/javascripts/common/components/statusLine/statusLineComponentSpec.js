/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */
/* global angular */

import _ from 'lodash'
import ngModule from '../../index'

describe('Component: statusLine', function() {

  beforeEach(() => {
    angular.mock.module(ngModule, 'biobank.test');
    angular.mock.inject(function(ComponentTestSuiteMixin) {
      _.extend(this, ComponentTestSuiteMixin);

      this.injectDependencies('$rootScope',
                              '$compile',
                              'Centre',
                              '$filter',
                              'Factory');

      this.stateLabelFunc = jasmine.createSpy().and.returnValue(null);

      this.createController = (stateLabelFunc, timeAdded, timeModified, useLabels) => {
        stateLabelFunc = stateLabelFunc || this.stateLabelFunc;
        ComponentTestSuiteMixin.createController.call(
          this,
          `<status-line
              state-label-func="vm.stateLabelFunc"
              time-added="vm.timeAdded"
              time-modified="vm.timeModified"
              use-labels="vm.useLabels">
            </status-line>`,
          {
            stateLabelFunc: stateLabelFunc,
            timeAdded:      timeAdded,
            timeModified:   timeModified,
            useLabels:      useLabels
          },
          'statusLine');
      };
    });
  });

  describe('when using labels', function() {
    var context = {};
    beforeEach(function() {
      var self = this;
      context.mainElement= 'span.label';
      context.createController = function (stateLabelFunc, timeAdded, timeModified) {
        self.createController(stateLabelFunc, timeAdded, timeModified, true);
      };
      context.itemClass = 'label-default';
    });

    sharedBehaviour(context);
  });

  describe('when not using labels', function() {
    var context = {};
    beforeEach(function() {
      var self = this;
      context.mainElement= 'small.text-muted';
      context.createController = function (stateLabelFunc, timeAdded, timeModified) {
        self.createController(stateLabelFunc, timeAdded, timeModified, false);
      };
      context.itemClass = 'text-info';
    });

    sharedBehaviour(context);
  });

  function sharedBehaviour(context) {

    it('has the correct number of items', function() {
      var stateLabel = this.Factory.stringNext(),
          stateLabelFunc = jasmine.createSpy().and.returnValue(stateLabel),
          centre = this.Centre.create(this.Factory.centre()),
          items;

      context.createController(stateLabelFunc, centre.timeAdded, centre.timeModified);
      items = this.element.find(context.mainElement);
      expect(items.length).toBe(3);
      _.range(3).forEach((n) => {
        var strong = items.eq(n).find('span');
        expect(strong.length).toBe(1);
      });

      expect(this.controller.stateLabelFunc()()).toEqual(stateLabel);
      expect(items.eq(1).find('span').eq(0).text()).toContain(this.$filter('timeago')(centre.timeAdded));
    });

    it('items have the default class', function() {
      var stateLabel = this.Factory.stringNext(),
          stateLabelFunc = jasmine.createSpy().and.returnValue(stateLabel),
          centre = this.Centre.create(this.Factory.centre());

      context.createController(stateLabelFunc, centre.timeAdded, centre.timeModified);
      expect(this.controller.class).toEqual(context.itemClass);
    });

    it('if timeAdded is before the year 200, displays correct label', function() {
      var centre = this.Centre.create(this.Factory.centre()),
          timeAdded = new Date(1999, 1),
          items;

      context.createController(undefined, timeAdded, centre.timeModified);
      items = this.element.find(context.mainElement);
      expect(centre.timeModified).toBeDefined();
      expect(items.eq(1).find('span').eq(0).text()).toContain('on system initialization');
    });

    it('displays correpct value if timeModified has a value', function() {
      var centre = this.Centre.create(this.Factory.centre()),
          items;

      context.createController(undefined, centre.timeAdded, centre.timeModified);
      items = this.element.find(context.mainElement);
      expect(centre.timeModified).toBeDefined();
      expect(items.eq(2).find('span').eq(0).text()).toContain(this.$filter('timeago')(centre.timeModified));
    });

    it('displays correct value if timeModified is undefined', function() {
      var centre = new this.Centre(this.Factory.centre()),
          items;

      centre.timeModified = undefined;
      context.createController(undefined, centre.timeAdded, centre.timeModified);
      items = this.element.find(context.mainElement);
      expect(items.eq(2).find('span').eq(0).text()).toContain('never');
    });

    it('does not show status if stateLabelFunc is undefined', function() {
      var centre = new this.Centre(this.Factory.centre()),
          items;

      this.stateLabelFunc = undefined;
      context.createController(undefined, centre.timeAdded, centre.timeModified);
      items = this.element.find(context.mainElement);
      expect(this.controller.hasState).toBeFalse();
      expect(items.length).toBe(2);
      expect(items.eq(0).find('span').eq(0).text()).toContain(this.$filter('timeago')(centre.timeAdded));
      expect(items.eq(1).find('span').eq(0).text()).toContain(this.$filter('timeago')(centre.timeModified));
    });

  }

});
