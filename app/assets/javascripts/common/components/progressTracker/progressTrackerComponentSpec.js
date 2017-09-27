/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define([
  'angular',
  'angularMocks',
  'lodash'
], function(angular, mocks, _) {
  'use strict';

  describe('progressTrackerComponent', function() {

    function SuiteMixinFactory(ComponentTestSuiteMixin) {

      function SuiteMixin() {
        ComponentTestSuiteMixin.call(this);
      }

      SuiteMixin.prototype = Object.create(ComponentTestSuiteMixin.prototype);
      SuiteMixin.prototype.constructor = SuiteMixin;

      SuiteMixin.prototype.createController = function (items, current) {
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

      return SuiteMixin;
    }


    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function(ComponentTestSuiteMixin) {
      _.extend(this, new SuiteMixinFactory(ComponentTestSuiteMixin).prototype);

      this.injectDependencies('$q', '$rootScope', '$compile', 'factory');
      this.putHtmlTemplates(
        '/assets/javascripts/common/components/progressTracker/progressTracker.html');
    }));

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

});
