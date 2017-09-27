/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2017 Canadian BioSample Repository (CBSR)
 */
/* global angular */

import _ from 'lodash';

describe('Directive: passwordCheck', function() {

  beforeEach(() => {
    angular.mock.module('biobankApp', 'biobank.test');
    angular.mock.inject(
      function($compile, $rootScope) {
        this.element = angular.element(
          `<form name="form">
            <input name="password" type="password" ng-model="model.password"/>
            <input name="confirmPassword"
                   type="password"
                   ng-model="model.confirmPassword"
                   password-check="model.password"
                   ng-required/>
          </form>`
        );

        this.scope = $rootScope.$new();
        this.scope.model = {
          password: null,
          confirmPassword: null
        } ;
        $compile(this.element)(this.scope);
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
