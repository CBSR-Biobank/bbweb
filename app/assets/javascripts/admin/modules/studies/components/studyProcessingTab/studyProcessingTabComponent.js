/**
 * AngularJS Component for {@link domain.studies.Study Study} administration.
 *
 * @namespace admin.studies.components.studyProcessingTab
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

/*
 * Controller for this component.
 */
/* @ngInject */
function StudyProcessingTabController($scope) {
  var vm = this;
  vm.$onInit = onInit;

  //--

  function onInit() {
    // updates the selected tab in 'studyViewDirective' which is the parent directive
    $scope.$emit('tabbed-page-update', 'tab-selected');
  }

}

/**
 * An AngularJS component that displays the {@link domain.studies.ProcessingType ProcessingTypes} configured
 * for a {@link domain.studies.Study Study},
 *
 * @memberOf admin.studies.components.studyProcessingTab
 *
 * @param {domain.studies.Study} study - the *Study* to view *Processing Types* for.
 */
const studyProcessingTabComponent = {
  template: require('./studyProcessingTab.html'),
  controller: StudyProcessingTabController,
  controllerAs: 'vm',
  bindings: {
    study:         '<',
    processingDto: '<'
  }
};

export default ngModule => ngModule.component('studyProcessingTab', studyProcessingTabComponent)
