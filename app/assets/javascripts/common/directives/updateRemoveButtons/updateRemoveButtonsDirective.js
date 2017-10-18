/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */

/**
 * Displays a right justified button group with the following buttons:
 *
 *  - update
 *  - remove
 */
function updateRemoveButtonsDirective() {
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

export default ngModule => ngModule.directive('updateRemoveButtons', updateRemoveButtonsDirective)
