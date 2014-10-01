define(['../module'], function(module) {
  'use strict';

  module.directive('str2integer', str2integer);

  /**
   * Validates that the input is an integer.
   */
  function str2integer() {
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

});
