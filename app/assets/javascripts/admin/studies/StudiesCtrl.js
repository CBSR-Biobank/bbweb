define([], function() {
  'use strict';

  StudiesCtrl.$inject = [
    '$scope',
    'Study',
    'StudyStatus',
    'studyCounts'];

  /**
   * Displays a list of studies with each in its own mini-panel.
   *
   */
  function StudiesCtrl($scope, Study, StudyStatus, studyCounts) {
    var vm = this;

    vm.studyCounts      = studyCounts;
    vm.pageSize         = 5;
    vm.updateStudies    = Study.list;
    vm.possibleStatuses = [ { id: 'all', label: 'All' } ];

    _.each(StudyStatus.values(), function (status) {
      vm.possibleStatuses.push({id: status.toLowerCase(), label: status});
    });
  }

  return StudiesCtrl;
});
