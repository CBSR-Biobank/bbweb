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

  describe('biobankFooterDirective', function() {

    function createDirective(test) {
      var element,
          scope = test.$rootScope.$new();

      element = angular.element('<biobank-footer></biobank-footer>');
      test.$compile(element)(scope);
      scope.$digest();

      return {
        element:    element,
        scope:      scope,
        controller: element.controller('biobankFooter')
      };
    }

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function(templateMixin) {
      var self = this;

      self.$rootScope = self.$injector.get('$rootScope');
      self.$compile   = self.$injector.get('$compile');

      _.extend(self, templateMixin);
    }));

    it('has valid scope', function() {
      var directive = createDirective(this);
      expect(directive.controller).toBeDefined();
    });
  });

});
