/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(function (){
  'use strict';

  /**
   * Validates that the input is an integer.
   */
  function str2integerDirectiveFactory() {
    var INTEGER_REGEXP = /^\-?\d+$/;

    var directive = {
      require: 'ngModel',
      link: link
    };
    return directive;

    function link(scope, element, attrs, ctrl) {
      ctrl.$parsers.unshift(function(viewValue){
        if (INTEGER_REGEXP.test(viewValue)) {
          return viewValue;
        }
        return undefined;
      });
    }

  }

  return str2integerDirectiveFactory;
});
