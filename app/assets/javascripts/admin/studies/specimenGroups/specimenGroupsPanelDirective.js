define(['angular', 'underscore'], function(angular, _) {
  'use strict';

  /**
   *
   */
  function specimenGroupsPanelDirective() {
    return {
      require: '^tab',
      restrict: 'E',
      scope: {
        study: '=',
        specimenGroups: '=',
        specimenGroupIdsInUse: '='
      },
      templateUrl: '/assets/javascripts/admin/studies/specimenGroups/specimenGroupsPanel.html',
      controller: 'SpecimenGroupsPanelCtrl as vm'
    };
  }

  SpecimenGroupsPanelCtrl.$inject = [
    '$scope',
    '$state',
    'Panel',
    'modalService',
    'tableService',
    'domainEntityService',
    'SpecimenGroupViewer',
    'specimenGroupUtils'
  ];

  /**
   * A panel to display a study's specimen groups.
   */
  function SpecimenGroupsPanelCtrl($scope,
                                   $state,
                                   Panel,
                                   modalService,
                                   tableService,
                                   domainEntityService,
                                   SpecimenGroupViewer,
                                   specimenGroupUtils) {
    var vm = this,
        panel = new Panel('study.panel.specimenGroups',
                          'home.admin.studies.study.specimens.groupAdd');

    vm.study                 = $scope.study;
    vm.specimenGroups        = $scope.specimenGroups;
    vm.specimenGroupIdsInUse = $scope.specimenGroupIdsInUse;
    vm.update                = update;
    vm.remove                = remove;
    vm.add                   = add;
    vm.information           = information;
    vm.panelOpen             = panel.getPanelOpenState();
    vm.modificationsAllowed  = vm.study.isDisabled();
    vm.tableParams           = tableService.getTableParamsWithCallback(getTableData,
                                                                       {},
                                                                       { counts: [] });

    $scope.$watch(angular.bind(vm, function() { return vm.panelOpen; }),
                  angular.bind(panel, panel.watchPanelOpenChangeFunc));


    //--

    function add() {
      return panel.add();
    }

    /**
     * Displays a specimen group in a modal.
     */
    function information(specimenGroup) {
      return new SpecimenGroupViewer(specimenGroup);
    }

    /**
     * Switches state to updte a specimen group.
     */
    function update(specimenGroup) {
      if (!vm.study.isDisabled()) {
        throw new Error('study is not disabled');
      }

      if (_.contains(vm.specimenGroupIdsInUse, specimenGroup.id)) {
        specimenGroupUtils.inUseModal(specimenGroup, 'updated');
        return;
      }

      $state.go(
        'home.admin.studies.study.specimens.groupUpdate',
        { specimenGroupId: specimenGroup.id });
    }

    function remove(specimenGroup) {
      if (!vm.study.isDisabled()) {
        throw new Error('study is not disabled');
      }

      if (_.contains(vm.specimenGroupIdsInUse, specimenGroup.id)) {
        specimenGroupUtils.inUseModal(specimenGroup);
        return;
      }

      domainEntityService.removeEntity(
        specimenGroup,
        'Remove Specimen Group',
        'Are you sure you want to remove specimen group ' + specimenGroup.name + '?',
        'Remove Failed',
        'Specimen group ' + specimenGroup.name + ' cannot be removed: '
      ).then(function () {
        vm.specimenGroups = _.without(vm.specimenGroups, specimenGroup);
        vm.tableParams.reload();
      });
    }

    function getTableData() {
      return vm.specimenGroups;
    }
  }

  return {
    directive: specimenGroupsPanelDirective,
    controller: SpecimenGroupsPanelCtrl
  };
});
