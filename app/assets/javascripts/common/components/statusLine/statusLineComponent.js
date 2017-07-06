/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2017 Canadian BioSample Repository (CBSR)
 */
define(function () {
  'use strict';

  var component = {
    templateUrl: '/assets/javascripts/common/components/statusLine/statusLine.html',
    controller: StatusLineController,
    controllerAs: 'vm',
    bindings: {
      item:      '<',
      showState: '<'
    }
  };

  //StatusLineController.$inject = [];

  /*
   * Controller for this component.
   */
  function StatusLineController() {
  }

  return component;
});
