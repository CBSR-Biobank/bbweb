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
   * A panel to display a study's collection event types.
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
     * Links the collection event with the specimen groups that they use.
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

    /**
     * Display a collection event type in a modal.
     */
    function information(ceventType) {
      ceventTypeModalService.show(ceventType, vm.specimenGroups, vm.annotTypes);
    }

    /**
     * Displays a specimen group in a modal.
     */
    function showSpecimenGroup(specimenGroupId) {
      specimenGroupModalService.show(vm.specimenGroupsById[specimenGroupId]);
    }

    /**
     * Switches to the state to update a collection event type.
     */
    function update(ceventType) {
      $state.go(
        'admin.studies.study.collection.ceventTypeUpdate',
        { ceventTypeId: ceventType.id });
    }
  }

});
