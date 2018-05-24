/*
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

import _ from 'lodash'
import angular from 'angular'

function processingTypesPanelDirective() {  // eslint-disable-line no-unused-vars
  var directive = {
    require: '^tab',
    restrict: 'E',
    scope: {},
    bindToController: {
      study: '=',
      processingTypes: '='
    },
    template: require('./processingTypesPanel.html'),
    controller: ProcessingTypesPanelCtrl,
    controllerAs: 'vm'
  };

  return directive;
}

/**
 * A panel to display a study's processing types.
 */
/* @ngInject */
function ProcessingTypesPanelCtrl($scope,
                                  $state,
                                  Panel,
                                  ProcessingTypeViewer,
                                  domainNotificationService) {
  var vm = this,
      panel = new Panel('study.panel.processingTypes',
                        'home.admin.studies.study.processing.processingTypeAdd');

  vm.update               = update;
  vm.remove               = remove;
  vm.add                  = add;
  vm.information          = information;
  vm.panelOpen            = panel.getPanelOpenState();
  vm.modificationsAllowed = vm.study.isDisabled();

  $scope.$watch(angular.bind(vm, function() { return vm.panelOpen; }),
                angular.bind(panel, panel.watchPanelOpenChangeFunc));

  init();

  //--

  function init() {
    // updates the selected tab in 'studyViewDirective' which is the parent directive
    $scope.$emit('tabbed-page-update', 'tab-selected');
  }

  function add() {
    return panel.add();
  }

  /*
   * Displays a processing type in a modal.
   */
  function information(processingType) {
    return new ProcessingTypeViewer(processingType);
  }

  /*
   * Switches state to update a processing type.
   */
  function update(processingType) {
    if (!vm.study.isDisabled()) {
      throw new Error('study is not disabled');
    }
    $state.go(
      'home.admin.studies.study.processing.processingTypeUpdate',
      { processingTypeId: processingType.id });
  }

  function remove(processingType) {
    if (!vm.study.isDisabled()) {
      throw new Error('study is not disabled');
    }
    domainNotificationService.removeEntity(
      callback,
      'Remove Processing Type',
      'Are you sure you want to remove processing type ' + processingType.name + '?',
      'Remove Failed',
      'Processing type ' + processingType.name + ' cannot be removed: '
    ).then(function () {
      vm.processingTypes = _.without(vm.processingTypes, processingType);
    });

    function callback() {
      return processingType.remove();
    }

  }

}

// TEMP: don't add this controller for now
export default () => {}
