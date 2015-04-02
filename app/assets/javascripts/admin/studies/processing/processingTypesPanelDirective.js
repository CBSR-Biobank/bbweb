define(['angular', 'underscore'], function(angular, _) {
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
    'tableService',
    'ProcessingTypeViewer',
    'domainEntityService'
  ];

  /**
   * A panel to display a study's processing types.
   */
  function ProcessingTypesPanelCtrl($scope,
                                    $state,
                                    Panel,
                                    tableService,
                                    ProcessingTypeViewer,
                                    domainEntityService) {
    var vm = this,
        panel = new Panel('study.panel.processingTypes',
                          'home.admin.studies.study.processing.processingTypeAdd');

    vm.study                = $scope.study;
    vm.processingTypes      = $scope.processingDto.processingTypes;
    vm.update               = update;
    vm.remove               = remove;
    vm.add                  = add;
    vm.information          = information;
    vm.panelOpen            = panel.getPanelOpenState();
    vm.modificationsAllowed = vm.study.isDisabled();
    vm.tableParams          = tableService.getTableParamsWithCallback(getTableData,
                                                                       {},
                                                                       { counts: [] });

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
      domainEntityService.removeEntity(
        processingType.remove,
        'Remove Processing Type',
        'Are you sure you want to remove processing type ' + processingType.name + '?',
        'Remove Failed',
        'Processing type ' + processingType.name + ' cannot be removed: '
      ).then(function () {
        vm.processingTypes = _.without(vm.processingTypes, processingType);
        vm.tableParams.reload();
      });

    }

    function getTableData() {
      return vm.processingTypes;
    }
  }

  return {
    directive: processingTypesPanelDirective,
    controller: ProcessingTypesPanelCtrl
  };
});
