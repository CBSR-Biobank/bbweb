/**
 * User controllers.
 *
 * Allow for autofill / autocomplete. See the following web page for an explanation:
 *
 * http://timothy.userapp.io/post/63412334209/form-autocomplete-and-remember-password-with-angularjs
 */
define(['angular'], function(angular) {
  'use strict';

  var LoginCtrl = function($scope, $location, $log, userService, modalService) {
    $scope.login = function() {
      var credentials = {
        email: $("#email").val(),
        password: $("#password").val()
      };

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
    };
  };
  LoginCtrl.$inject = ['$scope', '$location', '$log', 'userService', 'modalService'];

  return {
    LoginCtrl: LoginCtrl
  };

});
