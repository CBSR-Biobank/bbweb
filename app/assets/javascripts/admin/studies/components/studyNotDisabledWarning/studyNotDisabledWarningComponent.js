/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2017 Canadian BioSample Repository (CBSR)
 */
define(function () {
  'use strict';

  var component = {
    templateUrl : '/assets/javascripts/admin/studies/components/studyNotDisabledWarning/studyNotDisabledWarning.html',
    controller: StudyNotDisabledWarningController,
    controllerAs: 'vm',
    bindings: {
      study: '='
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
