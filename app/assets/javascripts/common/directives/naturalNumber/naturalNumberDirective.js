/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(function () {
  'use strict';

  var INTEGER_REGEXP = /^-?\d+$/;

  /**
   * Restricts input to a positive integer greater than zero.
   */
  function naturalNumberDirective() {
    var directive = {
      restrict: 'A',
      require: 'ngModel',
      link: link
    };

    return directive;

    function link(scope, element, attrs, ctrl) {
      ctrl.$parsers.unshift(function(viewValue){
        if (INTEGER_REGEXP.test(viewValue)) {
          var intValue = parseInt(viewValue, 10);
          if (intValue > 0) {
            // it is valid
            ctrl.$setValidity('naturalNumber', true);
            return viewValue;
          }
        }

        // it is invalid, return undefined (no model update)
        ctrl.$setValidity('naturalNumber', false);
        return undefined;
      });

    }
  }

  return naturalNumberDirective;
});
