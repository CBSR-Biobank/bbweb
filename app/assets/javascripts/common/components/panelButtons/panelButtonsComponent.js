/**
 * An AngularJS Component available to the rest of the application.
 *
 * @namespace common.components.panelButtons
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

class PanelButtonsController {

  constructor() {
    'ngInject';
    Object.assign(this, {});
  }

  addButtonPressed($event) {
    $event.preventDefault();
    $event.stopPropagation();
    this.onAdd();
  }
}

/**
 * An AngularJS component that displays two right justified buttons:
 *
 * - an `Add` button containing a `plus` icon
 *
 * - a second button that allows the panel it is used in to be collapsed
 *
 * Meant to be used in a *UI Bootstrap* Panel to add a {@link domain|Domain Entity}.
 *
 * @memberOf common.components.panelButtons
 *
 * @param {function} onAdd - the function to call when the user presses the `Add` button.
 *
 * @param {string} addButtonTitle - the text to display for the button's tooltip.
 *
 * @param {boolean} addButtonEnabled - when `TRUE` the `Add` button is enabled.
 *
 * @param {boolean} panelOpen - the bound value to write to when the panel is collapsed or expanded.
 */
const panelButtonsComponent = {
  template: require('./panelButtons.html'),
  controller: PanelButtonsController,
  controllerAs: 'vm',
  bindings: {
    onAdd:            '&',
    addButtonTitle:   '@',
    addButtonEnabled: '<',
    panelOpen:        '<'
  }
};

export default ngModule => ngModule.component('panelButtons', panelButtonsComponent)
