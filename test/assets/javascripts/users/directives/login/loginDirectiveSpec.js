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

    it('displays a modal on invalid login attempt', function () {
      var deferred = this.$q.defer();

      spyOn(this.usersService, 'login').and.returnValue(deferred.promise);
      spyOn(this.modalService, 'showModal').and.returnValue(this.$q.when('OK'));
      spyOn(this.$state, 'go').and.returnValue(true);
      deferred.reject({});

      this.createController();
      this.controller.login({ email: 'test@test.com', password: 'secret-password' });
      this.scope.$digest();

      expect(this.modalService.showModal).toHaveBeenCalled();
      expect(this.$state.go).toHaveBeenCalledWith('home.users.login', {}, { reload: true });
    });

  });

});
