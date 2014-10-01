define(['../module'], function(module) {
  'use strict';

  module.service('stateHelper', stateHelper);

  stateHelper.$inject = ['$state'];

  /**
   * Utility function dealing with states for ui-router.
   */
  function stateHelper($state) {
    var service = {
      reloadAndReinit: reloadAndReinit,
      reloadStateAndReinit: reloadStateAndReinit
    };

    return service;

    //-------

    function reloadState(stateName, stateParams, stateOptions) {
      var params = stateParams || {};
      var options = stateOptions || {};
      $state.transitionTo(stateName, params, options);
    }

    /**
     * Hack for ui-router to reload state and re-initialize the controller.
     */
    function reloadAndReinit() {
      return reloadState($state.current);
    }

    function reloadStateAndReinit(stateName, stateParams, stateOptions) {
      return reloadState(stateName, stateParams, stateOptions);
    }
  }

});
