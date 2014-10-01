define(['../../module'], function(module) {
  'use strict';

  module.directive('processingTypesPanel', processingTypesPanel);

  /**
   *
   */
  function processingTypesPanel() {
    var directive = {
      require: '^tab',
      restrict: 'E',
      scope: {
        processingDto: '='
      },
      templateUrl: '/assets/javascripts/admin/studies/processing/processingTypesPanel.html',
      controller: 'ProcessingTypesPanelCtrl as vm'
    };
    return directive;
  }

});
