/**
 *
 */

var component = {
  template: require('./studyProcessingTab.html'),
  controller: StudyProcessingTabController,
  controllerAs: 'vm',
  bindings: {
    study:         '<',
    processingDto: '<'
  }
};

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

export default ngModule => ngModule.component('studyProcessingTab', component)
