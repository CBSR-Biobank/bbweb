/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
/* global angular */

import _ from 'lodash';

describe('Component: registerUser', function() {

  beforeEach(() => {
    angular.mock.module('biobankApp', 'biobank.test');
    angular.mock.inject(function(TestSuiteMixin, ServerReplyMixin) {
      _.extend(this, TestSuiteMixin.prototype, ServerReplyMixin.prototype);
      this.injectDependencies('$rootScope',
                              '$compile',
                              '$q',
                              '$state',
                              'User',
                              'notificationsService');

      spyOn(this.$state, 'go').and.returnValue(null);

      this.createController = () => {
        this.element = angular.element('<register-user></register-user>');
        this.scope = this.$rootScope.$new();
        this.$compile(this.element)(this.scope);
        this.scope.$digest();
        this.controller = this.element.controller('registerUser');
      };
    });
  });

  it('has valid scope', function() {
    this.createController();
    expect(this.controller.user).toEqual(new this.User());
    expect(this.controller.password).toBeEmptyString();
    expect(this.controller.confirmPassword).toBeEmptyString();
  });

  it('displays login page after successful registration', function() {
    spyOn(this.User.prototype, 'register').and.returnValue(this.$q.when('ok'));
    this.createController();
    this.controller.submit({});
    this.scope.$digest();
    expect(this.$state.go).toHaveBeenCalledWith('home.users.login');
  });

  it('displays a notification after registering an already registered email address', function() {
    this.createController();
    spyOn(this.User.prototype, 'register').and.returnValue(
      this.$q.reject({ status: 403, data: { message: 'already registered' } }));
    spyOn(this.notificationsService, 'error').and.returnValue(null);
    this.controller.submit({});
    this.scope.$digest();
    expect(this.notificationsService.error).toHaveBeenCalled();
  });

  it('displays a notification after registration failure', function() {
    this.createController();
    spyOn(this.User.prototype, 'register').and.returnValue(
      this.$q.reject({ status: 401, data: { message: 'xxx' } }));
    spyOn(this.notificationsService, 'error').and.returnValue(null);
    this.controller.submit({});
    this.scope.$digest();
    expect(this.notificationsService.error).toHaveBeenCalled();
  });

  it('goes to home state when cancel button is pressed', function() {
    this.createController();
    this.controller.cancel();
    this.scope.$digest();
    expect(this.$state.go).toHaveBeenCalledWith('home');
  });

});
