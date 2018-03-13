/**
 * AngularJS Component for {@link domain.users.User User} administration.
 *
 * @namespace admin.users.components.accessRoles
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

/*
 * Controller for this component.
 */
/* @ngInject */
function AccessRolesController(breadcrumbService) {
  var vm = this;
  vm.$onInit = onInit;

  //--

  function onInit() {
    vm.breadcrumbs = [
      breadcrumbService.forState('home'),
      breadcrumbService.forState('home.admin'),
      breadcrumbService.forState('home.admin.access'),
      breadcrumbService.forState('home.admin.access.roles'),
    ];
  }

}

/**
 * An AngularJS component that allows the logged in user to modify another {@link domain.users.User User's}
 * {@link domain.access.Role Roles}.
 *
 * @memberOf admin.users.components.accessRoles
 *
 * @param {domain.users.User} user - the user to modify roles for.
 */
const accessRolesComponent = {
  template: require('./accessRoles.html'),
  controller: AccessRolesController,
  controllerAs: 'vm',
  bindings: {
    user: '<'
  }
};

export default ngModule => ngModule.component('accessRoles', accessRolesComponent)
