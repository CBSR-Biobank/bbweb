/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(function () {
  'use strict';

  /**
   * Used for user log in.
   *
   * Allow for autofill / autocomplete. See the following web page for an explanation:
   *
   * http://timothy.userapp.io/post/63412334209/form-autocomplete-and-remember-password-with-angularjs
   */
  function loginDirective() {
    var directive = {
      restrict: 'E',
      templateUrl : '/assets/javascripts/users/directives/login/login.html',
      controller: LoginCtrl,
      controllerAs: 'vm'
    };

    return directive;
  }

  LoginCtrl.$inject = [
    '$state',
    'gettext',
    'usersService',
    'modalService',
  ];

  function LoginCtrl($state,
                     gettext,
                     usersService,
                     modalService) {
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
      var header, body;

      if (!error.hasOwnProperty('data') || (error.data === null)) {
        header = gettext('Login error');
        body = gettext('Cannot login: server is not reachable.');
    } else {
      switch (error.data.message)  {
      case 'invalid email':
      case 'InvalidPassword':
        header = gettext('Invalid login credentials');
        body = gettext('The email and / or password you entered are invalid.');
        break;

      case 'the user is not active':
        header = gettext('Login not active');
        body = gettext('Your login is not active yet. ' +
                       'Please contact your system admnistrator for more information.');
        break;

      case 'the user is locked':
        header = gettext('Login is locked');
        body = gettext('Your login is locked. ' +
                       'Please contact your system admnistrator for more information.');
        break;

      default:
        header = gettext('Login error');
        body = gettext('Cannot login: ') + error.data.message;
      }
    }

    return modalService.modalOk(header, body)
        .then(returnToLoginState)
        .catch(goToHomeState);
    }

  }

  return loginDirective;
});
