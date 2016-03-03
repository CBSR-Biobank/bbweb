/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(function () {
  'use strict';

  /**
   *
   */
  function validCount() {
    var INTEGER_REGEXP = /^\-?\d+$/;
    var directive = {
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
            ctrl.$setValidity('validCount', true);
            return viewValue;
          }
        }

        // it is invalid, return undefined (no model update)
        ctrl.$setValidity('validCount', false);
        return undefined;
      });

    }
  }

  return validCount;
});
