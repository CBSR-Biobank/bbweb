/**
 * Jasmine test suite
 */
define([
  'angular',
  'angularMocks',
  'underscore',
  'faker',
  'biobankApp'
], function(angular, mocks, _, faker) {
  'use strict';

  describe('Controller: ForgotPasswordCtrl', function() {
    var createController;

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function() {
      createController = setupController(this.$injector);
    }));

    function setupController(injector) {
      var $rootScope   = injector.get('$rootScope'),
          $controller  = injector.get('$controller'),
          $state       = injector.get('$state'),
          usersService = injector.get('usersService'),
          modalService = injector.get('modalService');

      return create;

      //--

      function create() {
        var scope = $rootScope.$new();

        $controller('ForgotPasswordCtrl as vm', {
          $scope: scope,
          $state:       $state,
          usersService: usersService,
          modalService: modalService
        });
        scope.$digest();
        return scope;
      }
    }

    it('has valid scope', function() {
      var scope = createController();
      expect(scope.vm.email).toBe('');
    });

    it('goes to correct state on submit', function() {
      var $q           = this.$injector.get('$q'),
          $state       = this.$injector.get('$state'),
          usersService = this.$injector.get('usersService'),
          scope        = createController(),
          email        = faker.internet.email();

      spyOn(usersService, 'passwordReset').and.callFake(function () {
        return $q.when('OK');
      });
      spyOn($state, 'go').and.callFake(function () {});

      scope.vm.submit(email);
      scope.$digest();
      expect($state.go).toHaveBeenCalledWith('home.users.forgot.passwordSent',
                                             { email: email });
    });

    it('goes to correct state if email is not registered', function() {
      var $q           = this.$injector.get('$q'),
          $state       = this.$injector.get('$state'),
          usersService = this.$injector.get('usersService'),
          scope        = createController(),
          email        = faker.internet.email();

      spyOn(usersService, 'passwordReset').and.callFake(function () {
        var deferred = $q.defer();
        deferred.reject({ status: 'error', message: 'email address not registered'});
        return deferred.promise;
      });
      spyOn($state, 'go').and.callFake(function () {});

      scope.vm.submit(email);
      scope.$digest();
      expect($state.go).toHaveBeenCalledWith('home.users.forgot.emailNotFound');
    });

    it('displays information in modal on password reset failure and user presses OK', function() {
      var $q           = this.$injector.get('$q'),
          $state       = this.$injector.get('$state'),
          usersService = this.$injector.get('usersService'),
          modalService = this.$injector.get('modalService'),
          scope        = createController(),
          email        = faker.internet.email();

      spyOn(usersService, 'passwordReset').and.callFake(function () {
        var deferred = $q.defer();
        deferred.reject({ status: 'error', message: 'xxxx'});
        return deferred.promise;
      });
      spyOn(modalService, 'modalOk').and.callFake(function () {
        return $q.when('OK');
      });
      spyOn($state, 'go').and.callFake(function () {});

      scope.vm.submit(email);
      scope.$digest();
      expect($state.go).toHaveBeenCalledWith('home');
    });

    it('displays information in modal on password reset failure and user closes window', function() {
      var $q           = this.$injector.get('$q'),
          $state       = this.$injector.get('$state'),
          usersService = this.$injector.get('usersService'),
          modalService = this.$injector.get('modalService'),
          scope        = createController(),
          email        = faker.internet.email();

      spyOn(usersService, 'passwordReset').and.callFake(function () {
        var deferred = $q.defer();
        deferred.reject({ status: 'error', message: 'xxxx'});
        return deferred.promise;
      });
      spyOn(modalService, 'modalOk').and.callFake(function () {
        var deferred = $q.defer();
        deferred.reject();
        return deferred.promise;
      });
      spyOn($state, 'go').and.callFake(function () {});

      scope.vm.submit(email);
      scope.$digest();
      expect($state.go).toHaveBeenCalledWith('home');
    });

  });

});
