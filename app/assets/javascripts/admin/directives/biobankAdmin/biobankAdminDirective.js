/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(function () {
  'use strict';

  /**
   *
   */
  function biobankAdminDirective() {
    var directive = {
      restrict: 'E',
      templateUrl : '/assets/javascripts/admin/directives/biobankAdmin/biobankAdmin.html',
      controller: BiobankAdminCtrl,
      controllerAs: 'vm'
    };

    return directive;
  }

  BiobankAdminCtrl.$inject = ['adminService', 'usersService', 'User'];

  function BiobankAdminCtrl(adminService, usersService, User) {
    var vm = this;

    vm.counts = {};

    init();

    //--

    function init() {
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

  return biobankAdminDirective;
});
