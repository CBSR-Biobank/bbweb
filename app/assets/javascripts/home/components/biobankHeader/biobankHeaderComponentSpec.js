/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
/* global angular */

import _ from 'lodash';

describe('Component: biobankHeader', function() {

  beforeEach(() => {
    angular.mock.module('biobankApp', 'biobank.test');
    angular.mock.inject(function (ComponentTestSuiteMixin) {
      _.extend(this, ComponentTestSuiteMixin);
      this.injectDependencies('$q',
                              '$rootScope',
                              '$compile',
                              '$state',
                              'User',
                              'usersService',
                              'factory');
      this.createController = () =>
        ComponentTestSuiteMixin.createController.call(
          this,
          '<biobank-header></biobank-header>',
          undefined,
          'biobankHeader');
    });
  });

  it('should have valid scope', function() {
    this.createController();
    this.scope.$digest();
    expect(this.controller.logout).toBeFunction();
  });

  it('update user on login', function() {
    var jsonUser = this.factory.user();

    this.createController();
    expect(this.controller.user).toBeUndefined();

    spyOn(this.usersService, 'getCurrentUser').and.returnValue(jsonUser);
    this.scope.$digest();
    expect(this.controller.user).toEqual(jsonUser);
  });

  it('changes to correct state on logout', function() {
    spyOn(this.usersService, 'logout').and.returnValue(this.$q.when(true));
    spyOn(this.$state, 'go').and.returnValue(true);

    this.createController();
    this.controller.logout();
    this.scope.$digest();

    expect(this.$state.go).toHaveBeenCalledWith('home', {}, { reload: true});
  });

  it('changes to correct state on logout failure', function() {
    spyOn(this.$state, 'go').and.returnValue(true);

    this.createController();
    spyOn(this.usersService, 'logout').and.returnValue(this.$q.reject('simulated logout failure'));
    this.controller.logout();
    this.scope.$digest();

    expect(this.$state.go).toHaveBeenCalledWith('home', {}, { reload: true});
  });


});
