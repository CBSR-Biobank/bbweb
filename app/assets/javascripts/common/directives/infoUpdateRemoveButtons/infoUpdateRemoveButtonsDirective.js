/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */

/**
 * Displays a right justified button group with the following buttons:
 *
 *  - information,
 *  - update
 *  - remove
 */
function infoUpdateRemoveButtonsFactory() {
  var directive = {
    restrict: 'E',
    replace: true,
    scope: {
      info:                '&onInfo',
      update:              '&onUpdate',
      remove:              '&onRemove',
      updateButtonEnabled: '&',
      removeButtonEnabled: '&'
    },
    template: require('./infoUpdateRemoveButtons.html')
  };
  return directive;
}

export default ngModule => ngModule.directive('infoUpdateRemoveButtons', infoUpdateRemoveButtonsFactory)
