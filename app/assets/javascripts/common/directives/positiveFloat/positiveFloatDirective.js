/*
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

import { IntegerDirective } from '../integer/integerDirective';

function positiveFloatDirectiveFactory() {

  /**
   * Restricts input to a positive floating point number.
   *
   * @memberOf common.directives
   */
  class PositiveFloatDirective extends IntegerDirective {

    constructor() {
      super();
      this.restrict = 'A';

      /** @readonly */
      this.FLOAT_REGEXP = /^\-?\d+((\.|\,)\d+)?$/;
    }

    link(scope, elm, attrs, ctrl) {
      ctrl.$parsers.unshift((viewValue) => {
        let floatValue = NaN;

        if (this.FLOAT_REGEXP.test(viewValue)) {
          floatValue = parseFloat(viewValue);
          if (!isNaN(floatValue)) {
            floatValue = (floatValue > 0) ? parseFloat(viewValue.replace(',', '.')) : NaN;
          }
        }

        ctrl.$setValidity('positiveFloat', !isNaN(floatValue));
        return floatValue;
      });
    }
  }

  return new PositiveFloatDirective();
}

export default ngModule => ngModule.directive('positiveFloat', positiveFloatDirectiveFactory)
