/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */
/* global angular */

import { DirectiveTestSuiteMixin } from 'test/mixins/DirectiveTestSuiteMixin';
import ngModule from '../../index'

describe('Directive: smartFloat', function() {

  beforeEach(() => {
    angular.mock.module(ngModule, 'biobank.test');
    angular.mock.inject(function () {
      Object.assign(this, DirectiveTestSuiteMixin);

      this.injectDependencies();
      DirectiveTestSuiteMixin.createController.call(
        this,
        `<form name="testForm">
            <input type="number"
                   name="theNumber"
                   ng-model="vm.theNumber"
                   smart-float
                   required />
         </form>`,
        { theNumber: null  });
    });
  });

  it('should allow a positive integer', function() {
    var anInteger = 1;
    this.scope.testForm.theNumber.$setViewValue(anInteger.toString());
    expect(this.controller.theNumber).toEqual(anInteger);
    expect(this.scope.testForm.theNumber.$valid).toBe(true);
  });

  it('should allow a negative integer', function() {
    var anInteger = -1;
    this.scope.testForm.theNumber.$setViewValue(anInteger.toString());
    expect(this.controller.theNumber).toEqual(anInteger);
    expect(this.scope.testForm.theNumber.$valid).toBe(true);
  });

  it('should allow a positive floating point', function() {
    var aFloat = 1.10;
    this.scope.testForm.theNumber.$setViewValue(aFloat.toString());
    expect(this.controller.theNumber).toEqual(aFloat);
    expect(this.scope.testForm.theNumber.$valid).toBe(true);
  });

  it('should allow a negative floating point', function() {
    var aFloat = -1.10;
    this.scope.testForm.theNumber.$setViewValue(aFloat.toString());
    expect(this.controller.theNumber).toEqual(aFloat);
    expect(this.scope.testForm.theNumber.$valid).toBe(true);
  });

  it('should not allow an alphanumeric value', function() {
    var aString = 'x1.10';
    this.scope.testForm.theNumber.$setViewValue(aString);
    expect(this.controller.theNumber).toBeNull();
    expect(this.scope.testForm.theNumber.$valid).toBe(false);
  });

});
