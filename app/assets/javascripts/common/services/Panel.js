define([], function(){
  'use strict';

  PanelFactory.$inject = ['$window', '$state', 'panelTableService'];

  /**
   * Common functions for panels.
   *
   * Stores the panel's open / closed state in local storage.
   */
  function PanelFactory($window, $state, panelTableService) {

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
    Panel.prototype.getTableParams = function (data) {
      return panelTableService.getTableParams(data, {}, {counts: []});
    };

    return Panel;
  }

  return PanelFactory;
});
