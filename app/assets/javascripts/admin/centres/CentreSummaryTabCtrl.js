/**
 * Centre administration controllers.
 */
define(['./module'], function(module) {
  'use strict';

  /**
   * Displays the centre administrtion page, with a number of tabs. Each tab displays the configuration
   * for a different aspect of the centre.
   */
  module.controller('CentreSummaryTabCtrl', CentreSummaryTabCtrl);

  CentreSummaryTabCtrl.$inect = [
    '$state', '$filter', 'user', 'centre'
  ];

  function CentreSummaryTabCtrl($state, $filter, user, centre) {
    var vm = this;
    vm.centre = centre;
    vm.description = centre.description;
    vm.descriptionToggle = true;
    vm.descriptionToggleLength = 100;
    vm.changeStatus = changeStatus;
    vm.truncateDescriptionToggle = truncateDescriptionToggle;

    //----

    function changeStatus(centre) {
      if (centre.id) {
        alert('change status of ' + centre.name);
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
