/**
 * Tabs used when displaying a study.
 *
 * FIXME: See https://docs.angularjs.org/guide/directive, section "Creating Directives that Communicate" to
 * improve this code.
 */
define(['angular'], function(angular) {
  'use strict';

  var mod = angular.module('admin.studies.directives.studyForms', ['studies.services']);

  var INTEGER_REGEXP = /^\-?\d+$/;
  mod.directive('validCount', function(){
    return {
      require: 'ngModel',
      link: function(scope, ele, attr, ctrl) {
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
    };
  });

  var FLOAT_REGEXP = /^\-?\d+((\.|\,)\d+)?$/;
  mod.directive('validAmount', function() {
    return {
      require: 'ngModel',
      link: function(scope, elm, attrs, ctrl) {
        ctrl.$parsers.unshift(function(viewValue) {
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
    };
  });

  return mod;
});
