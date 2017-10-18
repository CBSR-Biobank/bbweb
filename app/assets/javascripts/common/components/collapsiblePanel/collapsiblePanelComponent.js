/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */

/**
 * A panel that can be collapsed by the user.
 */
const component = {
  template: require('./collapsiblePanel.html'),
  transclude: true,
  controller: CollapsiblePanelController,
  controllerAs: 'vm',
  bindings: {
    heading:     '@',
    collapsible: '<'
  }
};

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

export default ngModule => ngModule.component('collapsiblePanel', component)
