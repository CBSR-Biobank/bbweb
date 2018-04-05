/**
 * AngularJS Component for user login.
 *
 * @namespace users.components.forgotPassword
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

class ForgotPasswordController {

  constructor($state,
              gettextCatalog,
              userService,
              modalService) {
    'ngInject';
    Object.assign(this,
                  {
                    $state,
                    gettextCatalog,
                    userService,
                    modalService
                  });
    this.email = '';
  }

  pwdResetFailure(response) {
    // happens when the user is not found
    if (response.message === 'email address not registered') {
      this.$state.go('home.users.forgot.emailNotFound');
      return;
    }

    const gotoReturnState = () => {
      this.$state.go('home');
    };

    this.modalService.modalOk(
      this.gettextCatalog.getString('Cannot reset your password'),
      this.gettextCatalog.getString('The account associated with that email is not active in the system. ' +
                                    'Please contact your system administrator for more information.'))
      .then(gotoReturnState)
      .catch(gotoReturnState);
  }

  submit(email) {
    this.email = email;
    this.userService.passwordReset(email)
      .then(() => {
        this.$state.go('home.users.forgot.passwordSent', { email: this.email });
      })
      .catch(this.pwdResetFailure.bind(this));
  }
}

/**
 * An AngularJS component allows the user  reset their password by entering the email address they
 * registered with.
 *
 * @memberOf users.components.forgotPassword
 */
const forgotPasswordComponent = {
  template: require('./forgotPassword.html'),
  controller: ForgotPasswordController,
  controllerAs: 'vm',
  bindings: {
  }
};

export default ngModule => ngModule.component('forgotPassword', forgotPasswordComponent)
