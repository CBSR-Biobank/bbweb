/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(function () {
  'use strict';

  stateHelperService.$inject = ['$state'];

  /*
   * Utility function dealing with ui-router states.
   */
  function stateHelperService($state) {
    var service = {
      updateBreadcrumbs:    updateBreadcrumbs
    };
    return service;

    //-------

    /*
     * Reloads the current state in order to have breadcrumbs updated.
     */
    function updateBreadcrumbs() {
      $state.reload();
    }
  }

  return stateHelperService;
});
