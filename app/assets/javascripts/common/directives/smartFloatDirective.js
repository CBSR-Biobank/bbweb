define([], function(){
  'use strict';

  var FLOAT_REGEXP = /^\-?\d+((\.|\,)\d+)?$/;

  /**
   * Validates that value is a float.
   */
  function smartFloatDirectiveFactory() {
    var directive = {
      require: 'ngModel',
      link: link
    };
    return directive;

    function link(scope, element, attrs, ctrl) {
      ctrl.$parsers.unshift(function(viewValue) {
        if (FLOAT_REGEXP.test(viewValue)) {
          ctrl.$setValidity('float', true);
          return parseFloat(viewValue.replace(',', '.'));
        }

        ctrl.$setValidity('float', false);
        return undefined;
      });
    }
  }

  return smartFloatDirectiveFactory;
});
