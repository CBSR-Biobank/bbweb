define(['./module'], function(module) {
  'use strict';

  module.controller('ForgotPasswordCtrl', ForgotPasswordCtrl);

  ForgotPasswordCtrl.$inject = [
    '$state', '$stateParams', 'usersService', 'modalService',
  ];

  /**
   *
   */
  function ForgotPasswordCtrl($state, $stateParams, usersService, modalService) {
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
        var headerHtml = 'Cannot reset your password';
        var bodyHtml = 'The account associated with that email is not active in the system. ' +
            'Please contact your system administrator for more information.';
        modalService.modalOk(headerHtml, bodyHtml)
          .then(gotoReturnState)
          .catch(gotoReturnState);
      }
    }

    function submit(email) {
      vm.email = email;
      usersService.passwordReset(email).then(pwdResetSuccess).catch(pwdResetFailure);
    }

  }

});
