define(['../module'], function(module) {
  'use strict';

  module.directive('infoUpdateRemoveButtons', infoUpdateRemoveButtons);

  /**
   * Displays a right justified button group with the following buttons:
   *
   *  - information,
   *  - update
   *  - remove
   */
  function infoUpdateRemoveButtons() {
    var directive = {
      restrict: 'E',
      replace: 'true',
      scope: {
        info: '&onInfo',
        update: '&onUpdate',
        remove: '&onRemove',
        updatedButtonEnabled: '&',
        removeButtonEnabled: '&'
      },
      templateUrl: '/assets/javascripts/common/directives/infoUpdateRemoveButtons.html'
    };
    return directive;
  }

});
