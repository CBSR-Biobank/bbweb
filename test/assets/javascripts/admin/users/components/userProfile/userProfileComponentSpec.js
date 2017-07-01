/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(function (require) {
  'use strict';

  var mocks = require('angularMocks'),
      _     = require('lodash');

  describe('Directive: userProfileDirective', function() {

    function SuiteMixinFactory(ComponentTestSuiteMixin) {

      function SuiteMixin() {
      }

      SuiteMixin.prototype = Object.create(ComponentTestSuiteMixin.prototype);
      SuiteMixin.prototype.constructor = SuiteMixin;

      SuiteMixin.prototype.createController = function (user) {
        ComponentTestSuiteMixin.prototype.createController.call(
          this,
          '<user-profile user="vm.user"></user-profile>',
          { user: user },
          'userProfile');
      };

      return SuiteMixin;
    }

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function(ComponentTestSuiteMixin) {
      _.extend(this, new SuiteMixinFactory(ComponentTestSuiteMixin).prototype);

      this.injectDependencies('$rootScope',
                              '$compile',
                              '$q',
                              'factory',
                              '$uibModal',
                              'modalService',
                              'modalInput',
                              'User',
                              'notificationsService');

      this.ctrlMethods = ['updateName', 'updateEmail', 'updateAvatarUrl'];

      this.putHtmlTemplates(
        '/assets/javascripts/admin/users/components/userProfile/userProfile.html',
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
        '/assets/javascripts/common/components/statusLine/statusLine.html',
        '/assets/javascripts/common/components/breadcrumbs/breadcrumbs.html');
    }));

    it('should have valid scope', function() {
      var user = new this.User(this.factory.user());
      this.createController(user);
      expect(this.controller.user.id).toEqual(user.id);
      expect(this.controller.user).toEqual(jasmine.any(this.User));
    });

    it('correct display when user has no memberships', function() {
      var user = new this.User(this.factory.user({ membership: undefined }));
      this.createController(user);
      expect(this.controller.studyMemberships).toEqual('None');
      expect(this.controller.centreMemberships).toEqual('None');
    });

    it('correct display when user has membership to all studies and centres', function() {
      var user = new this.User(this.factory.user({
        membership: {
          studyInfo:  { all: true, names: [] },
          centreInfo: { all: true, names: [] }
        }
      }));
      this.createController(user);
      expect(this.controller.studyMemberships).toEqual('All Studies');
      expect(this.controller.centreMemberships).toEqual('All Centres');
    });

    it('correct display when user has membership to some studies and centres', function() {
      var studyName = this.factory.stringNext(),
          centreName = this.factory.stringNext(),
          user = new this.User(this.factory.user({
            membership: {
              studyInfo:  { all: false, names: [ studyName ] },
              centreInfo: { all: false, names: [ centreName ] }
            }
          }));
      this.createController(user);
      expect(this.controller.studyMemberships).toEqual(studyName);
      expect(this.controller.centreMemberships).toEqual(centreName);
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

      spyOn(this.modalService, 'modalOkCancel').and.returnValue(this.$q.when('OK'));
      spyOn(this.User.prototype, 'updateAvatarUrl').and.returnValue(this.$q.when(new this.User()));
      spyOn(this.notificationsService, 'success').and.returnValue(null);

      this.createController(user);
      this.controller.removeAvatarUrl();
      this.scope.$digest();
      expect(this.notificationsService.success).toHaveBeenCalled();
    });

    it('should display a notification error when removing avatar URL fails', function() {
      var deferred = this.$q.defer(),
          user = new this.User(this.factory.user());

      spyOn(this.modalService, 'modalOkCancel').and.returnValue(this.$q.when('OK'));
      spyOn(this.User.prototype, 'updateAvatarUrl').and.returnValue(deferred.promise);
      spyOn(this.notificationsService, 'updateError').and.returnValue(null);

      deferred.reject({ data: { message: 'xxx' } });

      this.createController(user);
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

      this.createController(user);
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

      this.createController(user);
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

      this.createController(user);
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

          this.createController(this.user);
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

          this.createController(this.user);
          this.controller[context.controllerFuncName]();
          this.scope.$digest();

          expect(this.notificationsService.updateError).toHaveBeenCalled();
        });

      });
    }

  });

});
