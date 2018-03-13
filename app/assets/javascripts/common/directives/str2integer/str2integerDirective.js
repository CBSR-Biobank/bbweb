/*
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

function str2integerDirectiveFactory() {

  /**
   * Validates that the input is an integer.
   *
   * @memberOf common.directives
   */
  class Str2integerDirective {

    constructor() {
      this.require = 'ngModel';

      /** @readonly */
      this.INTEGER_REGEXP = /^\-?\d+$/;
    }


    link(scope, element, attrs, ctrl) {
      ctrl.$parsers.unshift((viewValue) => {
        const value = this.INTEGER_REGEXP.test(viewValue) ? viewValue : undefined;
        ctrl.$setValidity('str2integer', value !== undefined);
        return value;
      });
    }
  }

  return new Str2integerDirective();
}

export default ngModule => ngModule.directive('str2integer', str2integerDirectiveFactory)
