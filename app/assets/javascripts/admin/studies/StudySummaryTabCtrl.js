define(['../module'], function(module) {
  'use strict';

  module.controller('StudySummaryTabCtrl', StudySummaryTabCtrl);

  StudySummaryTabCtrl.$inject = ['$filter', 'study'];

  /**
   * Displays the study administrtion page, with a number of tabs. Each tab displays the configuration
   * for a different aspect of the study.
   */
  function StudySummaryTabCtrl($filter, study) {
    var vm = this;

    vm.study = study;
    vm.descriptionToggleControl = {}; // for truncateToggle directive
    vm.descriptionToggleState = true;
    vm.descriptionToggleLength = 100;

    vm.changeStatus = changeStatus;

    //--

    function changeStatus(study) {
      if (study.id) {
        alert('change status of ' + study.name);
        return;
      }
      throw new Error('study does not have an ID');
    }

  }

});
