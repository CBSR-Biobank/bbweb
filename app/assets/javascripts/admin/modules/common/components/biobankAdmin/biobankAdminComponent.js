/**
 * AngularJS Components used in Administration modules
 *
 * @namespace admin.common.components.biobankAdmin
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

/*
 * Controller for this component.
 */
/* @ngInject */
function BiobankAdminController(adminService, userService, User, breadcrumbService) {
  var vm = this;
  vm.$onInit = onInit;

  //--

  function onInit() {
    vm.counts = {};

    vm.breadcrumbs = [
      breadcrumbService.forState('home'),
      breadcrumbService.forState('home.admin')
    ];

    userService.requestCurrentUser()
      .then(function (user) {
        vm.user = user;
        return adminService.aggregateCounts();
      })
      .then(function (aggregateCounts) {
        vm.counts = {
          studies: aggregateCounts.studies,
          centres: aggregateCounts.centres,
          users:   aggregateCounts.users
        };
      });
  }
}

/**
 * An AngularJS component that displays the Administration Home page.
 * <p>
 * From this page the user is able to select various links that lead to other tasks.
 *
 * @memberOf admin.common.components.biobankAdmin
 */
const biobankAdminComponent = {
  template: require('./biobankAdmin.html'),
  controller: BiobankAdminController,
  controllerAs: 'vm',
  bindings: {
  }
};

export default ngModule => ngModule.component('biobankAdmin', biobankAdminComponent)
