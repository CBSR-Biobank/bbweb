define(['./module', 'toaster'], function(module, toaster) {
  'use strict';

  module.controller('RegisterUserCtrl', RegisterUserCtrl);

  RegisterUserCtrl.$inject = ['$state', '$stateParams', 'userService'];

  /**
   * Allows the user to register.
   *
   * Template file: registerUserForm.html
   * State definition: states.js
   */
  function RegisterUserCtrl($state, $stateParams, userService) {
    var vm = this;

    vm.user = {
      name: '',
      email: '',
      password: '',
      confirmPassword: '',
      avatarUrl: ''
    };
    vm.submit = submit;
    vm.cancel = cancel;

    //----

    function submit(user) {
      userService.add(user).then(
        function() {
          // user has been registerd
          toaster.success(
            'Your account was created and is now pending administrator approval.',
            'Registration success',
            {
              closeButton: true,
              timeOut:  0,
              extendedTimeOut: 0,
              positionClass: 'toast-bottom-right'
            });
          $state.go('users.login', {}, {reload: true});
        },
        function() {
          // registration failed
          toaster.error(
            'That email address is already registered.',
            'Registration error',
            {
              closeButton: true,
              timeOut:  0,
              extendedTimeOut: 0,
              positionClass: 'toast-bottom-right'
            });

          $state.go('users.register', {}, {reload: true});
        }
      );
    }

    function cancel() {
      $state.go('home');
    }
  }

});
