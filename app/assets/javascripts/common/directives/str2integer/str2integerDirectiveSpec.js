/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */
/* global angular */

import { DirectiveTestSuiteMixin } from 'test/mixins/DirectiveTestSuiteMixin';
import ngModule from '../../../app'  // the whole appliction has to be loaded for these tests

describe('Directive: str2integer', function() {

  beforeEach(() => {
    angular.mock.module(ngModule, 'biobank.test');
    angular.mock.inject(function () {
      Object.assign(this, DirectiveTestSuiteMixin);
      this.injectDependencies();
      this.createControllerInternal(
        `<form name="testForm">
          <input type="number"
                 name="theNumber"
                 ng-model="vm.theNumber"
                 str2integer
                 required />
         </form>`,
        { theNumber: null  });
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
