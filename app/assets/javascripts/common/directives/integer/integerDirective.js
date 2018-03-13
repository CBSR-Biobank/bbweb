/*
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

/**
 * Restricts input to a an integer value.
 *
 * @memberOf common.directives
 */
class IntegerDirective {

  constructor() {
    this.require = 'ngModel';

    /** @readonly */
    this.INTEGER_REGEXP = /^\-?\d+$/;
  }

  /**
   * @protected
   */
  parse(viewValue) {
    return this.INTEGER_REGEXP.test(viewValue) ? viewValue : undefined;
  }

  link(scope, element, attrs, ctrl) {
    ctrl.$parsers.unshift((viewValue) => {
      const value = this.parse(viewValue);
      ctrl.$setValidity('integer', value !== undefined);
      return value;
    });
  }
}

function integerDirectiveFactory() {
  return new IntegerDirective();
}

export { IntegerDirective }
export default ngModule => ngModule.directive('integer', integerDirectiveFactory)
