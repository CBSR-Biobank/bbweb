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

  BiobankAdminCtrl.$inject = ['adminService'];

  function BiobankAdminCtrl(adminService) {
    var vm = this;

    vm.counts = {};

    init();

    //--

    function init() {
      adminService.aggregateCounts().then(function (aggregateCounts) {
        vm.counts = {
          studies: aggregateCounts.studies,
          centres: aggregateCounts.centres,
          users:   aggregateCounts.users
        };
      });
    }
  }

  return biobankAdminDirective;
});
