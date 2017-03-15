/**
 * Jasmine test suite
 *
 */
define(function (require) {
  'use strict';

  var mocks = require('angularMocks'),
      _     = require('lodash');

  describe('stateHelperService', function() {

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function(TestSuiteMixin, ServerReplyMixin) {
      _.extend(this, TestSuiteMixin.prototype, ServerReplyMixin.prototype);
      this.injectDependencies('$state', 'stateHelper', 'factory');
      spyOn(this.$state, 'transitionTo').and.returnValue(null);
    }));

    it('reload and reinit reloads current state', function() {
      var args;

      this.stateHelper.reloadAndReinit();
      args = this.$state.transitionTo.calls.argsFor(0);
      expect(args[2]).toBeObject();
      expect(args[2].reload).toBeTrue();
      expect(args[2].inherit).toBeFalse();
      expect(args[2].notify).toBeTrue();
    });

    it('reloadStateAndReinit with default parameters', function() {
      var stateName = this.factory.stringNext(),
          args;

      this.stateHelper.reloadStateAndReinit(stateName, {}, {});
      args = this.$state.transitionTo.calls.argsFor(0);
      expect(args[0]).toBe(stateName);
      expect(args[1]).toBeObject();
      expect(args[2]).toBeObject();
    });

    it('reloadStateAndReinit with single parameter', function() {
      var stateName = this.factory.stringNext(),
          args;

      this.stateHelper.reloadStateAndReinit(stateName);
      args = this.$state.transitionTo.calls.argsFor(0);
      expect(args[0]).toBe(stateName);
      expect(args[1]).toBeObject();
      expect(args[2]).toBeObject();
    });

  });

});
