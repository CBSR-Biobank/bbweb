/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */

/**
 * Restricts input to a postiive floating point number.
 *
 * @return {object} An AngularJS directive.
 */
function positiveFloatDirective() {
  const FLOAT_REGEXP = /^\-?\d+((\.|\,)\d+)?$/,
        directive = {
          restrict: 'A',
          require:  'ngModel',
          link:     link
        };

  return directive;

  function link(scope, elm, attrs, ctrl) {
    ctrl.$parsers.unshift(function (viewValue) {
      if (FLOAT_REGEXP.test(viewValue)) {
        var floatValue = parseFloat(viewValue);
        if (floatValue > 0) {
          ctrl.$setValidity('positiveFloat', true);
          return parseFloat(viewValue.replace(',', '.'));
        }
      }

      ctrl.$setValidity('positiveFloat', false);
      return undefined;
    });
  }
}

export default ngModule => ngModule.directive('positiveFloat', positiveFloatDirective)
