/**
 * Jasmine test suite
 */
define([
  'angular',
  'angularMocks',
  'underscore',
  'biobankApp'
], function(angular, mocks, _) {
  'use strict';

  describe('Controller: RegisterUserCtrl', function() {
    var createController;

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function() {
      createController = setupController(this.$injector);
    }));

    function setupController(injector) {
      var $rootScope           = injector.get('$rootScope'),
          $controller          = injector.get('$controller'),
          $state               = injector.get('$state'),
          User                 = injector.get('User'),
          notificationsService = injector.get('notificationsService');

      return create;

      //--

      function create() {
        var scope = $rootScope.$new();

        $controller('RegisterUserCtrl as vm', {
          $scope:               scope,
          $state:               $state,
          User:                 User,
          notificationsService: notificationsService
        });
        scope.$digest();
        return scope;
      }
    }

    it('has valid scope', function() {
      var User  = this.$injector.get('User'),
          scope = createController();

      expect(scope.vm.user).toEqual(new User());
      expect(scope.vm.password).toBeEmptyString();
      expect(scope.vm.confirmPassword).toBeEmptyString();
    });

    it('displays login page after successful registration', function() {
      var $q     = this.$injector.get('$q'),
          $state = this.$injector.get('$state'),
          User   = this.$injector.get('User'),
          scope;

      spyOn(User.prototype, 'register').and.callFake(function () {
        return $q.when('ok');
      });
      spyOn($state, 'go').and.callFake(function () {});
      scope = createController();
      scope.vm.submit({});
      scope.$digest();
      expect($state.go).toHaveBeenCalledWith('home.users.login');
    });

    it('displays a notification after registering an already registered email address', function() {
      var $q                   = this.$injector.get('$q'),
          User                 = this.$injector.get('User'),
          notificationsService = this.$injector.get('notificationsService'),
          scope;

      spyOn(User.prototype, 'register').and.callFake(function () {
        var deferred = $q.defer();
        deferred.reject({ status: 403, data: { message: 'already registered' } });
        return deferred.promise;
      });
      spyOn(notificationsService, 'error').and.callFake(function () {});
      scope = createController();
      scope.vm.submit({});
      scope.$digest();
      expect(notificationsService.error).toHaveBeenCalled();
    });

    it('displays a notification after registration failure', function() {
      var $q                   = this.$injector.get('$q'),
          User                 = this.$injector.get('User'),
          notificationsService = this.$injector.get('notificationsService'),
          scope;

      spyOn(User.prototype, 'register').and.callFake(function () {
        var deferred = $q.defer();
        deferred.reject({ status: 401, data: { message: 'xxx' } });
        return deferred.promise;
      });
      spyOn(notificationsService, 'error').and.callFake(function () {});
      scope = createController();
      scope.vm.submit({});
      scope.$digest();
      expect(notificationsService.error).toHaveBeenCalled();
    });

    it('goes to home state when cancel button is pressed', function() {
      var $state = this.$injector.get('$state'),
          scope;

      spyOn($state, 'go').and.callFake(function () {});
      scope = createController();
      scope.vm.cancel();
      scope.$digest();
      expect($state.go).toHaveBeenCalledWith('home');
    });

  });

});
