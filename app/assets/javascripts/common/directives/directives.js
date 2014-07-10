/**
 * A common directive.
 * It would also be ok to put all directives into one file, or to define one RequireJS module
 * that references them all.
 */
define(['angular'], function(angular) {
  'use strict';

  var mod = angular.module('common.directives.directives', []);

  var INTEGER_REGEXP = /^\-?\d+$/;
  mod.directive('integer', function(){
    return {
      require: 'ngModel',
      link: function(scope, ele, attr, ctrl){
        ctrl.$parsers.unshift(function(viewValue){
          if (INTEGER_REGEXP.test(viewValue)) {
            // it is valid
            ctrl.$setValidity('integer', true);
            return viewValue;
          } else {
            // it is invalid, return undefined (no model update)
            ctrl.$setValidity('integer', false);
            return undefined;
          }
        });
      }
    };
  });

  var FLOAT_REGEXP = /^\-?\d+((\.|\,)\d+)?$/;
  mod.directive('smartFloat', function() {
    return {
      require: 'ngModel',
      link: function(scope, elm, attrs, ctrl) {
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
    };
  });

  mod.directive('str2integer', function(){
    return {
      require: 'ngModel',
      link: function(scope, ele, attr, ctrl){
        ctrl.$parsers.unshift(function(viewValue){
          return parseInt(viewValue);
        });
      }
    };
  });

  /**
   * Fixes form auto fill. See the following web page for an explanation:
   *
   * http://timothy.userapp.io/post/63412334209/form-autocomplete-and-remember-password-with-angularjs
   */
  mod.directive("ngLoginSubmit", function(){
    return {
      restrict: "A",
      scope: {
        onSubmit: "=ngLoginSubmit"
      },
      link: function(scope, element, attrs) {
        $(element)[0].onsubmit = function() {
          $("#login-login").val($("#login", element).val());
          $("#login-password").val($("#password", element).val());

          scope.onSubmit(function() {
            $("#login-form")[0].submit();
          });
          return false;
        };
      }
    };
  });

  /**
   * Displays a right justified button with a 'plus' icon. Meant to be used in a pane to add a
   * domain object.
   */
  mod.directive("addButton", function () {
    return {
      restrict: "E",
      replace: 'true',
      scope: {
        'add': '&onAdd'
      },
      template: '<div class="btn-group pull-right">' +
        '  <button class="btn btn-xs btn-primary"' +
        '          ng-click="add()"' +
        '          title="Add annotation type">' +
        '  <i class="glyphicon glyphicon-plus"></i></button>' +
        '</div>'
    };
  });

  /**
   * Displays a right justified button with a 'plus' icon. Meant to be used in table displaying
   * domain object information.
   */
  mod.directive("infoUpdateRemoveButtons", function () {
    return {
      restrict: "E",
      replace: 'true',
      scope: {
        'info': '&onInfo',
        'update': '&onUpdate',
        'remove': '&onRemove'
      },
      templateUrl: '/assets/javascripts/common/directives/infoUpdateRemoveButtons.html'
    };
  });

  return mod;
});
