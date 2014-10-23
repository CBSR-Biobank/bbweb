define(['./module', 'toastr'], function(module, toastr) {
  'use strict';

  module.controller('RegisterUserCtrl', RegisterUserCtrl);

  RegisterUserCtrl.$inject = ['$state', '$stateParams', 'usersService'];

  /**
   * Allows the user to register.
   *
   * Template file: registerUserForm.html
   * State definition: states.js
   */
  function RegisterUserCtrl($state, $stateParams, usersService) {
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
      usersService.add(user).then(
        function() {
          // user has been registerd
          toastr.success(
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
          toastr.error(
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
