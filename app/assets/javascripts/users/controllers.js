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
    '$scope', '$state', '$stateParams', 'stateHelper', 'userService', 'modalService', 'notifications',
    function($scope, $state, $stateParams, stateHelper, userService, modalService, notifications) {
      $scope.form = {
        notifications: notifications,
        credentials: {
          email: '',
          password: ''
        },
        login: function(credentials) {
          userService.login(credentials).then(
            function() {
              $state.go('dashboard');
            },
            function(response) {
              var modalDefaults = {};
              var modalOptions = {};

              if (response.data.message === 'invalid email or password') {
                modalOptions.closeButtonText = 'Cancel';
                modalOptions.actionButtonText = 'Retry';
                modalOptions.headerText = 'Invalid login credentials';
                modalOptions.bodyText = 'The email and / or password you entered are invalid.';
              } else if (response.data.message === 'the user is not active') {
                modalOptions.headerText = 'Login not active';
                modalOptions.bodyText = 'Your login is not active yet. ' +
                  'Please contact your system admnistrator for more information.';
                modalDefaults.templateUrl = '/assets/javascripts/common/modalOk.html';
              } else if (response.data.message === 'the user is locked') {
                modalOptions.headerText = 'Login is locked';
                modalOptions.bodyText = 'Your login is locked. ' +
                  'Please contact your system admnistrator for more information.';
                modalDefaults.templateUrl = '/assets/javascripts/common/modalOk.html';
              } else {
                modalOptions.headerText = 'Login error';
                modalOptions.bodyText = 'Cannot login: ' + response.data.message;
                modalDefaults.templateUrl = '/assets/javascripts/common/modalOk.html';
              }
              modalService.showModal(modalDefaults, modalOptions).then(
                function() {
                  stateHelper.reloadAndReinit();
                },
                function() {
                  $state.go('home');
                }
              );
            });
        }
      };
    }
  ]);

  mod.controller('ForgotPasswordCtrl', [
    '$scope', '$state', '$stateParams', '$log', 'userService', 'modalService', 'emailNotFound',
    function($scope, $state, $stateParams, $log, userService, modalService, emailNotFound) {
      $scope.form = {
        email: '',
        emailNotFound: emailNotFound,
        submit: function(email) {
          userService.passwordReset(email).then(
            function() {
              // password reset, email sent
              $state.go('users.forgot.passwordSent', { email: email });
            },
            function(response) {
              // user not found
              var status = response.status;
              if (status === 404) {
                $state.go('users.forgot.emailNotFound');
              } else {
                // user not active
                var modalDefaults = {
                  templateUrl: '/assets/javascripts/common/modalOk.html'
                };
                var modalOptions = {
                  headerText: 'Cannot reset your password',
                  bodyText: 'The account associated with that email is not active in the system. ' +
                    'Please contact your system administrator for more information.'
                };
                modalService.showModal(modalDefaults, modalOptions).then(
                  function () {
                    $state.go('home');
                  }, function () {
                    $state.go('home');
                  });
              }
            });
        }
      };
    }
  ]);

  mod.controller('ResetPasswordCtrl', [
    '$scope', '$state', '$stateParams',
    function($scope, $state, $stateParams) {
      $scope.page = {
        email: $stateParams.email,
        login: function() {
          $state.go('users.login');
        }
      };
    }
  ]);

  mod.controller('RegisterUserCtrl', [
    '$scope', '$state', '$stateParams', 'userService', 'notifications',
    function($scope, $state, $stateParams, userService, notifications) {
      $scope.form = {
        notifications: notifications,
        user: {
          name: '',
          email: '',
          password: '',
          confirmPassword: '',
          avatarUrl: ''
        },
        submit: function(user) {
          userService.add(user).then(
            function() {
              // user has been registerd
              $state.go('users.login.registered');
            },
            function() {
              // registration failed
              $state.go('users.register.failed');
            }
          );
        },
        cancel: function() {
          $state.go('home');
        }
      };
    }
  ]);

});
