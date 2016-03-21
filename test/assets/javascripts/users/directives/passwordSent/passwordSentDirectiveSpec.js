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

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function($rootScope, $compile, directiveTestSuite) {
      var self = this;

      _.extend(self, directiveTestSuite);

      self.putHtmlTemplates(
        '/assets/javascripts/users/directives/passwordSent/passwordSent.html');

      self.createController = createController;

      ///--

      function createController(email) {
        self.element = angular.element('<password-sent email="vm.email"></password-sent>');
        self.scope = $rootScope.$new();
        self.scope.vm = { email:email };

        $compile(self.element)(self.scope);
        self.scope.$digest();
        self.controller = self.element.controller('passwordSent');
      }
    }));

    it('has valid scope', function() {
      var email = faker.internet.email();

      this.createController(email);
      expect(this.scope.vm.email).toEqual(email);
    });

  });

});
