/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(function(){
  'use strict';

  /**
   * Common functions for panels.
   *
   * Stores the panel's open / closed state in local storage.
   *
   * @return {domain.common.Panel} A Panel object.
   */
  /* @ngInject */
  function PanelFactory($window, $state) {

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

    return Panel;
  }

  return PanelFactory;
});
