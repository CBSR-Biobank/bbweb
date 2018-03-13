/**
 * AngularJS Component for {@link domain.users.User User} administration.
 *
 * @namespace admin.users.components.userAdmin
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

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
      breadcrumbService.forState('home.admin.access')
    ];

    // request the user to determine if they have the right permissions to be on this page
    userService.requestCurrentUser()
      .then((user) => {
        vm.user = user;
      })
      .catch(resourceErrorService.checkUnauthorized());
  }
}

/**
 * An AngularJS component that displays the main page for {@link domain.users.User User} Adminisrattion.
 *
 * This page lists all users (in a paged fashion) in the system. The logged in user can then select one of
 * these users to configure it.
 *
 * @memberOf admin.users.components.userAdmin
 */
const userAdminComponent = {
  template: require('./userAdmin.html'),
  controller: UserAdminController,
  controllerAs: 'vm',
  bindings: {
  }
};

export default ngModule => ngModule.component('userAdmin', userAdminComponent)
