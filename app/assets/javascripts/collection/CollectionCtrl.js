define(['./module'], function(module) {
  'use strict';

  module.controller('CollectionCtrl', CollectionCtrl);

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
      return studiesService.getStudies(options);
    }

    function getEnabledStudiesPanelHeader() {
      return '<strong>Enabled studies</strong>';
    }

  }

});
