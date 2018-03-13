/*
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

import { IntegerDirective } from '../integer/integerDirective';

function naturalNumberDirectiveFactory() {

  /**
   * Restricts input to a positive integer greater than zero.
   *
   * @memberOf common.directives
   */
  class NaturalNumberDirective extends IntegerDirective {

    constructor() {
      super();
      this.restrict = 'A';
    }

    /**
     * @protected
     */
    parse(viewValue) {
      const value = super.parse(viewValue)
      if (value === undefined) {
        return value;
      }
      return (parseInt(value, 10) > 0) ? viewValue : undefined;
    }

    link(scope, element, attrs, ctrl) {
      ctrl.$parsers.unshift((viewValue) => {
        const value = this.parse(viewValue);
        ctrl.$setValidity('integer', !isNaN(value));
        return value;
      });
    }
  }

  return new NaturalNumberDirective();
}

export default ngModule => ngModule.directive('naturalNumber', naturalNumberDirectiveFactory)
