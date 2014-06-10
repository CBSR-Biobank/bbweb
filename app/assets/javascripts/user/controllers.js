/**
 * User controllers.
 *
 * Allow for autofill / autocomplete. See the following web page for an explanation:
 *
 * http://timothy.userapp.io/post/63412334209/form-autocomplete-and-remember-password-with-angularjs
 */
define(['angular'], function(angular) {
  'use strict';

  var LoginCtrl = function($scope, $location, userService) {
    $scope.login = function() {
      var credentials = {
        email: $("#email").val(),
        password: $("#password").val()
      };

      userService.loginUser(credentials).then(function(/*user*/) {
        $location.path('/dashboard');
      });
    };
  };
  LoginCtrl.$inject = ['$scope', '$location', 'userService'];

  return {
    LoginCtrl: LoginCtrl
  };

});
