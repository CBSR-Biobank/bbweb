define(['./module'], function(module) {
  'use strict';

  module.controller('LoginCtrl', LoginCtrl);

  LoginCtrl.$inject = [
    '$state', 'stateHelper', 'userService', 'modalService',
  ];

  /**
   * Used for user log in.
   *
   * Allow for autofill / autocomplete. See the following web page for an explanation:
   *
   * http://timothy.userapp.io/post/63412334209/form-autocomplete-and-remember-password-with-angularjs
   */
  function LoginCtrl($state, stateHelper, userService, modalService) {
    var vm = this;
    if (userService.getUser()) {
      // user already logged in, send him to home page
      $state.go('home');
    }
    vm.credentials = {
      email: '',
      password: ''
    };
    vm.login = login;

    //--

    function loginSuccess() {
      $state.go('dashboard');
    }

    function loginFailure(error) {
      var modalDefaults = {};
      var modalOptions = {};

      if (error.data.message === 'invalid email or password') {
        modalOptions.closeButtonText = 'Cancel';
        modalOptions.actionButtonText = 'Retry';
        modalOptions.headerText = 'Invalid login credentials';
        modalOptions.bodyText = 'The email and / or password you entered are invalid.';
      } else if (error.data.message === 'the user is not active') {
        modalOptions.headerText = 'Login not active';
        modalOptions.bodyText = 'Your login is not active yet. ' +
          'Please contact your system admnistrator for more information.';
        modalDefaults.templateUrl = '/assets/javascripts/common/modalOk.html';
      } else if (error.data.message === 'the user is locked') {
        modalOptions.headerText = 'Login is locked';
        modalOptions.bodyText = 'Your login is locked. ' +
          'Please contact your system admnistrator for more information.';
        modalDefaults.templateUrl = '/assets/javascripts/common/modalOk.html';
      } else {
        modalOptions.headerText = 'Login error';
        modalOptions.bodyText = 'Cannot login: ' + error.data.message;
        modalDefaults.templateUrl = '/assets/javascripts/common/modalOk.html';
      }

      modalService.showModal(modalDefaults, modalOptions)
        .then(stateHelper.reloadAndReinit())
        .catch(function() {
          $state.go('home');
        });
    }

    function login(credentials) {
      userService.login(credentials).then(loginSuccess).catch(loginFailure);
    }
  }

});
