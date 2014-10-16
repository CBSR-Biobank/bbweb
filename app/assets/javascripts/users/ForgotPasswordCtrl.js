define(['./module'], function(module) {
  'use strict';

  module.controller('ForgotPasswordCtrl', ForgotPasswordCtrl);

  ForgotPasswordCtrl.$inject = [
    '$state', '$stateParams', 'userService', 'modalService',
  ];

  /**
   *
   */
  function ForgotPasswordCtrl($state, $stateParams, userService, modalService) {
    var vm = this;
    vm.email = '';
    vm.submit = submit;
    vm.emailNotFound = $state.current.data.emailNotFound;

    //---

    function gotoReturnState() {
      $state.go('home');
    }

    function pwdResetSuccess() {
      $state.go('users.forgot.passwordSent', { email: vm.email });
    }

    function pwdResetFailure(response) {
      // user not found
      if (response.message === 'email address not registered') {
        $state.go('users.forgot.emailNotFound');
      } else {
        var headerText = 'Cannot reset your password';
        var bodyText = 'The account associated with that email is not active in the system. ' +
            'Please contact your system administrator for more information.';
        modalService.modalOk(headerText, bodyText)
          .then(gotoReturnState)
          .catch(gotoReturnState);
      }
    }

    function submit(email) {
      vm.email = email;
      userService.passwordReset(email).then(pwdResetSuccess).catch(pwdResetFailure);
    }

  }

});
