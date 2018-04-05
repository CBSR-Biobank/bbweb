/**
 * AngularJS Component for user login.
 *
 * @namespace users.components.registerUser
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

class RegisterUserController {

  constructor($state,
              gettextCatalog,
              User,
              notificationsService) {
    'ngInject';
    Object.assign(this,
                  {
                    $state,
                    gettextCatalog,
                    User,
                    notificationsService
                  });
  }

  $onInit() {
    this.user = new this.User();
    this.password = '';
    this.confirmPassword = '';
  }

  submit() {
    this.user.register(this.password)
      .then(() => {
        // user has been registerd
        this.notificationsService.success(
          this.gettextCatalog.getString('Your account was created and is now pending administrator approval.'),
          this.gettextCatalog.getString('Registration success'),
          4000);
        this.$state.go('home.users.login');
      })
      .catch((err) => {
        var message;
        if ((err.status === 403) && (err.data.message === 'already registered')) {
          message = this.gettextCatalog.getString('That email address is already registered.');
        } else if (err.data) {
          message = err.data.message;
        } else {
          message = 'Registration failed';
        }

        // registration failed
        this.notificationsService.error(message,
                                   this.gettextCatalog.getString('Registration error'),
                                   4000);
      });
  }

  cancel() {
    this.$state.go('home');
  }
}

/**
 * An AngularJS component that allows a user to register with the application.
 *
 * @memberOf users.components.registerUser
 */
const registerUserComponent = {
  template: require('./registerUser.html'),
  controller: RegisterUserController,
  controllerAs: 'vm',
  bindings: {
  }
};

export default ngModule => ngModule.component('registerUser', registerUserComponent)
