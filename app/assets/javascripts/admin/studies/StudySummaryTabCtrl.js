define(['../module'], function(module) {
  'use strict';

  module.controller('StudySummaryTabCtrl', StudySummaryTabCtrl);

  StudySummaryTabCtrl.$inject = ['$state', '$filter', 'study'];

  /**
   * Displays the study administrtion page, with a number of tabs. Each tab displays the configuration
   * for a different aspect of the study.
   */
  function StudySummaryTabCtrl($state, $filter, study) {
    var vm = this;

    vm.study = study;
    vm.description = study.description;
    vm.descriptionToggle = true;
    vm.descriptionToggleLength = 100;

    vm.changeStatus = changeStatus;
    vm.truncateDescriptionToggle = truncateDescriptionToggle;

    //--

    function changeStatus(study) {
      if (study.id) {
        alert('change status of ' + study.name);
        return;
      }
      throw new Error('study does not have an ID');
    }

    function truncateDescriptionToggle() {
      if (vm.descriptionToggle) {
        vm.description = $filter('truncate')(vm.study.description, vm.descriptionToggleLength);
      } else {
        vm.description = vm.study.description;
      }
      vm.descriptionToggle = !vm.descriptionToggle;
    }

  }

});
