/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
/* global angular */

import _ from 'lodash';

describe('Directive: validAmount', function() {

  beforeEach(() => {
    angular.mock.module('biobankApp', 'biobank.test');
    angular.mock.inject(function (DirectiveTestSuiteMixin) {
      _.extend(this, DirectiveTestSuiteMixin);
      DirectiveTestSuiteMixin.createController.call(
        this,
        `<form name="testForm">
             <input type="number"
                    name="theNumber"
                    ng-model="vm.theNumber"
                    positive-float
                    required></input>
         </form>`,
        { theNumber: null });
    });
  });

  it('should allow a positive integer', function() {
    var anInteger = 1;
    this.scope.testForm.theNumber.$setViewValue(anInteger.toString());
    expect(this.controller.theNumber).toEqual(anInteger);
    expect(this.scope.testForm.theNumber.$valid).toBe(true);
  });

  it('should not allow a negative integer', function() {
    var anInteger = -1;
    this.scope.testForm.theNumber.$setViewValue(anInteger.toString());
    expect(this.controller.theNumber).toBeUndefined();
    expect(this.scope.testForm.theNumber.$valid).toBe(false);
  });

  it('should allow a positive floating point', function() {
    var aFloat = 1.10;
    this.scope.testForm.theNumber.$setViewValue(aFloat.toString());
    expect(this.controller.theNumber).toEqual(aFloat);
    expect(this.scope.testForm.theNumber.$valid).toBe(true);
  });

  it('should not allow a negative floating point', function() {
    var aFloat = -1.10;
    this.scope.testForm.theNumber.$setViewValue(aFloat.toString());
    expect(this.controller.theNumber).toBeUndefined();
    expect(this.scope.testForm.theNumber.$valid).toBe(false);
  });

  it('should not allow an alphanumeric value', function() {
    var aString = 'x1.10';
    this.scope.testForm.theNumber.$setViewValue(aString);
    expect(this.controller.theNumber).toBeUndefined();
    expect(this.scope.testForm.theNumber.$valid).toBe(false);
  });

});
