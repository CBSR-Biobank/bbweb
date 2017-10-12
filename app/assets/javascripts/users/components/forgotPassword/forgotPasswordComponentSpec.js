/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
/* global angular */

import _ from 'lodash';

describe('Component: forgotPassword', function() {

  beforeEach(() => {
    angular.mock.module('biobankApp', 'biobank.test');
    angular.mock.inject(function(ComponentTestSuiteMixin) {
      _.extend(this, ComponentTestSuiteMixin);

      this.injectDependencies('$rootScope',
                              '$compile',
                              '$q',
                              '$state',
                              'usersService',
                              'modalService',
                              'notificationsService',
                              'Factory');

      this.createController = () => {
        ComponentTestSuiteMixin.createController.call(
          this,
          '<forgot-password></forgot-password>',
          undefined,
          'forgotPassword');
      };
    });
  });

  it('has valid scope', function() {
    this.createController();
    expect(this.controller.email).toBe('');
    expect(this.controller.submit).toBeFunction();
  });

  it('goes to correct state on submit', function() {
    var email = this.Factory.emailNext();

    spyOn(this.usersService, 'passwordReset').and.returnValue(this.$q.when('OK'));
    spyOn(this.$state, 'go').and.returnValue(null);

    this.createController();
    this.controller.submit(email);
    this.scope.$digest();
    expect(this.$state.go).toHaveBeenCalledWith('home.users.forgot.passwordSent',
                                                { email: email });
  });

  it('goes to correct state if email is not registered', function() {
    var email = this.Factory.emailNext();

    spyOn(this.$state, 'go').and.returnValue(null);

    this.createController();
    spyOn(this.usersService, 'passwordReset').and.returnValue(
      this.$q.reject({ status: 'error', message: 'email address not registered'}));
    this.controller.submit(email);
    this.scope.$digest();
    expect(this.$state.go).toHaveBeenCalledWith('home.users.forgot.emailNotFound');
  });

  it('displays information in modal on password reset failure and user presses OK', function() {
    var email = this.Factory.emailNext();

    spyOn(this.modalService, 'modalOk').and.returnValue(this.$q.when('OK'));
    spyOn(this.$state, 'go').and.returnValue(null);

    this.createController();
    spyOn(this.usersService, 'passwordReset').and.returnValue(
      this.$q.reject({ status: 'error', message: 'xxxx'}));
    this.controller.submit(email);
    this.scope.$digest();
    expect(this.$state.go).toHaveBeenCalledWith('home');
  });

  it('displays information in modal on password reset failure and user closes window', function() {
    var email = this.Factory.emailNext();

    spyOn(this.$state, 'go').and.returnValue(null);
    this.createController();
    spyOn(this.usersService, 'passwordReset').and.returnValue(
      this.$q.reject({ status: 'error', message: 'xxxx'}));
    spyOn(this.modalService, 'modalOk').and.returnValue(this.$q.reject('Cancel'));
    this.controller.submit(email);
    this.scope.$digest();
    expect(this.$state.go).toHaveBeenCalledWith('home');
  });

});
