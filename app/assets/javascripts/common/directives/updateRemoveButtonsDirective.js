define([], function(){
  'use strict';

  /**
   * Displays a right justified button group with the following buttons:
   *
   *  - update
   *  - remove
   */
  function updateRemoveButtonsDirectiveFactory() {
    return {
      restrict: 'E',
      replace: true,
      scope: {
        update: '&onUpdate',
        remove: '&onRemove',
        updateButtonEnabled: '&',
        removeButtonEnabled: '&'
      },
      templateUrl: '/assets/javascripts/common/directives/updateRemoveButtons.html'
    };
  }

  return updateRemoveButtonsDirectiveFactory;
});
