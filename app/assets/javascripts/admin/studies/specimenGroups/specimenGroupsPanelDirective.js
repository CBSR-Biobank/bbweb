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
    'specimenGroupsService',
    'SpecimenGroupViewer',
    'specimenGroupRemoveService'
  ];

  /**
   * A panel to display a study's specimen groups.
   */
  function SpecimenGroupsPanelCtrl($scope,
                                   $state,
                                   Panel,
                                   modalService,
                                   specimenGroupsService,
                                   SpecimenGroupViewer,
                                   specimenGroupRemoveService) {
    var vm = this;

    var panel = new Panel('study.panel.specimenGroups',
                           'home.admin.studies.study.specimens.groupAdd');

    vm.study                 = $scope.study;
    vm.specimenGroups        = $scope.specimenGroups;
    vm.specimenGroupIdsInUse = $scope.specimenGroupIdsInUse;
    vm.update                = update;
    vm.remove                = remove;
    vm.add                   = add;
    vm.information           = information;
    vm.panelOpen             = panel.getPanelOpenState();

    $scope.$watch(angular.bind(vm, function() { return vm.panelOpen; }),
                  angular.bind(panel, panel.watchPanelOpenChangeFunc));

    vm.modificationsAllowed = vm.study.status === 'Disabled';

    vm.tableParams = panel.getTableParams(vm.specimenGroups);

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
      if (_.contains(vm.specimenGroupIdsInUse, specimenGroup.id)) {
        modalService.modalOk(
          'Specimen Group in use',
          'This specimen group cannot be modified because it is in use by either ' +
            'a collection event type or a specimen link type');
      } else {
        $state.go(
          'home.admin.studies.study.specimens.groupUpdate',
          { specimenGroupId: specimenGroup.id });
      }
    }

    function remove(specimenGroup) {
      specimenGroupRemoveService.remove(specimenGroup, vm.specimenGroupIdsInUse);
    }
  }

  return {
    directive: specimenGroupsPanelDirective,
    controller: SpecimenGroupsPanelCtrl
  };
});
