define(['../../module', 'underscore'], function(module, _) {
  'use strict';

  module.directive('specimenGroupsPanel', specimenGroupsPanel);

  /**
   *
   */
  function specimenGroupsPanel() {
    var directive = {
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
    return directive;
  }

  module.controller('SpecimenGroupsPanelCtrl', SpecimenGroupsPanelCtrl);

  SpecimenGroupsPanelCtrl.$inject = [
    '$scope',
    '$state',
    '$stateParams',
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
                                   $stateParams,
                                   Panel,
                                   modalService,
                                   specimenGroupsService,
                                   SpecimenGroupViewer,
                                   specimenGroupRemoveService) {
    var vm = this;

    var helper = new Panel('study.panel.specimenGroups',
                           'home.admin.studies.study.specimens.groupAdd');

    vm.study                 = $scope.study;
    vm.specimenGroups        = $scope.specimenGroups;
    vm.specimenGroupIdsInUse = $scope.specimenGroupIdsInUse;
    vm.update                = update;
    vm.remove                = remove;
    vm.add                   = add;
    vm.information           = information;
    vm.panelOpen             = helper.panelOpen;
    vm.panelToggle           = panelToggle;

    vm.modificationsAllowed = vm.study.status === 'Disabled';

    vm.tableParams = helper.getTableParams(vm.specimenGroups);

    //--

    function add() {
      return helper.add;
    }

    function panelToggle() {
      return helper.panelToggle();
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

});
