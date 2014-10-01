define(['../module'], function(module) {
  'use strict';

  module.directive('updateRemoveButtons', updateRemoveButtons);

  /**
   * Displays a right justified button group with the following buttons:
   *
   *  - update
   *  - remove
   */
  function updateRemoveButtons() {
    return {
      restrict: 'E',
      replace: 'true',
      scope: {
        'update': '&onUpdate',
        'remove': '&onRemove'
      },
      templateUrl: '/assets/javascripts/common/directives/updateRemoveButtons.html'
    };
  }

});
