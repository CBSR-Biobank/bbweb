define(['../module'], function(module) {
  'use strict';

  module.directive('centreStudiesPanel', centreStudiesPanel);

  /**
   *
   */
  function centreStudiesPanel() {
    var directive = {
      require: '^tab',
      restrict: 'EA',
      scope: {
        centre: '=',
        centreStudies: '=',
        allStudies: '='
      },
      templateUrl: '/assets/javascripts/admin/centres/studiesPanel.html',
      controller: 'CentreStudiesPanelCtrl as vm'
    };
    return directive;
  }

});
