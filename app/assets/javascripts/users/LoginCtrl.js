define(['./module', 'toastr'], function(module, toastr) {
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
    vm.credentials = {
      email: '',
      password: ''
    };
    vm.login = login;

    if ($state.current.data.notifications.length > 0) {
      toastr.success(
        $state.current.data.notifications,
        'Registration success',
        {
          closeButton: true,
          timeOut:  0,
          extendedTimeOut: 0,
          positionClass: 'toast-bottom-right'
        });
    }

    //--

    function loginSuccess() {
      $state.go('dashboard');
    }

    function loginFailure(errorMessage) {
      var modalDefaults = {};
      var modalOptions = {};

      if (errorMessage === 'invalid email or password') {
        modalOptions.closeButtonText = 'Cancel';
        modalOptions.actionButtonText = 'Retry';
        modalOptions.headerText = 'Invalid login credentials';
        modalOptions.bodyText = 'The email and / or password you entered are invalid.';
      } else if (errorMessage === 'the user is not active') {
        modalOptions.headerText = 'Login not active';
        modalOptions.bodyText = 'Your login is not active yet. ' +
          'Please contact your system admnistrator for more information.';
        modalDefaults.templateUrl = '/assets/javascripts/common/modalOk.html';
      } else if (errorMessage === 'the user is locked') {
        modalOptions.headerText = 'Login is locked';
        modalOptions.bodyText = 'Your login is locked. ' +
          'Please contact your system admnistrator for more information.';
        modalDefaults.templateUrl = '/assets/javascripts/common/modalOk.html';
      } else {
        modalOptions.headerText = 'Login error';
        modalOptions.bodyText = 'Cannot login: ' + errorMessage;
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
