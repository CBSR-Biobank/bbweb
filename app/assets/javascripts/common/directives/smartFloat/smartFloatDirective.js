/*
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

function smartFloatDirectiveFactory() {

  /**
   * Validates that a value is a float.
   *
   * @memberOf common.directives
   */
  class SmartFloatDirective {

    constructor() {
      this.require = 'ngModel';

      /** @readonly */
      this.FLOAT_REGEXP = /^\-?\d+((\.|\,)\d+)?$/;
    }

    link(scope, element, attrs, ctrl) {
      ctrl.$parsers.unshift((viewValue) => {
        const value = this.FLOAT_REGEXP.test(viewValue) ? parseFloat(viewValue.replace(',', '.')) : NaN;
        ctrl.$setValidity('float', !isNaN(value));
        return value;
      });
    }
  }

  return new SmartFloatDirective();
}

export default ngModule => ngModule.directive('smartFloat', smartFloatDirectiveFactory)
