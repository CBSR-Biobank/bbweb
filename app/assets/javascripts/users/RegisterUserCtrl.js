/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(function () {
  'use strict';

  RegisterUserCtrl.$inject = ['$state', 'User', 'notificationsService'];

  /**
   * Allows the user to register.
   *
   * Template file: registerUserForm.html
   * State definition: states.js
   */
  function RegisterUserCtrl($state, User, notificationsService) {
    var vm = this;

    vm.user = new User();
    vm.password = '';
    vm.confirmPassword = '';

    vm.submit = submit;
    vm.cancel = cancel;

    //----

    function submit(user) {
      vm.user.register(vm.password)
        .then(registerSuccess)
        .catch(registerFailure);
    }

    function registerSuccess() {
      // user has been registerd
      notificationsService.success(
        'Your account was created and is now pending administrator approval.',
        'Registration success',
        4000);
      $state.go('home.users.login');
    }

    function registerFailure(err) {
      var message;
      if ((err.status === 403) && (err.data.message === 'already registered')) {
        message = 'That email address is already registered.';
      } else {
        message = err.data.message;
      }

      // registration failed
      notificationsService.error(message, 'Registration error', 4000);
    }

    function cancel() {
      $state.go('home');
    }
  }

  return RegisterUserCtrl;
});
