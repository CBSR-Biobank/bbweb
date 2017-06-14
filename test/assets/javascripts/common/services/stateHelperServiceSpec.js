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

    xit('update breadcrumbs', function() {
    });

  });

});
