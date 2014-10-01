define(['../../module', 'underscore'], function(module, _) {
  'use strict';

  module.controller('CeventTypesPanelCtrl', CeventTypesPanelCtrl);

  CeventTypesPanelCtrl.$inject = [
    '$scope',
    '$state',
    '$stateParams',
    'panelService',
    'CeventTypeService',
    'SpecimenGroupService',
    'CeventAnnotTypeService',
    'ceventTypeModalService',
    'specimenGroupModalService',
    'ceventTypeRemoveService'
  ];

  /**
   *
   */
  function CeventTypesPanelCtrl($scope,
                                $state,
                                $stateParams,
                                panelService,
                                CeventTypeService,
                                SpecimenGroupService,
                                CeventAnnotTypeService,
                                ceventTypeModalService,
                                specimenGroupModalService,
                                ceventTypeRemoveService) {
    var vm = this;

    var helper = panelService.panel(
      'study.panel.collectionEventTypes',
      'admin.studies.study.collection.ceventTypeAdd');

    vm.ceventTypes        = $scope.ceventTypes;
    vm.annotTypes         = $scope.annotTypes;
    vm.specimenGroups     = $scope.specimenGroups;
    vm.specimenGroupsById = [];

    vm.update            = update;
    vm.remove            = ceventTypeRemoveService.remove;
    vm.add               = helper.add;
    vm.information       = information;
    vm.showSpecimenGroup = showSpecimenGroup;
    vm.panelOpen         = helper.panelOpen;
    vm.panelToggle       = helper.panelToggle;

    init();

    vm.tableParams = helper.getTableParams(vm.ceventTypes);
    vm.tableParams.settings().$scope = $scope;  // kludge: see https://github.com/esvit/ng-table/issues/297#issuecomment-55756473

    //--

    /**
     * Gets all the collection event types for the study, and then all the specimen groups.
     */
    function init() {
      vm.specimenGroupsById = _.indexBy(vm.specimenGroups, 'id');

      _.each(vm.ceventTypes, function (cet) {
        cet.specimenGroups = [];
        cet.specimenGroupData.forEach(function (sgItem) {
          var sg = vm.specimenGroupsById[sgItem.specimenGroupId];
          cet.specimenGroups.push({ id: sgItem.specimenGroupId, name: sg.name });
        });
      });
    }

    function information(ceventType) {
      ceventTypeModalService.show(ceventType, vm.specimenGroups, vm.annotTypes);
    }

    function update(ceventType) {
      $state.go(
        'admin.studies.study.collection.ceventTypeUpdate',
        { ceventTypeId: ceventType.id });
    }

    function showSpecimenGroup(specimenGroupId) {
      specimenGroupModalService.show(vm.specimenGroupsById[specimenGroupId]);
    }
  }

});
