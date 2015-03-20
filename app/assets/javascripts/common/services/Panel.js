define(['underscore'], function(_){
  'use strict';

  PanelFactory.$inject = ['$window', '$state', 'tableService'];

  /**
   * Common functions for panels.
   *
   * Stores the panel's open / closed state in local storage.
   */
  function PanelFactory($window, $state, tableService) {

    function Panel(panelId, addStateName) {
      var self = this, panelStateLocalStorage;

      self.panelId = panelId;
      self.addStateName = addStateName;

      panelStateLocalStorage = $window.localStorage.getItem(self.panelId);
      if ((panelStateLocalStorage === null) || (panelStateLocalStorage === ''))  {
        $window.localStorage.setItem(self.panelId, 'true');
      }
    }

    Panel.prototype.add = function () {
      $state.go(this.addStateName);
    };

    Panel.prototype.getPanelOpenState = function () {
      return ($window.localStorage.getItem(this.panelId) === 'true');
    };

    Panel.prototype.watchPanelOpenChangeFunc = function(newValue) {
      $window.localStorage.setItem(this.panelId, newValue.toString());
    };

    /**
     * Use the table settings parameter so that the table's page counts widget is not displayed
     */
    Panel.prototype.getTableParams = function (data, tableParameters, tableSettings) {
      tableParameters = tableParameters || {};
      tableSettings = tableSettings || {};
      tableSettings = _.defaults(tableSettings, {counts: []});
      return tableService.getTableParams(data, tableParameters, tableSettings);
    };

    return Panel;
  }

  return PanelFactory;
});
