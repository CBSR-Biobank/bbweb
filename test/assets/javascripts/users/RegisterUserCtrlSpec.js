/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define([
  'angular',
  'angularMocks',
  'lodash',
  'biobankApp'
], function(angular, mocks, _) {
  'use strict';

  describe('Controller: RegisterUserCtrl', function() {

    var createController = function () {
      this.scope = this.$rootScope.$new();

      this.$controller('RegisterUserCtrl as vm', {
        $scope:               this.scope,
        $state:               this.$state,
        User:                 this.User,
        notificationsService: this.notificationsService
      });
      this.scope.$digest();
    };

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function(testSuiteMixin) {
      var self = this;
      _.extend(self, testSuiteMixin);
      self.injectDependencies('$rootScope',
                              '$controller',
                              '$q',
                              '$state',
                              'User',
                              'notificationsService');
    }));

    it('has valid scope', function() {
      var User  = this.$injector.get('User');

      createController.call(this);
      expect(this.scope.vm.user).toEqual(new User());
      expect(this.scope.vm.password).toBeEmptyString();
      expect(this.scope.vm.confirmPassword).toBeEmptyString();
    });

    it('displays login page after successful registration', function() {
      spyOn(this.User.prototype, 'register').and.returnValue(this.$q.when('ok'));
      spyOn(this.$state, 'go').and.callFake(function () {});
      createController.call(this);
      this.scope.vm.submit({});
      this.scope.$digest();
      expect(this.$state.go).toHaveBeenCalledWith('home.users.login');
    });

    it('displays a notification after registering an already registered email address', function() {
      var deferred = this.$q.defer();

      spyOn(this.User.prototype, 'register').and.returnValue(deferred.promise);
      spyOn(this.notificationsService, 'error').and.returnValue(this.$q.when('ok'));
      createController.call(this);
      this.scope.vm.submit({});
      deferred.reject({ status: 403, data: { message: 'already registered' } });
      this.scope.$digest();
      expect(this.notificationsService.error).toHaveBeenCalled();
    });

    it('displays a notification after registration failure', function() {
      var deferred = this.$q.defer();

      spyOn(this.User.prototype, 'register').and.returnValue(deferred.promise);
      spyOn(this.notificationsService, 'error').and.callFake(function () {});
      createController.call(this);
      this.scope.vm.submit({});
      deferred.reject({ status: 401, data: { message: 'xxx' } });
      this.scope.$digest();
      expect(this.notificationsService.error).toHaveBeenCalled();
    });

    it('goes to home state when cancel button is pressed', function() {
      spyOn(this.$state, 'go').and.callFake(function () {});
      createController.call(this);
      this.scope.vm.cancel();
      this.scope.$digest();
      expect(this.$state.go).toHaveBeenCalledWith('home');
    });

  });

});
