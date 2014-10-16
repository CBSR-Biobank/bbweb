define(['../../module'], function(module) {
  'use strict';

  module.directive('spcLinkAnnotTypesPanel', spcLinkAnnotTypesPanel);

  /**
   *
   */
  function spcLinkAnnotTypesPanel() {
    var directive = {
      require: '^tab',
      restrict: 'E',
      scope: {
        annotTypes: '=',
        spcLinkTypes: '='
      },
      templateUrl: '/assets/javascripts/admin/studies/annotationTypes/spcLinkAnnotTypesPanel.html',
      controller: 'SpcLinkAnnotTypesPanelCtrl as vm'
    };
    return directive;
  }

});
