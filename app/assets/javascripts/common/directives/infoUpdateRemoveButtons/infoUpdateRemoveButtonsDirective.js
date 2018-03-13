/*
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

import { UpdateRemoveButtonsDirective } from '../updateRemoveButtons/updateRemoveButtonsDirective';

function infoUpdateRemoveButtonsFactory() {

  /**
   * An AngularJS Directive that displays a right justified button group with the following buttons:
   *
   * - information
   * - update
   * - remove
   *
   * @memberOf common.directives
   *
   * @param {function} onInfo - the function to be called when the user presses the `Info` button.
   *
   * @param {function} onUpdate - the function to be called when the user presses the `Update` button.
   *
   * @param {function} onRemove - the function to be called when the user presses the `Remove` button.
   *
   * @param {boolean} updateButtonEnabled - when `TRUE` the `Update` button is enabled.
   *
   * @param {boolean} removeButtonEnabled - when `TRUE` the `Remove` button is enabled.
   */
  class InfoUpdateRemoveButtonsDirective extends UpdateRemoveButtonsDirective {

    constructor() {
      super(require('./infoUpdateRemoveButtons.html'));
      this.scope.info = '&onInfo';
    }

  }

  return new InfoUpdateRemoveButtonsDirective();
}

export default ngModule => ngModule.directive('infoUpdateRemoveButtons', infoUpdateRemoveButtonsFactory)
