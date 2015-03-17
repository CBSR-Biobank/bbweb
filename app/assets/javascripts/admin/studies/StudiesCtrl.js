define([], function() {
  'use strict';

  StudiesCtrl.$inject = ['$scope', 'studiesService', 'studyCounts'];

  /**
   * Displays a list of studies with each in its own mini-panel.
   *
   */
  function StudiesCtrl($scope, studiesService, studyCounts) {
    var vm = this;

    vm.studyCounts      = studyCounts;
    vm.pageSize         = 5;
    vm.updateStudies    = updateStudies;
    vm.possibleStatuses = [
      { id: 'all',      label: 'All' },
      { id: 'disabled', label: 'Disabled' },
      { id: 'enabled',  label: 'Enabled' },
      { id: 'retired',  label: 'Retired' }
    ];

    function updateStudies(options) {
      return studiesService.list(options);
    }
  }

  return StudiesCtrl;
});
