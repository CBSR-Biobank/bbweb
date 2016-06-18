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

  describe('loginDirective', function() {

    var createController = function () {
      this.element = angular.element('<login></login>');
      this.scope = this.$rootScope.$new();

      this.$compile(this.element)(this.scope);
      this.scope.$digest();
      this.controller = this.element.controller('login');
    };

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function(testSuiteMixin, testUtils) {
      var self = this;

      _.extend(self, testSuiteMixin);

      self.injectDependencies('$rootScope',
                              '$compile',
                              '$q',
                              '$state',
                              'usersService',
                              'modalService');

      self.putHtmlTemplates(
        '/assets/javascripts/users/directives/login/login.html');
    }));

    it('has valid state', function() {
      createController.call(this);
      expect(this.controller.credentials.email).toBeEmptyString();
      expect(this.controller.credentials.password).toBeEmptyString();
      expect(this.controller.login).toBeFunction();
    });

    it('on initializaton, changes to home state if user is already logged in', function() {
      spyOn(this.usersService, 'isAuthenticated').and.returnValue(true);
      spyOn(this.$state, 'go').and.returnValue(true);

      createController.call(this);
      this.scope.$digest();
      expect(this.$state.go).toHaveBeenCalledWith('home');
    });

    it('changes to home state on login attempt', function () {
      spyOn(this.usersService, 'login').and.returnValue(this.$q.when(true));
      spyOn(this.$state, 'go').and.returnValue(true);

      createController.call(this);
      this.controller.login({ email: 'test@test.com', password: 'secret-password' });
      this.scope.$digest();

      expect(this.$state.go).toHaveBeenCalledWith('home');
    });

    describe('for badly formatted login error message', function() {

      var context = {};

      beforeEach(function() {
        context.loginError = {};
      });

      invalidLoginAttemptShared(context);

    });

    describe('for invalid login attempt invalid password or email', function() {

      var context = {};

      beforeEach(function() {
        context.loginError = { data: { message: 'invalid email or password'} };
      });

      invalidLoginAttemptShared(context);

    });

    describe('user not active login attempt', function() {

      var context = {};

      beforeEach(function() {
        context.loginError = { data: { message: 'the user is not active'} };
      });

      invalidLoginAttemptShared(context);

    });

    describe('user is locked login attempt', function() {

      var context = {};

      beforeEach(function() {
        context.loginError = { data: { message: 'the user is locked'} };
      });

      invalidLoginAttemptShared(context);

    });

    describe('other error message with data.message field', function() {

      var context = {};

      beforeEach(function() {
        context.loginError = { data: { message: 'xxxx'} };
      });

      invalidLoginAttemptShared(context);

    });

    function invalidLoginAttemptShared(context) {

      describe('for invalid login attempt', function() {

        beforeEach(function() {
          spyOn(this.$state, 'go').and.returnValue(true);
        });

        it('for invalid email or password and OK', function () {
          spyOn(this.usersService, 'login').and.returnValue(this.$q.reject(context.loginError));
          spyOn(this.modalService, 'showModal').and.returnValue(this.$q.when('OK'));

          createController.call(this);
          this.controller.login({ email: 'test@test.com', password: 'secret-password' });
          this.scope.$digest();

          expect(this.modalService.showModal).toHaveBeenCalled();
          expect(this.$state.go).toHaveBeenCalledWith('home.users.login', {}, { reload: true });
        });

        it('for invalid email or password and cancel pressed', function () {
          spyOn(this.usersService, 'login').and.returnValue(this.$q.reject(context.loginError));
          spyOn(this.modalService, 'showModal').and.returnValue(this.$q.reject('Cancel'));

          createController.call(this);
          this.controller.login({ email: 'test@test.com', password: 'secret-password' });
          this.scope.$digest();

          expect(this.modalService.showModal).toHaveBeenCalled();
          expect(this.$state.go).toHaveBeenCalledWith('home');
        });

      });
    }

  });

});
