/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(function (require) {
  'use strict';

  var angular = require('angular'),
      mocks   = require('angularMocks'),
      _       = require('underscore');

  describe('Directive: userProfileDirective', function() {

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function($rootScope, $compile, directiveTestSuite, testUtils) {
      var self = this;

      _.extend(self, directiveTestSuite);

      self.$q                   = this.$injector.get('$q');
      self.jsonEntities         = this.$injector.get('jsonEntities');
      self.$uibModal            = this.$injector.get('$uibModal');
      self.modalService         = this.$injector.get('modalService');
      self.modalInput           = this.$injector.get('modalInput');
      self.User                 = this.$injector.get('User');
      self.notificationsService = this.$injector.get('notificationsService');

      self.createController = createController;
      self.updateUserCommon = updateUserCommon;
      self.ctrlMethods = ['updateName', 'updateEmail', 'updateAvatarUrl'];

      self.putHtmlTemplates(
        '/assets/javascripts/users/directives/userProfile/userProfile.html',
        '/assets/javascripts/common/directives/updateRemoveButtons.html',
        '/assets/javascripts/common/modalInput/modalInput.html',
        '/assets/javascripts/common/modalInput/boolean.html',
        '/assets/javascripts/common/modalInput/dateTime.html',
        '/assets/javascripts/common/modalInput/email.html',
        '/assets/javascripts/common/modalInput/naturalNumber.html',
        '/assets/javascripts/common/modalInput/number.html',
        '/assets/javascripts/common/modalInput/password.html',
        '/assets/javascripts/common/modalInput/positiveFloat.html',
        '/assets/javascripts/common/modalInput/select.html',
        '/assets/javascripts/common/modalInput/selectMultiple.html',
        '/assets/javascripts/common/modalInput/textArea.html',
        '/assets/javascripts/common/modalInput/text.html',
        '/assets/javascripts/common/modalInput/url.html',
        '/assets/javascripts/common/directives/dateTime/dateTime.html');

      //--

      function createController(user) {
        self.element = angular.element('<user-profile user="vm.user"></user-profile>');
        self.scope = $rootScope.$new();
        self.scope.vm = { user: user};

        $compile(self.element)(self.scope);
        self.scope.$digest();
        self.controller = self.element.controller('userProfile');
      }

      function updateUserCommon(fakeUserUpdate, expectClause) {
        var user = self.jsonEntities.user(),
            deferred = self.$q.defer();

        deferred.resolve('OK');
        spyOn(self.modalInput, 'text').and.returnValue({ result: deferred.promise });
        self.createController(user);

        _.each(self.ctrlMethods, function (ctrlMethod) {
          spyOn(self.User.prototype, ctrlMethod).and.returnValue(fakeUserUpdate());

          self.controller[ctrlMethod]();
          self.scope.$digest();
          expectClause();
        });
      }

    }));

    it('should have valid scope', function() {
      var user = this.jsonEntities.user();

      this.createController(user);
      expect(this.scope.vm.user).toEqual(new this.User(user));
    });

    it('should update a users name, email and avatar URL', function() {
      var self = this;

      spyOn(self.notificationsService, 'success').and.callFake(function () {});
      self.updateUserCommon(fakeUserUpdate, expectClause);

      function fakeUserUpdate() {
        return self.$q.when(new self.User());
      }

      function expectClause() {
        expect(self.notificationsService.success).toHaveBeenCalled();
      }
    });

    it('should display a notification error when update fails', function() {
      var self = this,
          deferred = this.$q.defer();

      spyOn(self.notificationsService, 'error').and.callFake(function () {});
      deferred.reject({ data: { message: 'update failed' } });
      self.updateUserCommon(fakeUserUpdate, expectClause);

      function fakeUserUpdate() {
        return deferred.promise;
      }

      function expectClause() {
        expect(self.notificationsService.error).toHaveBeenCalled();
      }
    });

    it('can remove a users avatar', function() {
      var user = this.jsonEntities.user();

      spyOn(this.modalService, 'showModal').and.returnValue(this.$q.when('OK'));
      spyOn(this.User.prototype, 'updateAvatarUrl').and.returnValue(this.$q.when(new this.User()));
      spyOn(this.notificationsService, 'success').and.callFake(function () {});

      this.createController(user);
      this.controller.removeAvatarUrl();
      this.scope.$digest();
      expect(this.notificationsService.success).toHaveBeenCalled();
    });

    it('should display a notification error when removing avatar URL fails', function() {
      var deferred = this.$q.defer(),
          user = this.jsonEntities.user();

      spyOn(this.modalService, 'showModal').and.returnValue(this.$q.when('OK'));
      spyOn(this.User.prototype, 'updateAvatarUrl').and.returnValue(deferred.promise);
      spyOn(this.notificationsService, 'error').and.callFake(function () {});

      deferred.reject({ data: { message: 'xxx' } });

      this.createController(user);
      this.controller.removeAvatarUrl();
      this.scope.$digest();
      expect(this.notificationsService.error).toHaveBeenCalled();
    });

    it('can update users password', function() {
      var deferred = this.$q.defer(),
          user = this.jsonEntities.user();

      deferred.resolve({ currentPassword: 'xx', newPassword: 'xx' });
      spyOn(this.modalInput, 'password').and.returnValue({ result: deferred.promise });
      spyOn(this.User.prototype, 'updatePassword').and.returnValue(this.$q.when(new this.User()));
      spyOn(this.notificationsService, 'success').and.callFake(function () {});

      this.createController(user);
      this.controller.updatePassword();
      this.scope.$digest();
      expect(this.notificationsService.success).toHaveBeenCalled();
    });

    it('should display a notification error when current password is invalid', function() {
      var passwordDeferred = this.$q.defer(),
          deferred = this.$q.defer(),
          user = this.jsonEntities.user();

      spyOn(this.modalInput, 'password').and.returnValue({ result: passwordDeferred.promise });
      spyOn(this.User.prototype, 'updatePassword').and.returnValue(deferred.promise);
      spyOn(this.notificationsService, 'error').and.callFake(function () {});
      passwordDeferred.resolve({ currentPassword: 'xx', newPassword: 'xx' });
      deferred.reject({ data: { message: 'invalid password' } });

      this.createController(user);
      this.controller.updatePassword();
      this.scope.$digest();
      expect(this.notificationsService.error).toHaveBeenCalled();
    });

    it('should display a notification error when updating password fails', function() {
      var passwordDeferred = this.$q.defer(),
          deferred = this.$q.defer(),
          user = this.jsonEntities.user();

      spyOn(this.modalInput, 'password').and.returnValue({ result: passwordDeferred.promise });
      spyOn(this.User.prototype, 'updatePassword').and.returnValue(deferred.promise);
      spyOn(this.notificationsService, 'error').and.callFake(function () {});
      passwordDeferred.resolve({ currentPassword: 'xx', newPassword: 'xx' });
      deferred.reject({ data: { message: 'xxx' } });

      this.createController(user);
      this.controller.updatePassword();
      this.scope.$digest();
      expect(this.notificationsService.error).toHaveBeenCalled();
    });

  });

});
