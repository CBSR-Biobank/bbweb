/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(function () {
  'use strict';

  var component = {
    templateUrl : '/assets/javascripts/users/components/forgotPassword/forgotPassword.html',
    controller: ForgotPasswordController,
    controllerAs: 'vm',
    bindings: {}
  };

  ForgotPasswordController.$inject = [ '$state', 'usersService', 'modalService' ];

  /**
   * Allows the user to have his password reset by entering the email address he registered with.
   */
  function ForgotPasswordController($state, usersService, modalService) {
    var vm = this;

    vm.email = '';
    vm.submit = submit;

    //---

    function gotoReturnState() {
      $state.go('home');
    }

    function pwdResetSuccess() {
      $state.go('home.users.forgot.passwordSent', { email: vm.email });
    }

    function pwdResetFailure(response) {
      // user not found
      if (response.message === 'email address not registered') {
        $state.go('home.users.forgot.emailNotFound');
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

  return component;
});
