define([], function() {
  'use strict';

  CollectionCtrl.$inject = ['studiesService', 'studyCounts'];

  /**
   *
   */
  function CollectionCtrl(studiesService, studyCounts) {
    var vm = this;

    vm.studyCounts = studyCounts;
    vm.haveEnabledStudies = (studyCounts.enabledCount > 0);
    vm.updateEnabledStudies = updateEnabledStudies;
    vm.getEnabledStudiesPanelHeader = getEnabledStudiesPanelHeader;

    //---

    function updateEnabledStudies(options) {
      return studiesService.list(options);
    }

    function getEnabledStudiesPanelHeader() {
      return 'Studies you participate in';
    }

  }

  return CollectionCtrl;
});
