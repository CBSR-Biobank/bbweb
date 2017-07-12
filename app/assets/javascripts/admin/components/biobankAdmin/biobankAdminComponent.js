/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2017 Canadian BioSample Repository (CBSR)
 */
define(function () {
  'use strict';

  /**
   * Home page for Administration tasks.
   */
  var component = {
    templateUrl: '/assets/javascripts/admin/components/biobankAdmin/biobankAdmin.html',
    controller: BiobankAdminController,
    controllerAs: 'vm',
    bindings: {
    }
  };

  BiobankAdminController.$inject = ['adminService', 'usersService', 'User', 'breadcrumbService'];

  /*
   * Controller for this component.
   */
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

      usersService.requestCurrentUser().then(function (user) {
        vm.user = User.create(user);
        adminService.aggregateCounts().then(function (aggregateCounts) {
          vm.counts = {
            studies: aggregateCounts.studies,
            centres: aggregateCounts.centres,
            users:   aggregateCounts.users
          };
        });
      });
    }
  }

  return component;
});
