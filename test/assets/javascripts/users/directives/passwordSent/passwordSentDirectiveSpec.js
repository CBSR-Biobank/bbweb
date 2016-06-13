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

  describe('passwordSentDirective', function() {

    var createDirective = function () {
      this.element = angular.element('<password-sent email="vm.email"></password-sent>');
      this.scope = this.$rootScope.$new();
      this.scope.vm = { email: this.email };
      this.$compile(this.element)(this.scope);
      this.scope.$digest();
      this.controller = this.element.controller('passwordSent');
    };

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function($rootScope, $compile, testSuiteMixin) {
      var self = this;

      _.extend(self, testSuiteMixin);

      self.injectDependencies('$rootScope', '$compile', 'factory');
      self.putHtmlTemplates(
        '/assets/javascripts/users/directives/passwordSent/passwordSent.html');

      self.email = self.factory.emailNext();
    }));

    it('has valid scope', function() {
      createDirective.call(this);
      expect(this.controller.email).toEqual(this.email);
    });

  });

});
