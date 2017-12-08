/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */

var component = {
  template: require('./userAdmin.html'),
  controller: UserAdminController,
  controllerAs: 'vm'
};

/*
 * Shows a table of users.
 */
/* @ngInject */
function UserAdminController(userService, UserCounts, breadcrumbService, resourceErrorService) {
  var vm = this;
  vm.$onInit = onInit;

  //--

  function onInit() {
    vm.breadcrumbs = [
      breadcrumbService.forState('home'),
      breadcrumbService.forState('home.admin'),
      breadcrumbService.forState('home.admin.users')
    ];

    // request the user to determine if they have the right permissions to be on this page
    userService.requestCurrentUser()
      .then((user) => {
        vm.user = user;
      })
      .catch(resourceErrorService.checkUnauthorized());
  }
}

export default ngModule => ngModule.component('userAdmin', component)
