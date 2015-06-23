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

  describe('Controller: LoginCtrl', function() {
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

        $controller('LoginCtrl as vm', {
          $scope:       scope,
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
      expect(scope.vm.credentials.email).toBeEmptyString();
      expect(scope.vm.credentials.password).toBeEmptyString();
    });

    it('returns to home state if user is authenticated', function() {
      var $state       = this.$injector.get('$state'),
          usersService = this.$injector.get('usersService');

      spyOn(usersService, 'isAuthenticated').and.callFake(function () {
        return true;
      });
      spyOn($state, 'go').and.callFake(function () {});
      createController();
      expect($state.go).toHaveBeenCalledWith('home');
    });

    it('returns to home state after successful login', function() {
      var $q           = this.$injector.get('$q'),
          $state       = this.$injector.get('$state'),
          usersService = this.$injector.get('usersService'),
          scope;

      spyOn(usersService, 'isAuthenticated').and.callFake(function () {
        return false;
      });

      spyOn(usersService, 'login').and.callFake(function () {
        return $q.when('OK');
      });
      spyOn($state, 'go').and.callFake(function () {});
      scope = createController();
      scope.vm.login({});
      scope.$digest();
    });

    it('displays information modal on bad login attempt', function() {
      var modalService, modalServiceCallArgs;

      modalService = badLoginAttempt(this.$injector, 'invalid email or password');
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

      modalService = badLoginAttempt(this.$injector, 'the user is not active');
      expect(modalService.showModal.calls.count()).toBe(1);

      modalServiceCallArgs = modalService.showModal.calls.allArgs()[0];
      expect(modalServiceCallArgs[0].templateUrl).toBe('/assets/javascripts/common/modalOk.html');
      expect(modalServiceCallArgs[1].headerHtml).toBe('Login not active');
      expect(modalServiceCallArgs[1].bodyHtml).toContain('Your login is not active yet.');
    });

    it('displays information modal on login attempt for an locked user', function() {
      var modalService, modalServiceCallArgs;

      modalService = badLoginAttempt(this.$injector, 'the user is locked');
      expect(modalService.showModal.calls.count()).toBe(1);

      modalServiceCallArgs = modalService.showModal.calls.allArgs()[0];
      expect(modalServiceCallArgs[0].templateUrl).toBe('/assets/javascripts/common/modalOk.html');
      expect(modalServiceCallArgs[1].headerHtml).toBe('Login is locked');
      expect(modalServiceCallArgs[1].bodyHtml).toContain('Your login is locked.');
    });

    it('displays information modal on login attempt for an locked user', function() {
      var modalService, modalServiceCallArgs;

      modalService = badLoginAttempt(this.$injector, 'xxxx');
      expect(modalService.showModal.calls.count()).toBe(1);

      modalServiceCallArgs = modalService.showModal.calls.allArgs()[0];
      expect(modalServiceCallArgs[0].templateUrl).toBe('/assets/javascripts/common/modalOk.html');
      expect(modalServiceCallArgs[1].headerHtml).toBe('Login error');
      expect(modalServiceCallArgs[1].bodyHtml).toContain('Cannot login:');
    });

    function badLoginAttempt(injector, replyMessage) {
      var $q           = injector.get('$q'),
          modalService = injector.get('modalService'),
          $state       = injector.get('$state'),
          usersService = injector.get('usersService'),
          scope;

      spyOn(modalService, 'showModal').and.callFake(function () {
        return $q.when('OK');
      });
      spyOn(usersService, 'isAuthenticated').and.callFake(function () {
        return false;
      });

      spyOn(usersService, 'login').and.callFake(function () {
        var deferred = $q.defer();
        deferred.reject({ status: 'error', data: { message: replyMessage } });
        return deferred.promise;
      });

      spyOn($state, 'go').and.callFake(function () {});

      scope = createController();
      scope.vm.login({});
      scope.$digest();

      expect($state.go).toHaveBeenCalledWith('home.users.login', {}, { reload: true });
      return modalService;
    }

  });

});
