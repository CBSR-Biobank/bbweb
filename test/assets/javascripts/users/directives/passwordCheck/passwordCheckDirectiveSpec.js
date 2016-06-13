/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
// Jasmine test suite
//
define(['angular', 'angularMocks', 'biobankApp'], function(angular, mocks) {
  'use strict';

  describe('Directive: passwordCheck', function() {

    beforeEach(mocks.module('biobankApp'));

    beforeEach(inject(function($compile, $rootScope) {
      this.element = angular.element([
        '<form name="form">',
        '  <input name="password" type="password" ng-model="model.password"/>',
        '  <input name="confirmPassword" ',
        '         type="password" ',
        '         ng-model="model.confirmPassword" ',
        '         password-check="model.password" ',
        '         ng-required/>',
        '</form>'
      ].join(''));

      this.scope = $rootScope.$new();
      this.scope.model = {
        password: null,
        confirmPassword: null
      } ;
      $compile(this.element)(this.scope);
    }));

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

});
