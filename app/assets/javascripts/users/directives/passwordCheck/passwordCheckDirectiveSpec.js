/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2017 Canadian BioSample Repository (CBSR)
 */
/* global angular */

import _ from 'lodash';
import ngModule from '../../index'

describe('Directive: passwordCheck', function() {

  beforeEach(() => {
    angular.mock.module(ngModule, 'biobank.test');
    angular.mock.inject(function (DirectiveTestSuiteMixin) {
      _.extend(this, DirectiveTestSuiteMixin);

      DirectiveTestSuiteMixin.createController.call(
        this,
        `<form name="form">
            <input name="password" type="password" ng-model="vm.password"/>
            <input name="confirmPassword"
                   type="password"
                   ng-model="vm.confirmPassword"
                   password-check="vm.password"
                   ng-required></input>
         </form>`,
        {
          password: null,
          confirmPassword: null
        });
    });
  });

  it('success when passwords match', function() {
    this.scope.form.password.$setViewValue('test-password');
    this.scope.form.confirmPassword.$setViewValue('test-password');
    this.scope.$digest();
    expect(this.scope.form.confirmPassword.$valid).toBe(true);
  });

  it('failure when passwords do not match', function() {
    this.scope.form.password.$setViewValue('test-password');
    this.scope.form.confirmPassword.$setViewValue('abcdefghi');
    this.scope.$digest();
    expect(this.scope.form.confirmPassword.$valid).toBe(false);
  });

});
