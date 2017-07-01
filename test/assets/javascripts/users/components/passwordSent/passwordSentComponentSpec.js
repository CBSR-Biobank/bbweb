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

  describe('Component: passwordSent', function() {

    function SuiteMixinFactory(ComponentTestSuiteMixin) {

      function SuiteMixin() {
      }

      SuiteMixin.prototype = Object.create(ComponentTestSuiteMixin.prototype);
      SuiteMixin.prototype.constructor = SuiteMixin;

      SuiteMixin.prototype.createController = function (email) {
        email = email || this.email;
        ComponentTestSuiteMixin.prototype.createController.call(
          this,
          '<password-sent email="vm.email"></password-sent>',
          { email: email },
          'passwordSent');
      };

      return SuiteMixin;
    }

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function(ComponentTestSuiteMixin) {
      _.extend(this, new SuiteMixinFactory(ComponentTestSuiteMixin).prototype);
      this.injectDependencies('$rootScope', '$compile', 'factory');
      this.putHtmlTemplates('/assets/javascripts/users/components/passwordSent/passwordSent.html');
      this.email = this.factory.emailNext();
    }));

    it('has valid scope', function() {
      this.createController();
      expect(this.controller.email).toEqual(this.email);
    });

  });

});
