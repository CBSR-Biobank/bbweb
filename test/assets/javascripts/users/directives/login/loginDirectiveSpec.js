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

  describe('loginDirective', function() {

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function($rootScope, $compile, directiveTestSuite, testUtils) {
      var self = this;

      _.extend(self, directiveTestSuite);

      self.$q           = self.$injector.get('$q');
      self.$state       = self.$injector.get('$state');
      self.usersService = self.$injector.get('usersService');
      self.modalService = self.$injector.get('modalService');

      self.putHtmlTemplates(
        '/assets/javascripts/users/directives/login/login.html');

      self.createController = createController;

      ///--

      function createController() {
        self.element = angular.element('<login></login>');
        self.scope = $rootScope.$new();

        $compile(self.element)(self.scope);
        self.scope.$digest();
        self.controller = self.element.controller('login');
      }
    }));

    it('has valid state', function() {
      this.createController();
      expect(this.controller.credentials.email).toBeEmptyString();
      expect(this.controller.credentials.password).toBeEmptyString();
      expect(this.controller.login).toBeFunction();
    });

    it('on initializaton, changes to home state if user is already logged in', function() {
      spyOn(this.usersService, 'isAuthenticated').and.returnValue(true);
      spyOn(this.$state, 'go').and.returnValue(true);

      this.createController();
      this.scope.$digest();
      expect(this.$state.go).toHaveBeenCalledWith('home');
    });

    it('changes to home state on login attempt', function () {
      spyOn(this.usersService, 'login').and.returnValue(this.$q.when(true));
      spyOn(this.$state, 'go').and.returnValue(true);

      this.createController();
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

  });

  function invalidLoginAttemptShared(context) {

    describe('for invalid login attempt', function() {

      beforeEach(function() {
        spyOn(this.$state, 'go').and.returnValue(true);
      });


      it('for invalid email or password and OK', function () {
        var deferred = this.$q.defer();

        deferred.reject(context.loginError);
        spyOn(this.usersService, 'login').and.returnValue(deferred.promise);
        spyOn(this.modalService, 'showModal').and.returnValue(this.$q.when('OK'));

        this.createController();
        this.controller.login({ email: 'test@test.com', password: 'secret-password' });
        this.scope.$digest();

        expect(this.modalService.showModal).toHaveBeenCalled();
        expect(this.$state.go).toHaveBeenCalledWith('home.users.login', {}, { reload: true });
      });

      it('for invalid email or password and cancel pressed', function () {
        var loginDeferred = this.$q.defer(),
            modalDeferred = this.$q.defer();

        loginDeferred.reject(context.loginError);
        modalDeferred.reject('Cancel');

        spyOn(this.usersService, 'login').and.returnValue(loginDeferred.promise);
        spyOn(this.modalService, 'showModal').and.returnValue(modalDeferred.promise);

        this.createController();
        this.controller.login({ email: 'test@test.com', password: 'secret-password' });
        this.scope.$digest();

        expect(this.modalService.showModal).toHaveBeenCalled();
        expect(this.$state.go).toHaveBeenCalledWith('home');
      });

    });
  }

});
