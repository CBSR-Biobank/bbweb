define(['angular'], function(angular) {
  'use strict';

  /**
   *
   */
  function processingTypesPanelDirective() {
    return {
      require: '^tab',
      restrict: 'E',
      scope: {
        study: '=',
        processingDto: '='
      },
      templateUrl: '/assets/javascripts/admin/studies/processing/processingTypesPanel.html',
      controller: 'ProcessingTypesPanelCtrl as vm'
    };
  }

  ProcessingTypesPanelCtrl.$inject = [
    '$scope',
    '$state',
    'Panel',
    'processingTypesService',
    'ProcessingTypeViewer',
    'processingTypeRemoveService'
  ];

  /**
   * A panel to display a study's processing types.
   */
  function ProcessingTypesPanelCtrl($scope,
                                    $state,
                                    Panel,
                                    processingTypesService,
                                    ProcessingTypeViewer,
                                    processingTypeRemoveService) {
    var vm = this;

    var panel = new Panel('study.panel.processingTypes',
                           'home.admin.studies.study.processing.processingTypeAdd');

    vm.study            = $scope.study;
    vm.processingTypes  = $scope.processingDto.processingTypes;
    vm.update           = update;
    vm.remove           = processingTypeRemoveService.remove;
    vm.add              = add;
    vm.information      = information;
    vm.panelOpen   = panel.getPanelOpenState();

    vm.modificationsAllowed = vm.study.status === 'Disabled';
    vm.tableParams = panel.getTableParams(vm.processingTypes);

    $scope.$watch(angular.bind(vm, function() { return vm.panelOpen; }),
                  angular.bind(panel, panel.watchPanelOpenChangeFunc));

    //--

    function add() {
      return panel.add();
    }

    /**
     * Displays a processing type in a modal.
     */
    function information(processingType) {
      return new ProcessingTypeViewer(processingType);
    }

    /**
     * Switches state to update a processing type.
     */
    function update(processingType) {
      $state.go(
        'home.admin.studies.study.processing.processingTypeUpdate',
        { processingTypeId: processingType.id });
    }

  }

  return {
    directive: processingTypesPanelDirective,
    controller: ProcessingTypesPanelCtrl
  };
});
