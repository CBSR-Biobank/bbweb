/**
 *
 */
define(function () {
  'use strict';

  var component = {
    templateUrl: '/assets/javascripts/admin/studies/components/studyProcessingTab/studyProcessingTab.html',
    controller: StudyProcessingTabController,
    controllerAs: 'vm',
    bindings: {
      study:         '<',
      processingDto: '<'
    }
  };

  //StudyProcessingTabController.$inject = [];

  /*
   * Controller for this component.
   */
  function StudyProcessingTabController() {

  }

  return component;
});
