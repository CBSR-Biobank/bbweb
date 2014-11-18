define(['../../module'], function(module) {
  'use strict';

  module.directive('specimenGroupsPanel', specimenGroupsPanel);

  /**
   *
   */
  function specimenGroupsPanel() {
    var directive = {
      require: '^tab',
      restrict: 'E',
      scope: {
        study: '=',
        specimenGroups: '=',
        specimenGroupIdsInUse: '='
      },
      templateUrl: '/assets/javascripts/admin/studies/specimenGroups/specimenGroupsPanel.html',
      controller: 'SpecimenGroupsPanelCtrl as vm'
    };
    return directive;
  }

});
