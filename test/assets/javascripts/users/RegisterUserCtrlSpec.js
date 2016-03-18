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
  'biobankApp'
], function(angular, mocks, _) {
  'use strict';

  describe('Controller: RegisterUserCtrl', function() {

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function($rootScope, $controller, $state) {
      var self = this;

      self.$q                   = self.$injector.get('$q');
      self.$state               = this.$injector.get('$state');
      self.User                 = self.$injector.get('User');
      self.notificationsService = self.$injector.get('notificationsService');

      self.createController = createController;

      function createController(injector) {
        self.scope = $rootScope.$new();

        $controller('RegisterUserCtrl as vm', {
          $scope:               self.scope,
          $state:               $state,
          User:                 self.User,
          notificationsService: self.notificationsService
        });
        self.scope.$digest();
      }
    }));

    it('has valid scope', function() {
      var User  = this.$injector.get('User');

      this.createController();
      expect(this.scope.vm.user).toEqual(new User());
      expect(this.scope.vm.password).toBeEmptyString();
      expect(this.scope.vm.confirmPassword).toBeEmptyString();
    });

    it('displays login page after successful registration', function() {
      spyOn(this.User.prototype, 'register').and.returnValue(this.$q.when('ok'));
      spyOn(this.$state, 'go').and.callFake(function () {});
      this.createController();
      this.scope.vm.submit({});
      this.scope.$digest();
      expect(this.$state.go).toHaveBeenCalledWith('home.users.login');
    });

    it('displays a notification after registering an already registered email address', function() {
      var deferred = this.$q.defer();

      spyOn(this.User.prototype, 'register').and.returnValue(deferred.promise);
      spyOn(this.notificationsService, 'error').and.returnValue(this.$q.when('ok'));
      this.createController();
      this.scope.vm.submit({});
      deferred.reject({ status: 403, data: { message: 'already registered' } });
      this.scope.$digest();
      expect(this.notificationsService.error).toHaveBeenCalled();
    });

    it('displays a notification after registration failure', function() {
      var deferred = this.$q.defer();

      spyOn(this.User.prototype, 'register').and.returnValue(deferred.promise);
      spyOn(this.notificationsService, 'error').and.callFake(function () {});
      this.createController();
      this.scope.vm.submit({});
      deferred.reject({ status: 401, data: { message: 'xxx' } });
      this.scope.$digest();
      expect(this.notificationsService.error).toHaveBeenCalled();
    });

    it('goes to home state when cancel button is pressed', function() {
      spyOn(this.$state, 'go').and.callFake(function () {});
      this.createController();
      this.scope.vm.cancel();
      this.scope.$digest();
      expect(this.$state.go).toHaveBeenCalledWith('home');
    });

  });

});
