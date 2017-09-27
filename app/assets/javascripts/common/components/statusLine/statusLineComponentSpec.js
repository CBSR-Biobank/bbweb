/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(function (require) {
  'use strict';

  var mocks = require('angularMocks'),
      _     = require('lodash');

  describe('Component: statusLine', function() {

    function SuiteMixinFactory(ComponentTestSuiteMixin) {

      function SuiteMixin() {
      }

      SuiteMixin.prototype = Object.create(ComponentTestSuiteMixin.prototype);
      SuiteMixin.prototype.constructor = SuiteMixin;

      SuiteMixin.prototype.createController = function (stateLabelFunc, timeAdded, timeModified, useLabels) {
        stateLabelFunc = stateLabelFunc || this.stateLabelFunc;
        ComponentTestSuiteMixin.prototype.createController.call(
          this,
          [
            '<status-line ',
            '  state-label-func="vm.stateLabelFunc" ',
            '  time-added="vm.timeAdded" ',
            '  time-modified="vm.timeModified" ',
            '  use-labels="vm.useLabels"> ',
            '</status-line>'
          ].join(''),
          {
            stateLabelFunc: stateLabelFunc,
            timeAdded:      timeAdded,
            timeModified:   timeModified,
            useLabels:      useLabels
          },
          'statusLine');
      };

      return SuiteMixin;
    }

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function(ComponentTestSuiteMixin) {
      _.extend(this, new SuiteMixinFactory(ComponentTestSuiteMixin).prototype);

      this.injectDependencies('$rootScope',
                              '$compile',
                              'Centre',
                              '$filter',
                              'factory');

      this.putHtmlTemplates(
        '/assets/javascripts/common/components/statusLine/statusLine.html');
      this.stateLabelFunc = jasmine.createSpy().and.returnValue(null);
    }));

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
        var stateLabel = this.factory.stringNext(),
            stateLabelFunc = jasmine.createSpy().and.returnValue(stateLabel),
            centre = this.Centre.create(this.factory.centre()),
            items;

        context.createController(stateLabelFunc, centre.timeAdded, centre.timeModified);
        items = this.element.find(context.mainElement);
        expect(items.length).toBe(3);
        _.each(_.range(3), function (n) {
          var strong = items.eq(n).find('span');
          expect(strong.length).toBe(1);
        });

        expect(this.controller.stateLabelFunc()()).toEqual(stateLabel);
        expect(items.eq(1).find('span').eq(0).text()).toContain(this.$filter('timeago')(centre.timeAdded));
      });

      it('items have the default class', function() {
        var stateLabel = this.factory.stringNext(),
            stateLabelFunc = jasmine.createSpy().and.returnValue(stateLabel),
            centre = this.Centre.create(this.factory.centre());

        context.createController(stateLabelFunc, centre.timeAdded, centre.timeModified);
        expect(this.controller.class).toEqual(context.itemClass);
      });

      it('if timeAdded is before the year 200, displays correct label', function() {
        var centre = this.Centre.create(this.factory.centre()),
            timeAdded = new Date(1999, 1),
            items;

        context.createController(undefined, timeAdded, centre.timeModified);
        items = this.element.find(context.mainElement);
        expect(centre.timeModified).toBeDefined();
        expect(items.eq(1).find('span').eq(0).text()).toContain('on system initialization');
      });

      it('displays correpct value if timeModified has a value', function() {
        var centre = this.Centre.create(this.factory.centre()),
            items;

        context.createController(undefined, centre.timeAdded, centre.timeModified);
        items = this.element.find(context.mainElement);
        expect(centre.timeModified).toBeDefined();
        expect(items.eq(2).find('span').eq(0).text()).toContain(this.$filter('timeago')(centre.timeModified));
      });

      it('displays correct value if timeModified is undefined', function() {
        var centre = new this.Centre(this.factory.centre()),
            items;

        centre.timeModified = undefined;
        context.createController(undefined, centre.timeAdded, centre.timeModified);
        items = this.element.find(context.mainElement);
        expect(items.eq(2).find('span').eq(0).text()).toContain('never');
      });

      it('does not show status if stateLabelFunc is undefined', function() {
        var centre = new this.Centre(this.factory.centre()),
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

});
