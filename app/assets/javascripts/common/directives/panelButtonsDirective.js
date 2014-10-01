define(['../module'], function(module) {
  'use strict';

  module.directive('panelButtons', panelButtons);

  /**
   * Displays a right justified button with a 'plus' icon. Meant to be used in a pane to add a
   * domain object.
   */
  function panelButtons() {
    var directive = {
      restrict: 'E',
      replace: 'true',
      scope: {
        add: '&onAdd',
        addButtonTitle: '@',
        panelToggle: '&',
        panelOpen: '&'
      },
      templateUrl: '/assets/javascripts/common/directives/panelButtons.html'
    };
    return directive;
  }

});
