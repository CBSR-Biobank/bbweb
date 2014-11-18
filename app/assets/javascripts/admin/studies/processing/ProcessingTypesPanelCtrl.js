define(['../../module'], function(module) {
  'use strict';

  module.controller('ProcessingTypesPanelCtrl', ProcessingTypesPanelCtrl);

  ProcessingTypesPanelCtrl.$inject = [
    '$scope',
    '$state',
    '$stateParams',
    'panelService',
    'processingTypesService',
    'processingTypeModalService',
    'processingTypeRemoveService'
  ];

  /**
   * A panel to display a study's processing types.
   */
  function ProcessingTypesPanelCtrl($scope,
                                    $state,
                                    $stateParams,
                                    panelService,
                                    processingTypesService,
                                    processingTypeModalService,
                                    processingTypeRemoveService) {
    var vm = this;

    var helper = panelService.panel(
      'study.panel.processingTypes',
      'admin.studies.study.processing.processingTypeAdd');

    vm.study            = $scope.study;
    vm.processingTypes  = $scope.processingDto.processingTypes;
    vm.update           = update;
    vm.remove           = processingTypeRemoveService.remove;
    vm.add              = helper.add;
    vm.addButtonEnabled = vm.study.status === 'Disabled';
    vm.information      = information;
    vm.panelOpen        = helper.panelOpen;
    vm.panelToggle      = helper.panelToggle;

    vm.tableParams = helper.getTableParams(vm.processingTypes);

    //--

    /**
     * Displays a processing type in a modal.
     */
    function information(processingType) {
      processingTypeModalService.show(processingType);
    }

    /**
     * Switches state to update a processing type.
     */
    function update(processingType) {
      $state.go(
        'admin.studies.study.processing.processingTypeUpdate',
        { processingTypeId: processingType.id });
    }

  }

});
