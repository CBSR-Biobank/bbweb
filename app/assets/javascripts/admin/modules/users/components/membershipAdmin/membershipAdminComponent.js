/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2017 Canadian BioSample Repository (CBSR)
 */

/*
 * Allows the logged in user to modify another user's memberships.
 */
var component = {
  template: require('./membershipAdmin.html'),
  controller: MembershipAdminsController,
  controllerAs: 'vm',
  bindings: {
  }
};

/*
 * Controller for this component.
 */
/* @ngInject */
function MembershipAdminsController(breadcrumbService) {
  var vm = this;
  vm.$onInit = onInit;

  //--

  function onInit() {
    vm.breadcrumbs = [
      breadcrumbService.forState('home'),
      breadcrumbService.forState('home.admin'),
      breadcrumbService.forState('home.admin.users'),
      breadcrumbService.forState('home.admin.users.memberships'),
    ];
  }

}

export default ngModule => ngModule.component('membershipAdmin', component)
