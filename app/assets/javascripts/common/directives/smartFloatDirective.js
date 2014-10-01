define(['../module'], function(module) {
  'use strict';

  var FLOAT_REGEXP = /^\-?\d+((\.|\,)\d+)?$/;

  module.directive('smartFloat', smartFloat);

  /**
   * Validates that value is a float.
   */
  function smartFloat() {
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
        } else {
          ctrl.$setValidity('float', false);
          return undefined;
        }
      });
    }
  }

});
