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
    '$scope', '$location', '$log', 'userService', 'modalService',
    function($scope, $location, $log, userService, modalService) {
      $scope.form = {
        credentials: {
          email: "",
          password: ""
        },
        login: function(credentials) {
          userService.loginUser(credentials).then(
            function(user) {
              $location.path('/dashboard');
            },
            function() {
              var modalOptions = {
                closeButtonText: 'Cancel',
                actionButtonText: 'Retry',
                headerText: 'Invalid logon credentials',
                bodyText: 'The email and / or password you entered were invalid'
              };
              modalService.showModal({}, modalOptions).then(function (result) {
                $location.path('/login');
              }, function () {
                $location.path('/');
              });
            });
        },
        forgotPassword: function() {
          alert("send forgot password email");
        },
        register: function() {
          alert("register user");
        }
      };
    }]);

});
