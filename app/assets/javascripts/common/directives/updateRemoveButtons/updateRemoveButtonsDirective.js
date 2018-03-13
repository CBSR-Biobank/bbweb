/*
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

/**
 * An AngularJS Directive that displays a right justified button group with the following buttons:
 *
 * - update
 * - remove
 *
 * @memberOf common.directives
 *
 * @param {function} onUpdate - the function to be called when the user presses the `Update` button.
 *
 * @param {function} onRemove - the function to be called when the user presses the `Remove` button.
 *
 * @param {boolean} updateButtonEnabled - when `TRUE` the `Update` button is enabled.
 *
 * @param {boolean} removeButtonEnabled - when `TRUE` the `Remove` button is enabled.
 */
class UpdateRemoveButtonsDirective {

  constructor(template = require('./updateRemoveButtons.html')) {
    this.restrict = 'E';
    this.replace = true;
    this.scope = {
      update:              '&onUpdate',
      remove:              '&onRemove',
      updateButtonEnabled: '=',
      removeButtonEnabled: '='
    };
    this.template = template;
  }

}

function updateRemoveButtonsDirectiveFactory() {
  return new UpdateRemoveButtonsDirective();
}

export { UpdateRemoveButtonsDirective }
export default ngModule => ngModule.directive('updateRemoveButtons', updateRemoveButtonsDirectiveFactory)
