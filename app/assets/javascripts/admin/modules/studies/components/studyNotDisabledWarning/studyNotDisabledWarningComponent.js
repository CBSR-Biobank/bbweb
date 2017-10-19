/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2017 Canadian BioSample Repository (CBSR)
 */
define(function () {
  'use strict';

  var component = {
    template: require('./studyNotDisabledWarning.html'),
    controller: StudyNotDisabledWarningController,
    controllerAs: 'vm',
    bindings: {
      study: '<'
    }
  };

  //StudyNotDisabledWarningController.$inject = [];

  /*
   * Controller for this component.
   */
  function StudyNotDisabledWarningController() {

  }

  return component;
});
