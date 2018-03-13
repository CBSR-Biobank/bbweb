/**
 * AngularJS component available to the rest of the application.
 *
 * @namespace common.components.collapsiblePanel
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

function CollapsiblePanelController() {
  var vm = this;
  vm.$onInit = onInit;

  //--

  function onInit() {
    vm.panelOpen = true;
    vm.panelButtonClicked = panelButtonClicked;
  }

  function panelButtonClicked() {
    vm.panelOpen = ! vm.panelOpen;
  }
}

/**
 * An AngularJS component that displays a panel that can be collapsed by the user.
 *
 * @memberOf common.components.collapsiblePanel
 *
 * @param {string} heading - the text to display in the panel's heading.
 *
 * @param {boolean} collapsible - when `TRUE` the panel displays a button that lets the user collapse it.
 */
const collapsiblePanelComponent = {
  template: require('./collapsiblePanel.html'),
  transclude: true,
  controller: CollapsiblePanelController,
  controllerAs: 'vm',
  bindings: {
    heading:     '@',
    collapsible: '<'
  }
};

export default ngModule => ngModule.component('collapsiblePanel', collapsiblePanelComponent)
