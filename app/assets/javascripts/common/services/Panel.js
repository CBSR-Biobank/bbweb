define(['../module'], function(module) {
  'use strict';

  module.service('Panel', PanelFactory);

  PanelFactory.$inject = ['$window', '$state', 'panelTableService'];

  /**
   * Common functions for panels.
   *
   * Stores the panel's open / closed state in local storage.
   */
  function PanelFactory($window, $state, panelTableService) {

    function Panel(panelId, addStateName) {
      var self = this;

      self.panelId = panelId;
      self.addStateName = addStateName;
      if ($window.localStorage.getItem(self.panelId) === null) {
        $window.localStorage.setItem(self.panelId, 'true');
      }

      self.panelOpen = ($window.localStorage.getItem(this.panelId) === 'true');
    }

    Panel.prototype.add = function () {
      $state.go(this.addStateName);
    };

    Panel.prototype.panelToggle = function () {
      var currentState = ($window.localStorage.getItem(this.panelId) === 'true');
      this.panelOpen = !currentState;
      $window.localStorage.setItem(this.panelId, this.panelOpen);
      console.log('panelOpen', this.panelId, this.panelOpen, $window.localStorage.getItem(this.panelId));
      return this.panelOpen;
    };

    /**
     * Use the table settings parameter so that the table's page counts widget is not displayed
     */
    Panel.prototype.getTableParams = function (data) {
      return panelTableService.getTableParams(data, {}, {counts: []});
    };

    return Panel;
  }

});
