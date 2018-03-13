/*
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

/**
 * A controller base class for components that display tabbed pages.
 *
 * @memberOf common.controllers
 */
class TabbedPageController {

  /**
   * @param {common.controllers.TabbedPageController.TabInfo} tabs - the tab objects that contain the tab
   * information.
   *
   * @param {int} active - the index of the selected tab int `tabs`.
   *
   * @param {AngularJS} $scope - the scope object this controller inherits from.
   *
   * @param {UI_Rourter} $state - the UI Router state object.
   */
  constructor(tabs, active, $scope, $state) {
    Object.assign(this, { tabs, active, $scope, $state });
    $scope.$on('tabbed-page-update', this.activeTabUpdate.bind(this));
  }

  /**
   * Updates the selected tab.
   *
   * This function is called when event `tabbed-page-update` is emitted by child scopes.
   *
   * @protected
   */
  activeTabUpdate(event, tabName) { // eslint-disable-line no-unused-vars
    event.stopPropagation();
    this.tabs.forEach((tab, index) => {
      tab.active = (this.$state.current.name.indexOf(tab.sref) >= 0);
      if (tab.active) {
        this.active = index;
      }
    });
  }

}

/**
 * Used by {@link common.controllers.TabbedPageController TabbedPageController} to store page tab information.
 *
 * @typedef common.controllers.TabbedPageController.TabInfo
 * @type object
 *
 * @property {string} heading - the text to display in the tab.
 *
 * @property {string} sref - The **UI Router** state to change to when this tab is clicked on.
 *
 * @property {boolean} active - when `TRUE` the link for this tab is active.
 */

export { TabbedPageController }

// this controller does not need to be included in AngularJS since it is imported by the controllers that
// extend it
export default () => {}
