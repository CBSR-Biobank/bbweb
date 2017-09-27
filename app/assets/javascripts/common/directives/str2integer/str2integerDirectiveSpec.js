/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
/* global angular */

describe('Directive: str2integer', function() {

  beforeEach(() => {
    angular.mock.module('biobankApp', 'biobank.test');
    angular.mock.inject(function ($rootScope, $compile) {
      this.element = angular.element(
        `<form name="testForm">
          <input type="number"
                 name="theNumber"
                 ng-model="vm.theNumber"
                 str2integer
                 required />
        </form>`);

      this.scope = $rootScope.$new();
      this.scope.vm = { theNumber: null };
      $compile(this.element)(this.scope);
      this.scope.$digest();
    });
  });

  it('should allow a positive integer', function() {
    var anInteger = 1;
    this.scope.testForm.theNumber.$setViewValue(anInteger.toString());
    expect(this.scope.vm.theNumber).toEqual(anInteger);
    expect(this.scope.testForm.theNumber.$valid).toBe(true);
  });

  it('should allow a negative integer', function() {
    var anInteger = -1;
    this.scope.testForm.theNumber.$setViewValue(anInteger.toString());
    expect(this.scope.vm.theNumber).toEqual(anInteger);
    expect(this.scope.testForm.theNumber.$valid).toBe(true);
  });

  it('should not allow a positive floating point', function() {
    var aFloat = 1.10;
    this.scope.testForm.theNumber.$setViewValue(aFloat.toString());
    expect(this.scope.vm.theNumber).toBeUndefined();
    expect(this.scope.testForm.theNumber.$valid).toBe(false);
  });

  it('should not allow a negative floating point', function() {
    var aFloat = -1.10;
    this.scope.testForm.theNumber.$setViewValue(aFloat.toString());
    expect(this.scope.vm.theNumber).toBeUndefined();
    expect(this.scope.testForm.theNumber.$valid).toBe(false);
  });

  it('should not allow an alphanumeric value', function() {
    var aString = 'x110';
    this.scope.testForm.theNumber.$setViewValue(aString);
    expect(this.scope.vm.theNumber).toBeUndefined();
    expect(this.scope.testForm.theNumber.$valid).toBe(false);
  });

});
