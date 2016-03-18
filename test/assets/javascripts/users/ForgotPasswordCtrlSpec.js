/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define([
  'angular',
  'angularMocks',
  'underscore',
  'faker'
], function(angular, mocks, _, faker) {
  'use strict';

  describe('Controller: ForgotPasswordCtrl', function() {

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function($rootScope, $controller) {
      var self = this;

      self.$q                   = self.$injector.get('$q');
      self.$state               = self.$injector.get('$state');
      self.usersService         = self.$injector.get('usersService');
      self.modalService         = self.$injector.get('modalService');
      self.notificationsService = self.$injector.get('notificationsService');

      self.createController = createController;

      function createController() {
        self.scope = $rootScope.$new();

        $controller('ForgotPasswordCtrl as vm', {
          $scope:       self.scope,
          $state:       self.$state,
          usersService: self.usersService,
          modalService: self.modalService
        });
        self.scope.$digest();
      }
    }));

    it('has valid scope', function() {
      this.createController();
      expect(this.scope.vm.email).toBe('');
    });

    it('goes to correct state on submit', function() {
      var email        = faker.internet.email();

      spyOn(this.usersService, 'passwordReset').and.returnValue(this.$q.when('OK'));
      spyOn(this.$state, 'go').and.callFake(function () {});

      this.createController();
      this.scope.vm.submit(email);
      this.scope.$digest();
      expect(this.$state.go).toHaveBeenCalledWith('home.users.forgot.passwordSent',
                                                  { email: email });
    });

    it('goes to correct state if email is not registered', function() {
      var deferred = this.$q.defer(),
          email    = faker.internet.email();

      spyOn(this.usersService, 'passwordReset').and.returnValue(deferred.promise);
      spyOn(this.$state, 'go').and.callFake(function () {});

      this.createController();
      this.scope.vm.submit(email);
      deferred.reject({ status: 'error', message: 'email address not registered'});
      this.scope.$digest();
      expect(this.$state.go).toHaveBeenCalledWith('home.users.forgot.emailNotFound');
    });

    it('displays information in modal on password reset failure and user presses OK', function() {
      var deferred = this.$q.defer(),
          email        = faker.internet.email();

      spyOn(this.usersService, 'passwordReset').and.returnValue(deferred.promise);
      spyOn(this.modalService, 'modalOk').and.returnValue(this.$q.when('OK'));
      spyOn(this.$state, 'go').and.callFake(function () {});

      this.createController();
      this.scope.vm.submit(email);
      deferred.reject({ status: 'error', message: 'xxxx'});
      this.scope.$digest();
      expect(this.$state.go).toHaveBeenCalledWith('home');
    });

    it('displays information in modal on password reset failure and user closes window', function() {
      var userServiceDeferred = this.$q.defer(),
          modalDeferred       = this.$q.defer(),
          email               = faker.internet.email();

      spyOn(this.usersService, 'passwordReset').and.returnValue(userServiceDeferred.promise);
      spyOn(this.modalService, 'modalOk').and.returnValue(modalDeferred.promise);
      spyOn(this.$state, 'go').and.callFake(function () {});

      userServiceDeferred.reject({ status: 'error', message: 'xxxx'});
      modalDeferred.reject('Cancel');

      this.createController();
      this.scope.vm.submit(email);
      this.scope.$digest();
      expect(this.$state.go).toHaveBeenCalledWith('home');
    });

  });

});
