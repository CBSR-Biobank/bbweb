/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define([
  'angular',
  'angularMocks',
  'underscore'
], function(angular, mocks, _) {
  'use strict';

  describe('homeDirective', function() {

    function createDirective(test) {
      var element,
          scope = test.$rootScope.$new();

      element = angular.element('<home></home>');
      test.$compile(element)(scope);
      scope.$digest();

      return {
        element:    element,
        scope:      scope,
        controller: element.controller('home')
      };
    }

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function(directiveTestSuite) {
      var self = this;

      self.$rootScope = self.$injector.get('$rootScope');
      self.$compile   = self.$injector.get('$compile');

      _.extend(self, directiveTestSuite);

      self.putHtmlTemplates('/assets/javascripts/home/directives/home/home.html');
    }));

    it('has valid scope', function() {
      var directive = createDirective(this);
      expect(directive.controller).toBeDefined();
      expect(this.$rootScope.pageTitle).toBeDefined('Biobank');
    });
  });

});
