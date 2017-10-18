/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */

/**
 * Allows the user to log in.
 *
 * Allow for autofill / autocomplete.
 */
const COMPONENT = {
  template: require('./login.html'),
  controller: LoginController,
  controllerAs: 'vm',
  bindings: {
  }
};

/*
 * Controller for this component.
 */
/* @ngInject */
function LoginController($state,
                         gettextCatalog,
                         usersService,
                         modalService) {
  var vm = this;

  vm.$onInit = onInit;

  //--

  function onInit() {
    vm.credentials = {
      email: '',
      password: ''
    };

    vm.login = login;

    if (usersService.isAuthenticated()) {
      // user already logged in, send him to home page
      $state.go('home');
    }
  }

  function goToHomeState() {
    $state.go('home');
  }

  function returnToLoginState() {
    $state.go('home.users.login', {}, { reload: true });
  }

  function login(credentials) {
    usersService.login(credentials).then(goToHomeState).catch(loginFailure);
  }

  function loginFailure(error) {
    var header, body;

    if (!error.hasOwnProperty('data') || (error.data === null)) {
      header = gettextCatalog.getString('Login error');
      body = gettextCatalog.getString('Cannot login: server is not reachable.');
    } else if (error.status === 401) {
      header = gettextCatalog.getString('Cannot log in');
      body = gettextCatalog.getString('The email and / or password you entered are invalid.');
    } else {
      header = gettextCatalog.getString('Cannot log in');
      body = gettextCatalog.getString('Server error.');
    }

    return modalService.modalOk(header, body)
      .then(returnToLoginState)
      .catch(goToHomeState);
  }

}

export default ngModule => ngModule.component('login', COMPONENT)
