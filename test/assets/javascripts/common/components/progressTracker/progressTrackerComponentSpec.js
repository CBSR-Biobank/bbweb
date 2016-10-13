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

  function SuiteMixinFactory(TestSuiteMixin) {

    function SuiteMixin() {
    }

    SuiteMixin.prototype = Object.create(TestSuiteMixin.prototype);
    SuiteMixin.prototype.constructor = SuiteMixin;

    SuiteMixin.prototype.createScope = function (items, current) {
      this.element = angular.element([
        '<progress-tracker',
        '  items="vm.progressInfo.items"',
        '  current="vm.progressInfo.current">',
        '</progress-tracker>'
      ].join(''));
      this.scope = this.$rootScope.$new();
      this.scope.vm = { progressInfo:  { items: items, current: current } };
      this.$compile(this.element)(this.scope);
      this.scope.$digest();
      this.controller = this.element.controller('progressTracker');
    };

    return SuiteMixin;
  }

  describe('progressTrackerComponent', function() {

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function(TestSuiteMixin, testUtils) {
      var SuiteMixin = new SuiteMixinFactory(TestSuiteMixin);
      _.extend(this, SuiteMixin.prototype);
      this.injectDependencies('$q', '$rootScope', '$compile', 'factory');
      this.putHtmlTemplates(
        '/assets/javascripts/common/components/progressTracker/progressTracker.html');
    }));

    it('has valid scope', function() {
      var self = this,
          items = _.map(_.range(3), function () { return self.factory.stringNext(); }),
          current = items[0];
      self.createScope(items, current);
      expect(self.controller.numSteps).toBe(items.length);
      expect(self.controller.steps).toBeArrayOfSize(items.length);
    });

    it('all steps can be marked as todo', function() {
      var self = this,
          items = _.map(_.range(3), function () { return self.factory.stringNext(); });
      self.createScope(items, 0);
      expect(self.controller.numSteps).toBe(items.length);
      _.each(self.controller.steps, function (step) {
        expect(step.class).toBe('progtrckr-todo');
      });
    });

    it('all steps can be marked as done', function() {
      var self = this,
          items = _.map(_.range(3), function () { return self.factory.stringNext(); });
      self.createScope(items, items.length);
      expect(self.controller.numSteps).toBe(items.length);
      _.each(self.controller.steps, function (step) {
        expect(step.class).toBe('progtrckr-done');
      });
    });

  });

});
