/**
 * User controllers.
 *
 */
define(['./module', 'toastr'], function(module, toastr) {
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

    if ($state.current.data.notifications.length > 0) {
      toastr.error(
        $state.current.data.notifications,
        'Registration error',
        {
          closeButton: true,
          timeOut:  0,
          extendedTimeOut: 0,
          positionClass: 'toast-bottom-right'
        });
    }

    //----

    function submit(user) {
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
    }

    function cancel() {
      $state.go('home');
    }
  }

});
