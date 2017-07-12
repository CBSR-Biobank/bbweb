/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(function (require) {
  'use strict';

  var mocks   = require('angularMocks'),
      _       = require('lodash');

  describe('Component: forgotPassword', function() {

    function SuiteMixinFactory(ComponentTestSuiteMixin) {

      function SuiteMixin() {
        ComponentTestSuiteMixin.call(this);
      }

      SuiteMixin.prototype = Object.create(ComponentTestSuiteMixin.prototype);
      SuiteMixin.prototype.constructor = SuiteMixin;

      SuiteMixin.prototype.createController = function () {
        ComponentTestSuiteMixin.prototype.createController.call(
          this,
          '<forgot-password></forgot-password>',
          undefined,
          'forgotPassword');
      };

      return SuiteMixin;
    }

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function(ComponentTestSuiteMixin) {
      _.extend(this, new SuiteMixinFactory(ComponentTestSuiteMixin).prototype);

      this.injectDependencies('$rootScope',
                              '$compile',
                              '$q',
                              '$state',
                              'usersService',
                              'modalService',
                              'notificationsService',
                              'factory');

      this.putHtmlTemplates(
        '/assets/javascripts/users/components/forgotPassword/forgotPassword.html');
    }));

    it('has valid scope', function() {
      this.createController();
      expect(this.controller.email).toBe('');
      expect(this.controller.submit).toBeFunction();
    });

    it('goes to correct state on submit', function() {
      var email = this.factory.emailNext();

      spyOn(this.usersService, 'passwordReset').and.returnValue(this.$q.when('OK'));
      spyOn(this.$state, 'go').and.returnValue(null);

      this.createController();
      this.controller.submit(email);
      this.scope.$digest();
      expect(this.$state.go).toHaveBeenCalledWith('home.users.forgot.passwordSent',
                                                  { email: email });
    });

    it('goes to correct state if email is not registered', function() {
      var email = this.factory.emailNext();

      spyOn(this.$state, 'go').and.returnValue(null);

      this.createController();
      spyOn(this.usersService, 'passwordReset').and.returnValue(
        this.$q.reject({ status: 'error', message: 'email address not registered'}));
      this.controller.submit(email);
      this.scope.$digest();
      expect(this.$state.go).toHaveBeenCalledWith('home.users.forgot.emailNotFound');
    });

    it('displays information in modal on password reset failure and user presses OK', function() {
      var email = this.factory.emailNext();

      spyOn(this.modalService, 'modalOk').and.returnValue(this.$q.when('OK'));
      spyOn(this.$state, 'go').and.returnValue(null);

      this.createController();
      spyOn(this.usersService, 'passwordReset').and.returnValue(
        this.$q.reject({ status: 'error', message: 'xxxx'}));
      this.controller.submit(email);
      this.scope.$digest();
      expect(this.$state.go).toHaveBeenCalledWith('home');
    });

    it('displays information in modal on password reset failure and user closes window', function() {
      var email = this.factory.emailNext();

      spyOn(this.$state, 'go').and.returnValue(null);
      this.createController();
      spyOn(this.usersService, 'passwordReset').and.returnValue(
        this.$q.reject({ status: 'error', message: 'xxxx'}));
      spyOn(this.modalService, 'modalOk').and.returnValue(this.$q.reject('Cancel'));
      this.controller.submit(email);
      this.scope.$digest();
      expect(this.$state.go).toHaveBeenCalledWith('home');
    });

  });

});
