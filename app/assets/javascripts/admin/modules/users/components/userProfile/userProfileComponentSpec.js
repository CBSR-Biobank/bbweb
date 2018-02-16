/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
/* global angular */

import _ from 'lodash';
import ngModule from '../../index'

describe('Component: userProfile', function() {

  beforeEach(() => {
    angular.mock.module(ngModule, 'biobank.test');
    angular.mock.inject(function(ComponentTestSuiteMixin) {
      _.extend(this, ComponentTestSuiteMixin);

      this.injectDependencies('$rootScope',
                              '$compile',
                              '$q',
                              'Factory',
                              '$uibModal',
                              'modalService',
                              'modalInput',
                              'User',
                              'notificationsService');

      this.ctrlMethods = ['updateName', 'updateEmail', 'updateAvatarUrl'];

      this.createController = (user) => {
        ComponentTestSuiteMixin.createController.call(
          this,
          '<user-profile user="vm.user"></user-profile>',
          { user: user },
          'userProfile');
      };
    });
  });

  it('should have valid scope', function() {
    var user = this.User.create(this.Factory.user());
    this.createController(user);
    expect(this.controller.user.id).toEqual(user.id);
    expect(this.controller.user).toEqual(jasmine.any(this.User));
  });

  it('correct display when user has no memberships', function() {
    var user = new this.User(this.Factory.user({ membership: undefined }));
    this.createController(user);
    expect(this.controller.studyMembershipLabels).toBeUndefined();
    expect(this.controller.centreMembershipLabels).toBeUndefined();
  });

  it('correct display when user has membership to all studies and centres', function() {
    var user = new this.User(this.Factory.user({
      membership: {
        studyData:  { allEntities: true, entityData: [] },
        centreData: { allEntities: true, entityData: [] }
      }
    }));
    this.createController(user);
    expect(this.controller.studyMembershipLabels).toBeUndefined();
    expect(this.controller.centreMembershipLabels).toBeUndefined();
  });

  it('correct display when user has membership to some studies and centres', function() {
    var studyName = this.Factory.stringNext(),
        centreName = this.Factory.stringNext(),
        user = new this.User(this.Factory.user({
          membership: {
            studyData: {
              allEntities: false,
              entityData: [{
                id: this.Factory.stringNext(),
                name: studyName
              }]
            },
            centreData: {
              allEntities: false,
              entityData: [{
                id: this.Factory.stringNext(),
                name: centreName
              }]
            }
          }
        }));
    this.createController(user);
    const studyLabels = _.map(this.controller.studyMembershipLabels, 'label');
    expect(studyLabels).toBeArrayOfSize(1);
    expect(studyLabels).toContain(studyName);

    const centreLabels = _.map(this.controller.centreMembershipLabels, 'label');
    expect(centreLabels).toBeArrayOfSize(1);
    expect(centreLabels).toContain(centreName);
  });

  describe('updates to name', function () {

    var context = {};

    beforeEach(function () {
      context.controllerFuncName = 'updateName';
      context.modalInputFuncName = 'text';
      context.modalReturnValue = this.Factory.stringNext();
      context.userUpdateFuncName = 'updateName';
    });

    sharedUpdateBehaviour(context);

  });

  describe('updates to email', function () {

    var context = {};

    beforeEach(function () {
      context.controllerFuncName = 'updateEmail';
      context.modalInputFuncName = 'email';
      context.modalReturnValue = this.Factory.emailNext();
      context.userUpdateFuncName = 'updateEmail';
    });

    sharedUpdateBehaviour(context);

  });

  describe('updates to avatar URL', function () {

    var context = {};

    beforeEach(function () {
      context.controllerFuncName = 'updateAvatarUrl';
      context.modalInputFuncName = 'url';
      context.modalReturnValue = this.Factory.urlNext();
      context.userUpdateFuncName = 'updateAvatarUrl';
    });

    sharedUpdateBehaviour(context);

  });

  it('can remove a users avatar', function() {
    var user = this.User.create(this.Factory.user());

    spyOn(this.modalService, 'modalOkCancel').and.returnValue(this.$q.when('OK'));
    spyOn(this.User.prototype, 'updateAvatarUrl').and.returnValue(this.$q.when(user));
    spyOn(this.notificationsService, 'success').and.returnValue(null);

    this.createController(user);
    this.controller.removeAvatarUrl();
    this.scope.$digest();
    expect(this.notificationsService.success).toHaveBeenCalled();
  });

  it('should display a notification error when removing avatar URL fails', function() {
    var user = new this.User(this.Factory.user());

    this.createController(user);

    spyOn(this.modalService, 'modalOkCancel').and.returnValue(this.$q.when('OK'));
    spyOn(this.User.prototype, 'updateAvatarUrl')
      .and.returnValue(this.$q.reject({ data: { message: 'xxx' } }));
    spyOn(this.notificationsService, 'updateError').and.returnValue(null);

    this.controller.removeAvatarUrl();
    this.scope.$digest();
    expect(this.notificationsService.updateError).toHaveBeenCalled();
  });

  it('can update users password', function() {
    const user = this.User.create(this.Factory.user());

    this.createController(user);

    spyOn(this.modalInput, 'password').and.returnValue(
      { result : this.$q.when({ currentPassword: 'xx', newPassword: 'xx' })});
    spyOn(this.User.prototype, 'updatePassword').and.returnValue(this.$q.when(user));
    spyOn(this.notificationsService, 'success').and.callFake(function () {});

    this.controller.updatePassword();
    this.scope.$digest();
    expect(this.notificationsService.success).toHaveBeenCalled();
  });

  it('should display a notification error when current password is invalid', function() {
    var user = new this.User(this.Factory.user());
    this.createController(user);

    spyOn(this.modalInput, 'password').and.returnValue(
      { result : this.$q.when({ currentPassword: 'xx', newPassword: 'xx' })});
    spyOn(this.User.prototype, 'updatePassword').and.returnValue(
      this.$q.reject({ data: { message: 'invalid password' } }));
    spyOn(this.notificationsService, 'error').and.callFake(function () {});

    this.controller.updatePassword();
    this.scope.$digest();
    expect(this.notificationsService.error).toHaveBeenCalled();
  });

  it('should display a notification error when updating password fails', function() {
    var user = new this.User(this.Factory.user());

    spyOn(this.modalInput, 'password').and.returnValue(
      { result: this.$q.when({ currentPassword: 'xx', newPassword: 'xx' })});
    spyOn(this.notificationsService, 'updateError').and.returnValue(null);

    this.createController(user);

    spyOn(this.User.prototype, 'updatePassword').and.returnValue(
      this.$q.reject({ data: { message: 'xxx' } }));

    this.controller.updatePassword();
    this.scope.$digest();
    expect(this.notificationsService.updateError).toHaveBeenCalled();
  });

  function sharedUpdateBehaviour(context) {

    beforeEach(function () {
      this.injectDependencies('modalInput', 'notificationsService');
      this.user = new this.User(this.Factory.user());
    });

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
        this.createController(this.user);

        spyOn(this.modalInput, context.modalInputFuncName)
          .and.returnValue({ result: this.$q.when(context.modalReturnValue)});
        spyOn(this.User.prototype, context.userUpdateFuncName)
          .and.returnValue(this.$q.reject({ data: { message: 'simulated error'}}));
        spyOn(this.notificationsService, 'updateError').and.returnValue(this.$q.when('OK'));

        this.controller[context.controllerFuncName]();
        this.scope.$digest();

        expect(this.notificationsService.updateError).toHaveBeenCalled();
      });

    });
  }

});
