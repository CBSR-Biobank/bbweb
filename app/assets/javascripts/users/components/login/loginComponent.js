/**
 * AngularJS Component for user login.
 *
 * @namespace users.components.login
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

/*
 * Controller for this component.
 */
class LoginController {

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
  }

  $onInit() {
    this.credentials = {
      email: '',
      password: ''
    };

    if (this.userService.isAuthenticated()) {
      // user already logged in, send him to home page
      this.$state.go('home');
    }
  }

  goToHomeState() {
    this.$state.go('home');
  }

  returnToLoginState() {
    this.$state.go('home.users.login', {}, { reload: true });
  }

  login(credentials) {
    this.userService.login(credentials.email, credentials.password)
      .then(this.goToHomeState.bind(this))
      .catch(this.loginFailure.bind(this));
  }

  loginFailure(error) {
    var header, body;

    if (!error.hasOwnProperty('data') || (error.data === null)) {
      header = this.gettextCatalog.getString('Login error');
      body = this.gettextCatalog.getString('Cannot login: server is not reachable.');
    } else if (error.status === 401) {
      header = this.gettextCatalog.getString('Cannot log in');
      body = this.gettextCatalog.getString('The email and / or password you entered are invalid.');
    } else {
      header = this.gettextCatalog.getString('Cannot log in');
      body = this.gettextCatalog.getString('Server error.');
    }

    return this.modalService.modalOk(header, body)
      .then(this.returnToLoginState.bind(this))
      .catch(this.goToHomeState.bind(this));
  }

}

/**
 * An AngularJS component that allows the user to log in.
 *
 * Allow for autofill / autocomplete.
 *
 * @memberOf users.components.login
 */
const loginComponent = {
  template: require('./login.html'),
  controller: LoginController,
  controllerAs: 'vm',
  bindings: {
  }
};

export default ngModule => ngModule.component('login', loginComponent)
