define(['../module'], function(module) {
  'use strict';

  module.directive('locationsPanel', locationsPanel);

  /**
   *
   */
  function locationsPanel() {
    var directive = {
      require: '^tab',
      restrict: 'EA',
      scope: {
        centre: '=',
        locations: '='
      },
      templateUrl: '/assets/javascripts/admin/centres/locationsPanel.html',
      controller: 'LocationsPanelCtrl as vm'
    };
    return directive;
  }

});
