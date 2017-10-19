/**
 *
 */
define(function () {
  'use strict';

  var component = {
    template: require('./studyProcessingTab.html'),
    controller: StudyProcessingTabController,
    controllerAs: 'vm',
    bindings: {
      study:         '<',
      processingDto: '<'
    }
  };

  StudyProcessingTabController.$inject = ['$scope'];

  /*
   * Controller for this component.
   */
  function StudyProcessingTabController($scope) {
    var vm = this;
    vm.$onInit = onInit;

    //--

    function onInit() {
      // updates the selected tab in 'studyViewDirective' which is the parent directive
      $scope.$emit('tabbed-page-update', 'tab-selected');
    }

  }

  return component;
});
