define(['../../module'], function(module) {
  'use strict';

  module.directive('ceventTypesPanel', ceventTypesPanel);

  /**
   *
   */
  function ceventTypesPanel() {
    var directive = {
      require: '^tab',
      restrict: 'E',
      scope: {
        ceventTypes: '=',
        annotTypes: '=',
        specimenGroups: '='
      },
      templateUrl: '/assets/javascripts/admin/studies/ceventTypes/ceventTypesPanel.html',
      controller: 'CeventTypesPanelCtrl as vm'
    };
    return directive;
  }

});
