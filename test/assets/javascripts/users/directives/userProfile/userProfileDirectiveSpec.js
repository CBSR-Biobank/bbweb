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

    var createController = function (user) {
      this.$injector.get('usersService').requestCurrentUser =
        jasmine.createSpy().and.returnValue(this.$q.when(user));

      this.element = angular.element('<user-profile user="vm.user"></user-profile>');
      this.scope = this.$rootScope.$new();
      this.scope.vm = { user: user};

      this.$compile(this.element)(this.scope);
      this.scope.$digest();
      this.controller = this.element.controller('userProfile');
    };

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function($rootScope, $compile, testSuiteMixin, testUtils) {
      var self = this;

      _.extend(self, testSuiteMixin);

      self.injectDependencies('$rootScope',
                              '$compile',
                              '$q',
                              'factory',
                              '$uibModal',
                              'modalService',
                              'modalInput',
                              'User',
                              'notificationsService');

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
        '/assets/javascripts/common/modalInput/url.html');
    }));

    it('should have valid scope', function() {
      var user = this.factory.user();

      createController.call(this, user);
      expect(this.scope.vm.user).toEqual(user);
    });

    describe('updates to name', function () {

      var context = {};

      beforeEach(inject(function () {
        context.controllerFuncName = 'updateName';
        context.modalInputFuncName = 'text';
        context.modalReturnValue = this.factory.stringNext();
        context.userUpdateFuncName = 'updateName';
      }));

      sharedUpdateBehaviour(context);

    });

    describe('updates to email', function () {

      var context = {};

      beforeEach(inject(function () {
        context.controllerFuncName = 'updateEmail';
        context.modalInputFuncName = 'email';
        context.modalReturnValue = this.factory.emailNext();
        context.userUpdateFuncName = 'updateEmail';
      }));

      sharedUpdateBehaviour(context);

    });

    describe('updates to avatar URL', function () {

      var context = {};

      beforeEach(inject(function () {
        context.controllerFuncName = 'updateAvatarUrl';
        context.modalInputFuncName = 'url';
        context.modalReturnValue = this.factory.urlNext();
        context.userUpdateFuncName = 'updateAvatarUrl';
      }));

      sharedUpdateBehaviour(context);

    });

    it('can remove a users avatar', function() {
      var user = new this.User(this.factory.user());

      spyOn(this.modalService, 'showModal')
        .and.returnValue({ result: this.$q.when('OK')});
      spyOn(this.User.prototype, 'updateAvatarUrl').and.returnValue(this.$q.when(new this.User()));
      spyOn(this.notificationsService, 'success').and.returnValue(null);

      createController.call(this, user);
      this.controller.removeAvatarUrl();
      this.scope.$digest();
      expect(this.notificationsService.success).toHaveBeenCalled();
    });

    it('should display a notification error when removing avatar URL fails', function() {
      var deferred = this.$q.defer(),
          user = new this.User(this.factory.user());

      spyOn(this.modalService, 'showModal')
        .and.returnValue({ result: this.$q.when('OK')});
      spyOn(this.User.prototype, 'updateAvatarUrl').and.returnValue(deferred.promise);
      spyOn(this.notificationsService, 'updateError').and.returnValue(null);

      deferred.reject({ data: { message: 'xxx' } });

      createController.call(this, user);
      this.controller.removeAvatarUrl();
      this.scope.$digest();
      expect(this.notificationsService.updateError).toHaveBeenCalled();
    });

    it('can update users password', function() {
      var user = new this.User(this.factory.user());

      spyOn(this.modalInput, 'password').and.returnValue(
        {result : this.$q.when({ currentPassword: 'xx', newPassword: 'xx' })});
      spyOn(this.User.prototype, 'updatePassword').and.returnValue(this.$q.when(new this.User()));
      spyOn(this.notificationsService, 'success').and.callFake(function () {});

      createController.call(this, user);
      this.controller.updatePassword();
      this.scope.$digest();
      expect(this.notificationsService.success).toHaveBeenCalled();
    });

    it('should display a notification error when current password is invalid', function() {
      var user = new this.User(this.factory.user());

      spyOn(this.modalInput, 'password').and.returnValue(
        { result : this.$q.when({ currentPassword: 'xx', newPassword: 'xx' })});
      spyOn(this.User.prototype, 'updatePassword').and.returnValue(
        this.$q.reject({ data: { message: 'invalid password' } }));
      spyOn(this.notificationsService, 'error').and.callFake(function () {});

      createController.call(this, user);
      this.controller.updatePassword();
      this.scope.$digest();
      expect(this.notificationsService.error).toHaveBeenCalled();
    });

    it('should display a notification error when updating password fails', function() {
      var user = new this.User(this.factory.user());

      spyOn(this.modalInput, 'password').and.returnValue(
        { result: this.$q.when({ currentPassword: 'xx', newPassword: 'xx' })});
      spyOn(this.User.prototype, 'updatePassword').and.returnValue(
        this.$q.reject({ data: { message: 'xxx' } }));
      spyOn(this.notificationsService, 'updateError').and.returnValue(null);

      createController.call(this, user);
      this.controller.updatePassword();
      this.scope.$digest();
      expect(this.notificationsService.updateError).toHaveBeenCalled();
    });

    function sharedUpdateBehaviour(context) {

      beforeEach(inject(function () {
        this.injectDependencies('modalInput', 'notificationsService');
        this.user = new this.User(this.factory.user());
      }));

      describe('(shared) update functions', function () {

        it('on update should invoke the update method on entity', function() {
          spyOn(this.modalInput, context.modalInputFuncName)
            .and.returnValue({ result: this.$q.when(context.modalReturnValue)});
          spyOn(this.User.prototype, context.userUpdateFuncName)
            .and.returnValue(this.$q.when(this.user));
          spyOn(this.notificationsService, 'success').and.returnValue(this.$q.when('OK'));

          createController.call(this, this.user);
          expect(this.controller[context.controllerFuncName]).toBeFunction();
          this.controller[context.controllerFuncName]();
          this.scope.$digest();
          expect(this.User.prototype[context.userUpdateFuncName]).toHaveBeenCalled();
          expect(this.notificationsService.success).toHaveBeenCalled();
        });

        it('error message should be displayed when update fails', function() {
          spyOn(this.modalInput, context.modalInputFuncName)
            .and.returnValue({ result: this.$q.when(context.modalReturnValue)});
          spyOn(this.User.prototype, context.userUpdateFuncName)
            .and.returnValue(this.$q.reject({ data: { message: 'simulated error'}}));
          spyOn(this.notificationsService, 'updateError').and.returnValue(this.$q.when('OK'));

          createController.call(this, this.user);
          this.controller[context.controllerFuncName]();
          this.scope.$digest();

          expect(this.notificationsService.updateError).toHaveBeenCalled();
        });

      });
    }

  });

});
