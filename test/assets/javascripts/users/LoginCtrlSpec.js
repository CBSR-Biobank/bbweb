/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define([
  'angular',
  'angularMocks',
  'underscore'
], function(angular, mocks, _) {
  'use strict';

  describe('Controller: LoginCtrl', function() {

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function($rootScope, $controller) {
      var self = this;

      self.$q                   = self.$injector.get('$q');
      self.$state               = self.$injector.get('$state');
      self.usersService         = self.$injector.get('usersService');
      self.modalService         = self.$injector.get('modalService');

      this.createController = createController;
      this.badLoginAttempt = badLoginAttempt;

      function createController() {
        self.scope = $rootScope.$new();

        $controller('LoginCtrl as vm', {
          $scope:       self.scope,
          $state:       self.$state,
          usersService: self.usersService,
          modalService: self.modalService
        });
        self.scope.$digest();
      }

      function badLoginAttempt(replyMessage) {
        var deferred = self.$q.defer();

        spyOn(self.modalService, 'showModal').and.returnValue(self.$q.when('OK'));
        spyOn(self.usersService, 'isAuthenticated').and.returnValue(false);
        spyOn(self.usersService, 'login').and.returnValue(deferred.promise);
        spyOn(self.$state, 'go').and.callFake(function () {});

        deferred.reject({ status: 'error', data: { message: replyMessage } });

        self.createController();
        self.scope.vm.login({});
        self.scope.$digest();

        expect(self.$state.go).toHaveBeenCalledWith('home.users.login', {}, { reload: true });
        return self.modalService;
      }
    }));

    it('has valid scope', function() {
      this.createController();
      expect(this.scope.vm.credentials.email).toBeEmptyString();
      expect(this.scope.vm.credentials.password).toBeEmptyString();
    });

    it('returns to home state if user is authenticated', function() {
      spyOn(this.usersService, 'isAuthenticated').and.returnValue(true);
      spyOn(this.$state, 'go').and.callFake(function () {});
      this.createController();
      expect(this.$state.go).toHaveBeenCalledWith('home');
    });

    it('returns to home state after successful login', function() {
      spyOn(this.usersService, 'isAuthenticated').and.returnValue(true);
      spyOn(this.usersService, 'login').and.returnValue(this.$q.when('OK'));
      spyOn(this.$state, 'go').and.callFake(function () {});
      this.createController();
      this.scope.vm.login({});
      this.scope.$digest();
      expect(this.$state.go).toHaveBeenCalledWith('home');
    });

    it('displays information modal on bad login attempt', function() {
      var modalService, modalServiceCallArgs;

      modalService = this.badLoginAttempt('invalid email or password');
      expect(modalService.showModal.calls.count()).toBe(1);

      modalServiceCallArgs = modalService.showModal.calls.allArgs()[0];

      expect(modalServiceCallArgs[0]).toEqual({});
      expect(modalServiceCallArgs[1].closeButtonText).toBe('Cancel');
      expect(modalServiceCallArgs[1].actionButtonText).toBe('Retry');
      expect(modalServiceCallArgs[1].headerHtml).toBe('Invalid login credentials');
      expect(modalServiceCallArgs[1].bodyHtml).toBe('The email and / or password you entered are invalid.');
    });

    it('displays information modal on login attempt for an non active user', function() {
      var modalService, modalServiceCallArgs;

      modalService = this.badLoginAttempt('the user is not active');
      expect(modalService.showModal.calls.count()).toBe(1);

      modalServiceCallArgs = modalService.showModal.calls.allArgs()[0];
      expect(modalServiceCallArgs[0].templateUrl).toBe('/assets/javascripts/common/modalOk.html');
      expect(modalServiceCallArgs[1].headerHtml).toBe('Login not active');
      expect(modalServiceCallArgs[1].bodyHtml).toContain('Your login is not active yet.');
    });

    it('displays information modal on login attempt for an locked user', function() {
      var modalService, modalServiceCallArgs;

      modalService = this.badLoginAttempt('the user is locked');
      expect(modalService.showModal.calls.count()).toBe(1);

      modalServiceCallArgs = modalService.showModal.calls.allArgs()[0];
      expect(modalServiceCallArgs[0].templateUrl).toBe('/assets/javascripts/common/modalOk.html');
      expect(modalServiceCallArgs[1].headerHtml).toBe('Login is locked');
      expect(modalServiceCallArgs[1].bodyHtml).toContain('Your login is locked.');
    });

    it('displays information modal on login attempt for an locked user', function() {
      var modalService, modalServiceCallArgs;

      modalService = this.badLoginAttempt('xxxx');
      expect(modalService.showModal.calls.count()).toBe(1);

      modalServiceCallArgs = modalService.showModal.calls.allArgs()[0];
      expect(modalServiceCallArgs[0].templateUrl).toBe('/assets/javascripts/common/modalOk.html');
      expect(modalServiceCallArgs[1].headerHtml).toBe('Login error');
      expect(modalServiceCallArgs[1].bodyHtml).toContain('Cannot login:');
    });

  });

});
