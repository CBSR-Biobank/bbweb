/**
 * User controllers.
 */
define(["angular"], function(angular) {
  "use strict";

  var LoginCtrl = function($scope, $location, userService) {
    $scope.credentials = {};

    $scope.login = function(credentials) {
      userService.loginUser(credentials).then(function(/*user*/) {
        $location.path("/dashboard");
      });
    };
  };
  LoginCtrl.$inject = ["$scope", "$location", "userService"];

  return {
    LoginCtrl: LoginCtrl
  };

});
