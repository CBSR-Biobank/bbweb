define([], function(){
  'use strict';

 /**
   *
   */
  function integerDirectiveFactory() {
    var INTEGER_REGEXP = /^\-?\d+$/;

    var directive = {
      require: 'ngModel',
      link: link
    };
    return directive;

    function link(scope, element, attrs, ctrl) {
      ctrl.$parsers.unshift(function(viewValue){
        if (INTEGER_REGEXP.test(viewValue)) {
          // it is valid
          ctrl.$setValidity('integer', true);
          return viewValue;
        }

        // it is invalid, return undefined (no model update)
        ctrl.$setValidity('integer', false);
        return undefined;
      });

    }
  }

  return integerDirectiveFactory;
});
