/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2017 Canadian BioSample Repository (CBSR)
 */

/*
 * Allows the logged in user to modify another user's roles.
 */
var component = {
  template: require('./accessRoles.html'),
  controller: AccessRolesController,
  controllerAs: 'vm',
  bindings: {
    user: '<'
  }
};

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

export default ngModule => ngModule.component('accessRoles', component)
