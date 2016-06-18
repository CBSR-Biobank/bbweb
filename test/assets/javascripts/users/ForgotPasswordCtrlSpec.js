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
  'faker'
], function(angular, mocks, _, faker) {
  'use strict';

  describe('Controller: ForgotPasswordCtrl', function() {

    var createController = function () {
        this.scope = this.$rootScope.$new();

        this.$controller('ForgotPasswordCtrl as vm', {
          $scope:       this.scope,
          $state:       this.$state,
          usersService: this.usersService,
          modalService: this.modalService
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
                              'usersService',
                              'modalService',
                              'notificationsService');
    }));

    it('has valid scope', function() {
      createController.call(this);
      expect(this.scope.vm.email).toBe('');
    });

    it('goes to correct state on submit', function() {
      var email        = faker.internet.email();

      spyOn(this.usersService, 'passwordReset').and.returnValue(this.$q.when('OK'));
      spyOn(this.$state, 'go').and.callFake(function () {});

      createController.call(this);
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

      createController.call(this);
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

      createController.call(this);
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

      createController.call(this);
      this.scope.vm.submit(email);
      this.scope.$digest();
      expect(this.$state.go).toHaveBeenCalledWith('home');
    });

  });

});
