/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define([
  'angular',
  'angularMocks',
  'underscore',
  'faker'
], function(angular, mocks, _, faker) {
  'use strict';

  describe('passwordSentDirective', function() {

    function createDirective(test) {
      var element, scope;

      element = angular.element('<password-sent email="vm.email"></password-sent>');
      scope = test.$rootScope.$new();
      scope.vm = { email: test.email };

      test.$compile(element)(scope);
      scope.$digest();

      return {
        element:    element,
        scope:      scope,
        controller: element.controller('passwordSent')
      };
    }

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function($rootScope, $compile, templateMixin) {
      var self = this;

      _.extend(self, templateMixin);

      self.$rootScope          = self.$injector.get('$rootScope');
      self.$compile            = self.$injector.get('$compile');

      self.putHtmlTemplates(
        '/assets/javascripts/users/directives/passwordSent/passwordSent.html');

      self.email = faker.internet.email();
    }));

    it('has valid scope', function() {
      var directive = createDirective(this);
      expect(directive.controller.email).toEqual(this.email);
    });

  });

});
