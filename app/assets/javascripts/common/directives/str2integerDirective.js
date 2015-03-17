define([], function(){
  'use strict';

  /**
   * Validates that the input is an integer.
   */
  function str2integerDirectiveFactory() {
    var directive = {
      require: 'ngModel',
      link: link
    };
    return directive;

    function link(scope, element, attrs, ctrl) {
      ctrl.$parsers.unshift(function(viewValue){
        return parseInt(viewValue);
      });
    }

  }

  return str2integerDirectiveFactory;
});
