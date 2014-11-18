define(['../../module'], function(module) {
  'use strict';

  module.directive('spcLinkTypesPanel', spcLinkTypesPanel);

  /**
   *
   */
  function spcLinkTypesPanel() {
    var directive = {
      require: '^tab',
      restrict: 'E',
      scope: {
        study: '=',
        processingDto: '='
      },
      templateUrl: '/assets/javascripts/admin/studies/processing/spcLinkTypesPanel.html',
      controller: 'SpcLinkTypesPanelCtrl as vm'
    };
    return directive;
  }

});
