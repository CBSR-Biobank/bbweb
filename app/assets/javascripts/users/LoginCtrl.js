define(['./module'], function(module) {
  'use strict';

  module.controller('LoginCtrl', LoginCtrl);

  LoginCtrl.$inject = [
    '$state', 'stateHelper', 'usersService', 'modalService',
  ];

  /**
   * Used for user log in.
   *
   * Allow for autofill / autocomplete. See the following web page for an explanation:
   *
   * http://timothy.userapp.io/post/63412334209/form-autocomplete-and-remember-password-with-angularjs
   */
  function LoginCtrl($state, stateHelper, usersService, modalService) {
    var vm = this;
    if (usersService.getUser()) {
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
      $state.go('home');
    }

    function loginFailure(error) {
      var modalDefaults = {};
      var modalOptions = {};

      if (error.data.message === 'invalid email or password') {
        modalOptions.closeButtonText = 'Cancel';
        modalOptions.actionButtonText = 'Retry';
        modalOptions.headerHtml = 'Invalid login credentials';
        modalOptions.bodyHtml = 'The email and / or password you entered are invalid.';
      } else if (error.data.message === 'the user is not active') {
        modalOptions.headerHtml = 'Login not active';
        modalOptions.bodyHtml = 'Your login is not active yet. ' +
          'Please contact your system admnistrator for more information.';
        modalDefaults.templateUrl = '/assets/javascripts/common/modalOk.html';
      } else if (error.data.message === 'the user is locked') {
        modalOptions.headerHtml = 'Login is locked';
        modalOptions.bodyHtml = 'Your login is locked. ' +
          'Please contact your system admnistrator for more information.';
        modalDefaults.templateUrl = '/assets/javascripts/common/modalOk.html';
      } else {
        modalOptions.headerHtml = 'Login error';
        modalOptions.bodyHtml = 'Cannot login: ' + error.data.message;
        modalDefaults.templateUrl = '/assets/javascripts/common/modalOk.html';
      }

      modalService.showModal(modalDefaults, modalOptions)
        .then(stateHelper.reloadAndReinit())
        .catch(function() {
          $state.go('home');
        });
    }

    function login(credentials) {
      usersService.login(credentials).then(loginSuccess).catch(loginFailure);
    }
  }

});
