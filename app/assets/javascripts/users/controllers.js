/**
 * User controllers.
 *
 * Allow for autofill / autocomplete. See the following web page for an explanation:
 *
 * http://timothy.userapp.io/post/63412334209/form-autocomplete-and-remember-password-with-angularjs
 */
define(['angular'], function(angular) {
  'use strict';

  var mod = angular.module('users.controllers', []);

  mod.controller('LoginCtrl', [
    '$scope', '$state', '$stateParams', '$location', '$log', 'userService', 'modalService',
    function($scope, $state, $stateParams, $location, $log, userService, modalService) {
      $scope.form = {
        credentials: {
          email: "",
          password: ""
        },
        login: function(credentials) {
          userService.login(credentials).then(
            function(user) {
              $location.path('/dashboard');
            },
            function() {
              // login failed
              var modalOptions = {
                closeButtonText: 'Cancel',
                actionButtonText: 'Retry',
                headerText: 'Invalid logon credentials',
                bodyText: 'The email and / or password you entered were invalid'
              };
              modalService.showModal({}, modalOptions).then(function (result) {
                $state.go($state.current, $stateParams, {
                  reload: true,
                  inherit: false,
                  notify: true
                });
              }, function () {
                $location.path('/');
              });
            });
        },
        forgotPassword: function() {
          $state.go("users.forgot");
        },
        register: function() {
          alert("register user");
        }
      };
    }]);

  mod.controller('ForgotPasswordCtrl', [
    '$scope', '$state', '$stateParams', '$log', 'userService', 'emailNotFound',
    function($scope, $state, $stateParams, $log, userService, emailNotFound) {
      $scope.form = {
        email: "",
        emailNotFound: emailNotFound,
        submit: function(email) {
          userService.passwordReset(email).then(
            function() {
              // password reset, email sent
              $state.go("users.forgot.passwordSent", { email: email });
            },
            function() {
              // user not found
              $state.go("users.forgot.emailNotFound");
            });
        }
      };
    }]);

  mod.controller('ResetPasswordCtrl', [
    '$scope', '$state', '$stateParams', '$log', 'userService',
    function($scope, $state, $stateParams, $log, userService) {
      $scope.page = {
        email: $stateParams.email,
        login: function() {
          $state.go("users.login");
        }
      };
    }]);

});
