/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */
/* global angular */

import _ from 'lodash';
import ngModule from '../../index'

describe('Component: home', function() {

  beforeEach(() => {
    angular.mock.module(ngModule, 'biobank.test');
    angular.mock.inject(function (ComponentTestSuiteMixin) {
      _.extend(this, ComponentTestSuiteMixin);

      this.injectDependencies('$q',
                              '$rootScope',
                              '$compile',
                              '$httpBackend',
                              'User',
                              'userService',
                              'Factory');
      this.createController = () =>
        ComponentTestSuiteMixin.createController.call(
          this,
          '<home></home>',
          undefined,
          'home');
    });
  });

  it('has valid scope', function() {
    var user = this.User.create(this.Factory.user());
    this.userService.requestCurrentUser = jasmine.createSpy().and.returnValue(this.$q.when(user));
    this.createController();
    expect(this.controller.user).toEqual(jasmine.any(this.User));
    expect(this.$rootScope.pageTitle).toBeDefined('Biobank');
  });
});
