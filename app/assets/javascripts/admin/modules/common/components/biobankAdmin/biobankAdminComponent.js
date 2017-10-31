/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2017 Canadian BioSample Repository (CBSR)
 */

/**
 * Home page for Administration tasks.
 */
var component = {
  template: require('./biobankAdmin.html'),
  controller: BiobankAdminController,
  controllerAs: 'vm',
  bindings: {
  }
};

/*
 * Controller for this component.
 */
/* @ngInject */
function BiobankAdminController(adminService, usersService, User, breadcrumbService) {
  var vm = this;
  vm.$onInit = onInit;

  //--

  function onInit() {
    vm.counts = {};

    vm.breadcrumbs = [
      breadcrumbService.forState('home'),
      breadcrumbService.forState('home.admin')
    ];

    usersService.requestCurrentUser()
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

export default ngModule => ngModule.component('biobankAdmin', component)
