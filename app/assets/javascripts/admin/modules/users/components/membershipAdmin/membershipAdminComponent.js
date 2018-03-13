/**
 * AngularJS Component for {@link domain.users.User User} administration.
 *
 * @namespace admin.users.components.membershipAdmin
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

/*
 * Controller for this component.
 */
/* @ngInject */
function MembershipAdminController(breadcrumbService) {
  var vm = this;
  vm.$onInit = onInit;

  //--

  function onInit() {
    vm.breadcrumbs = [
      breadcrumbService.forState('home'),
      breadcrumbService.forState('home.admin'),
      breadcrumbService.forState('home.admin.access'),
      breadcrumbService.forState('home.admin.access.memberships'),
    ];
  }

}

/**
 * An AngularJS component that allows the logged in user to view {@link domain.access.Membership Memberships}.
 *
 * From this component, the logged in user is allowed to add, modify and remove {@link
 * domain.access.Membership Memberships}.
 *
 * @memberOf admin.users.components.membershipAdmin
 */
const membershipAdminComponent = {
  template: require('./membershipAdmin.html'),
  controller: MembershipAdminController,
  controllerAs: 'vm',
  bindings: {
  }
};

export default ngModule => ngModule.component('membershipAdmin', membershipAdminComponent)
