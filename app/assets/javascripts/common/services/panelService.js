define(['../module'], function(module) {
  'use strict';

  module.service('panelService', PanelService);

  PanelService.$inject = ['$window', '$state', 'panelTableService'];

  /**
   * Common functions for panels.
   *
   * Stores the panel's open / closed state in local storage.
   */
  function PanelService($window, $state, panelTableService) {
    var service = {
      panel: panel
    };

    return service;

    //--

    function panel(panelId, addStateName, modelObjModalService, modelObjModalTitle) {
      if ($window.localStorage.getItem(panelId) === null) {
        $window.localStorage.setItem(panelId, 'true');
      }

      var panelFunctions = {
        information: information,
        add: add,
        panelOpen: $window.localStorage.getItem(panelId) === 'true',
        panelToggle: panelToggle,
        getTableParams: getTableParams
      };
      return panelFunctions;

      function information(modelObj) {
        if (modelObjModalService === undefined) {
          throw new Error('domain object modal service not defined');
        }
        modelObjModalService.show(modelObjModalTitle, modelObj);
      }

      function add() {
        $state.go(addStateName);
      }

      function panelToggle() {
        var panelOpen = ($window.localStorage.getItem(panelId) !== 'true');
        $window.localStorage.setItem(panelId, panelOpen);
        return panelOpen;
      }

      /**
       * Use the table settings parameter so that the table's page counts widget is not displayed
       */
      function getTableParams(data) {
        return panelTableService.getTableParams(data, {}, {counts: []});
      }
    }
  }

});
