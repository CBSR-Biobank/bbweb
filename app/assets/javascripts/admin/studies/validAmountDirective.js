/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(['angular'], function(angular) {
  'use strict';

  /**
   * A directive to be used in form input that only allows positive integer or float values.
   */
  function validAmount() {
    var FLOAT_REGEXP = /^\-?\d+((\.|\,)\d+)?$/;
    var directive = {
      restrict: 'A',
      require: 'ngModel',
      link: link
    };
    return directive;

    function link(scope, elm, attrs, ctrl) {
      ctrl.$parsers.unshift(function (viewValue) {
        if (FLOAT_REGEXP.test(viewValue)) {
          var floatValue = parseFloat(viewValue);
          if (floatValue > 0) {
            ctrl.$setValidity('validAmount', true);
            return parseFloat(viewValue.replace(',', '.'));
          }
        }

        ctrl.$setValidity('validAmount', false);
        return undefined;
      });
    }
  }

  return validAmount;
});
