/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define([
  'angular',
  'angularMocks',
  'lodash'
], function(angular, mocks, _) {
  'use strict';

  describe('Component: forgotPassword', function() {

    var createController = function () {
      this.element = angular.element('<forgot-password></forgot-password>');
      this.scope = this.$rootScope.$new();
      this.$compile(this.element)(this.scope);
      this.scope.$digest();
      this.controller = this.element.controller('forgotPassword');
    };

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function(testSuiteMixin) {
      var self = this;
      _.extend(self, testSuiteMixin);

      self.injectDependencies('$rootScope',
                              '$compile',
                              '$q',
                              '$state',
                              'usersService',
                              'modalService',
                              'notificationsService',
                              'factory');

      self.putHtmlTemplates(
        '/assets/javascripts/users/components/forgotPassword/forgotPassword.html');
    }));

    it('has valid scope', function() {
      createController.call(this);
      expect(this.controller.email).toBe('');
      expect(this.controller.submit).toBeFunction();
    });

    it('goes to correct state on submit', function() {
      var email = this.factory.emailNext();

      spyOn(this.usersService, 'passwordReset').and.returnValue(this.$q.when('OK'));
      spyOn(this.$state, 'go').and.returnValue(null);

      createController.call(this);
      this.controller.submit(email);
      this.scope.$digest();
      expect(this.$state.go).toHaveBeenCalledWith('home.users.forgot.passwordSent',
                                                  { email: email });
    });

    it('goes to correct state if email is not registered', function() {
      var email = this.factory.emailNext();

      spyOn(this.usersService, 'passwordReset').and.returnValue(
        this.$q.reject({ status: 'error', message: 'email address not registered'}));
      spyOn(this.$state, 'go').and.returnValue(null);

      createController.call(this);
      this.controller.submit(email);
      this.scope.$digest();
      expect(this.$state.go).toHaveBeenCalledWith('home.users.forgot.emailNotFound');
    });

    it('displays information in modal on password reset failure and user presses OK', function() {
      var email = this.factory.emailNext();

      spyOn(this.usersService, 'passwordReset').and.returnValue(
        this.$q.reject({ status: 'error', message: 'xxxx'}));
      spyOn(this.modalService, 'modalOk').and.returnValue(this.$q.when('OK'));
      spyOn(this.$state, 'go').and.returnValue(null);

      createController.call(this);
      this.controller.submit(email);
      this.scope.$digest();
      expect(this.$state.go).toHaveBeenCalledWith('home');
    });

    it('displays information in modal on password reset failure and user closes window', function() {
      var email = this.factory.emailNext();

      spyOn(this.usersService, 'passwordReset').and.returnValue(
        this.$q.reject({ status: 'error', message: 'xxxx'}));
      spyOn(this.modalService, 'modalOk').and.returnValue(this.$q.reject('Cancel'));
      spyOn(this.$state, 'go').and.returnValue(null);


      createController.call(this);
      this.controller.submit(email);
      this.scope.$digest();
      expect(this.$state.go).toHaveBeenCalledWith('home');
    });

  });

});
