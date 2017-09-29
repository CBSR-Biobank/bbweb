/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(function () {
  'use strict';

  var component = {
    template: require('./forgotPassword.html'),
    controller: ForgotPasswordController,
    controllerAs: 'vm',
    bindings: {}
  };

  ForgotPasswordController.$inject = [
    '$state',
    'gettextCatalog',
    'usersService',
    'modalService'
  ];

  /**
   * Allows the user to have his password reset by entering the email address he registered with.
   */
  function ForgotPasswordController($state,
                                    gettextCatalog,
                                    usersService,
                                    modalService) {
    var vm = this;
    vm.$onInit = onInit;

    //--

    function onInit() {
      vm.email = '';
      vm.submit = submit;
    }

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
        modalService.modalOk(gettextCatalog.getString('Cannot reset your password'),
                             gettextCatalog.getString('The account associated with that email is not active in the system. ' +
                                     'Please contact your system administrator for more information.'))
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
