/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(['angular', 'angularMocks', 'lodash', 'biobankApp'], function(angular, mocks, _) {
  'use strict';

  describe('Directive: integer', function() {

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function ($rootScope, $compile) {
      this.scope = $rootScope.$new();

      this.element = angular.element(
        '<form name="testForm">' +
          '  <input type="number"' +
          '         name="theNumber"' +
          '         ng-model="theNumber"' +
          '         integer' +
          '         required />' +
          '</form>');

      this.scope.theNumber = null;
      $compile(this.element)(this.scope);
      this.scope.$digest();
    }));

    it('should allow a positive integer', function() {
      var anInteger = 1;
      this.scope.testForm.theNumber.$setViewValue(anInteger.toString());
      expect(this.scope.theNumber).toEqual(anInteger);
      expect(this.scope.testForm.theNumber.$valid).toBe(true);
    });

    it('should allow a negative integer', function() {
      var anInteger = -1;
      this.scope.testForm.theNumber.$setViewValue(anInteger.toString());
      expect(this.scope.theNumber).toEqual(anInteger);
      expect(this.scope.testForm.theNumber.$valid).toBe(true);
    });

    it('should not allow a positive floating point', function() {
      var aFloat = 1.10;
      this.scope.testForm.theNumber.$setViewValue(aFloat.toString());
      expect(this.scope.theNumber).toBeUndefined();
      expect(this.scope.testForm.theNumber.$valid).toBe(false);
    });

    it('should not allow a negative floating point', function() {
      var aFloat = -1.10;
      this.scope.testForm.theNumber.$setViewValue(aFloat.toString());
      expect(this.scope.theNumber).toBeUndefined();
      expect(this.scope.testForm.theNumber.$valid).toBe(false);
    });

    it('should not allow an alphanumeric value', function() {
      var aString = 'x110';
      this.scope.testForm.theNumber.$setViewValue(aString);
      expect(this.scope.theNumber).toBeUndefined();
      expect(this.scope.testForm.theNumber.$valid).toBe(false);
    });

  });

});
