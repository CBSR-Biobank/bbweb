/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2017 Canadian BioSample Repository (CBSR)
 */

/**
 * @class ng.admin.common.components.annotationTypeView
 *
 * An AngularJS component that displays the Administration Home page.
 * <p>
 * From this page the user is able to select various links that lead to other tasks.
 *
 * @memberOf ng.admin.common.components
 */
var biobankAdmin = {
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

export default ngModule => ngModule.component('biobankAdmin', biobankAdmin)
