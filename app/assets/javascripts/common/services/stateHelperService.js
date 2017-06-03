/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(function () {
  'use strict';

  stateHelperService.$inject = ['$state', '$stateParams'];

  /**
   * Utility function dealing with ui-router states.
   */
  function stateHelperService($state, $stateParams) {
    var service = {
      reloadAndReinit:      reloadAndReinit,
      reloadStateAndReinit: reloadStateAndReinit,
      updateBreadcrumbs:    updateBreadcrumbs
    };
    return service;

    //-------

    /**
     * Hack for ui-router to reload state and re-initialize the controller.
     */
    function reloadAndReinit() {
      return $state.transitionTo(
        $state.current,
        $stateParams,
        { reload: true, inherit: false, notify: true });
    }

    /**
     * FIXME: this should be removed, it just mirrors the transition to call
     */
    function reloadStateAndReinit(stateName, stateParams, stateOptions) {
      var params = stateParams || {};
      var options = stateOptions || {};
      return $state.transitionTo(stateName, params, options);
    }

    /**
     * Reloads the current state in order to have breadcrumbs updated.
     */
    function updateBreadcrumbs() {
      reloadAndReinit();
    }
  }

  return stateHelperService;
});
