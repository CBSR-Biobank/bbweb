/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(function (){
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
      template: require('./updateRemoveButtons.html')
    };
  }

  return updateRemoveButtonsDirectiveFactory;
});
