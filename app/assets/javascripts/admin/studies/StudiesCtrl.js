define([], function() {
  'use strict';

  StudiesCtrl.$inject = ['$scope', 'Study', 'studyCounts'];

  /**
   * Displays a list of studies with each in its own mini-panel.
   *
   */
  function StudiesCtrl($scope, Study, studyCounts) {
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

    /**
     * Callback provided to pagedItemsList directive.
     *
     * See {@link Study#list} for the format of the options object.
     */
    function updateStudies(options) {
      return Study.list(options);
    }
  }

  return StudiesCtrl;
});
