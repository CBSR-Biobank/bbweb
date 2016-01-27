/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define([], function() {
  'use strict';

  LoginCtrl.$inject = [
    '$state',
    'usersService',
    'modalService',
  ];

  /**
   * Used for user log in.
   *
   * Allow for autofill / autocomplete. See the following web page for an explanation:
   *
   * http://timothy.userapp.io/post/63412334209/form-autocomplete-and-remember-password-with-angularjs
   */
  function LoginCtrl($state, usersService, modalService) {
    var vm = this;

    vm.credentials = {
      email: '',
      password: ''
    };
    vm.login = login;

    init();

    //--

    function init() {
      if (usersService.isAuthenticated()) {
        // user already logged in, send him to home page
        $state.go('home');
      }
    }

    function goToHomeState() {
      $state.go('home');
    }

    function returnToLoginState() {
      $state.go('home.users.login', {}, { reload: true });
    }

    function login(credentials) {
      usersService.login(credentials).then(goToHomeState).catch(loginFailure);
    }

    function loginFailure(error) {
      var modalDefaults = {};
      var modalOptions = {};

      if (!error.hasOwnProperty('data') || (error.data === null)) {
          modalOptions.headerHtml = 'Login error';
          modalOptions.bodyHtml = 'Cannot login: server is not reachable.';
          modalDefaults.templateUrl = '/assets/javascripts/common/modalOk.html';
      } else {
        switch (error.data.message)  {
        case 'invalid email or password':
          modalOptions.closeButtonText = 'Cancel';
          modalOptions.actionButtonText = 'Retry';
          modalOptions.headerHtml = 'Invalid login credentials';
          modalOptions.bodyHtml = 'The email and / or password you entered are invalid.';
          break;

        case 'the user is not active':
          modalOptions.headerHtml = 'Login not active';
          modalOptions.bodyHtml = 'Your login is not active yet. ' +
            'Please contact your system admnistrator for more information.';
          modalDefaults.templateUrl = '/assets/javascripts/common/modalOk.html';
          break;

        case 'the user is locked':
          modalOptions.headerHtml = 'Login is locked';
          modalOptions.bodyHtml = 'Your login is locked. ' +
            'Please contact your system admnistrator for more information.';
          modalDefaults.templateUrl = '/assets/javascripts/common/modalOk.html';
          break;

        default:
          modalOptions.headerHtml = 'Login error';
          modalOptions.bodyHtml = 'Cannot login: ' + error.data.message;
          modalDefaults.templateUrl = '/assets/javascripts/common/modalOk.html';
        }
      }

      modalService.showModal(modalDefaults, modalOptions)
        .then(returnToLoginState)
        .catch(goToHomeState);
    }
  }

  return LoginCtrl;
});
