/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(function (require) {
  'use strict';

  var _ = require('lodash');

  TabbedPageController.$inject = [
    'vm',
    '$scope',
    '$state',
  ];

  /**
   * Controller base class for components that display tabbed pages.
   *
   * Controller members:
   *
   *  - vm.tabs: the tab objects that contain the tab information.
   *
   *  - vm.active: the index of the tab selected by the user.
   *
   * @param {Object} vm - the controller's 'this' value.
   *
   */
  function TabbedPageController(vm, $scope, $state) {
    $scope.$on('tabbed-page-update', activeTabUpdate);

    //---

    /**
     * Updates the selected tab.
     *
     * This function is called when event 'tabbed-page-update' is emitted by child scopes.
     */
    function activeTabUpdate(event) {
      event.stopPropagation();
      _.each(vm.tabs, function (tab, index) {
        tab.active = ($state.current.name.indexOf(tab.sref) >= 0);
        if (tab.active) {
          vm.active = index;
        }
      });
    }

  }

  return TabbedPageController;
});
