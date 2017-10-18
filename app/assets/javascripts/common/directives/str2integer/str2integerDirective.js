/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */

/**
 * Validates that the input is an integer.
 */
function str2integerDirective() {
  const INTEGER_REGEXP = /^\-?\d+$/;

  var directive = {
    require: 'ngModel',
    link: link
  };
  return directive;

  function link(scope, element, attrs, ctrl) {
    ctrl.$parsers.unshift(function(viewValue){
      if (INTEGER_REGEXP.test(viewValue)) {
        ctrl.$setValidity('str2integer', true);
        return viewValue;
      }
      ctrl.$setValidity('str2integer', false);
      return undefined;
    });
  }

}

export default ngModule => ngModule.directive('str2integer', str2integerDirective)
