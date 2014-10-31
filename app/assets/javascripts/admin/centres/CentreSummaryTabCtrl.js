/**
 * Centre administration controllers.
 */
define(['../module'], function(module) {
  'use strict';

  /**
   * Displays the centre administrtion page, with a number of tabs. Each tab displays the configuration
   * for a different aspect of the centre.
   */
  module.controller('CentreSummaryTabCtrl', CentreSummaryTabCtrl);

  CentreSummaryTabCtrl.$inject = ['$filter', 'centre'];

  function CentreSummaryTabCtrl($filter, centre) {
    var vm = this;
    vm.centre = centre;
    vm.description = centre.description;
    vm.descriptionToggle = true;
    vm.descriptionToggleLength = 100;
    vm.changeStatus = changeStatus;
    vm.truncateDescriptionToggle = truncateDescriptionToggle;

    //----

    function changeStatus() {
      if (vm.centre.id) {
        alert('change status of ' + vm.centre.name);
        return;
      }
      throw new Error('centre does not have an ID');
    }

    function truncateDescriptionToggle() {
      if (vm.descriptionToggle) {
        vm.description = $filter('truncate')(
          centre.description, vm.descriptionToggleLength);
      } else {
        vm.description = centre.description;
      }
      vm.descriptionToggle = !vm.descriptionToggle;
    }

  }

});
