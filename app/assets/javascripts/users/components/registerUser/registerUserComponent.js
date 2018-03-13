/*
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

const COMPONENT = {
  template: require('./registerUser.html'),
  controller: RegisterUserController,
  controllerAs: 'vm',
  bindings: {}
};

/**
 *
 */
/* @ngInject */
function RegisterUserController($state,
                                gettextCatalog,
                                User,
                                notificationsService) {
  var vm = this;
  vm.$onInit = onInit;

  //--

  function onInit() {
    vm.user = new User();
    vm.password = '';
    vm.confirmPassword = '';

    vm.submit = submit;
    vm.cancel = cancel;
  }

  function submit() {
    vm.user.register(vm.password)
      .then(registerSuccess)
      .catch(registerFailure);
  }

  function registerSuccess() {
    // user has been registerd
    notificationsService.success(
      gettextCatalog.getString('Your account was created and is now pending administrator approval.'),
      gettextCatalog.getString('Registration success'),
      4000);
    $state.go('home.users.login');
  }

  function registerFailure(err) {
    var message;
    if ((err.status === 403) && (err.data.message === 'already registered')) {
      message = gettextCatalog.getString('That email address is already registered.');
    } else if (err.data) {
      message = err.data.message;
    } else {
      message = 'Registration failed';
    }

    // registration failed
    notificationsService.error(message,
                               gettextCatalog.getString('Registration error'),
                               4000);
  }

  function cancel() {
    $state.go('home');
  }
}

export default ngModule => ngModule.component('registerUser', COMPONENT)
