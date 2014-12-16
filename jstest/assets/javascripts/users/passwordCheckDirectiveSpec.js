// Jasmine test suite
//
define(['angular', 'angularMocks', 'underscore', 'biobankApp'], function(angular, mocks, _) {
  'use strict';

  ddescribe('Directive: passwordCheck', function() {

    var scope, form;

    beforeEach(mocks.module('biobankApp'));

    beforeEach(inject(function($compile, $rootScope) {
      scope = $rootScope;

      var element = angular.element(
        '<form name="form">' +
          '<input name="password" type="password" ng-model="model.password"/>' +
          '<input name="confirmPassword" type="password" ng-model="model.confirmPassword" password-check="model.password" ng-required/>' +
          '</form>');

      scope.model = {
        password: null,
        confirmPassword: null
      } ;
      $compile(element)(scope);
      form = scope.form;
    }));

    it('success when passwords match', function() {
      form.password.$setViewValue('test-password');
      form.confirmPassword.$setViewValue('test-password');
      scope.$digest();
      expect(form.confirmPassword.$valid).toBe(true);
    });

    it('failure when passwords do not match', function() {
      form.password.$setViewValue('test-password');
      form.confirmPassword.$setViewValue('abcdefghi');
      scope.$digest();
      expect(form.confirmPassword.$valid).toBe(false);
    });

  });

});
