/**
 * Fixes form auto fill. See the following web page for an explanation:
 *
 * http://timothy.userapp.io/post/63412334209/form-autocomplete-and-remember-password-with-angularjs
 */
define(['angular'], function(angular) {
  'use strict';

  var mod = angular.module('common.directives.ngLoginSubmit', []);
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
  return mod;
});
