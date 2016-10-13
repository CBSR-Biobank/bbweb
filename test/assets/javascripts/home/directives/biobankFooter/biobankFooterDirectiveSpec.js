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

  describe('biobankFooterDirective', function() {

    var createDirective = function () {
      this.element = angular.element('<biobank-footer></biobank-footer>');
      this.scope = this.$rootScope.$new();
      this.$compile(this.element)(this.scope);
      this.scope.$digest();
      this.controller = this.element.controller('biobankFooter');
    };

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function(TestSuiteMixin) {
      var self = this;
      _.extend(self, TestSuiteMixin.prototype);
      self.injectDependencies('$rootScope', '$compile');
    }));

    it('has valid scope', function() {
      createDirective.call(this);
      expect(this.controller).toBeDefined();
    });
  });

});
