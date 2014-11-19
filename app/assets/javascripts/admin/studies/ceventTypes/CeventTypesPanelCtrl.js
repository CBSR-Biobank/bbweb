define(['../../module', 'underscore'], function(module, _) {
  'use strict';

  module.controller('CeventTypesPanelCtrl', CeventTypesPanelCtrl);

  CeventTypesPanelCtrl.$inject = [
    '$scope',
    '$state',
    '$stateParams',
    'panelService',
    'ceventTypesService',
    'specimenGroupsService',
    'ceventAnnotTypesService',
    'ceventTypeModalService',
    'annotTypeModalService',
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
                                ceventTypesService,
                                specimenGroupsService,
                                ceventAnnotTypesService,
                                ceventTypeModalService,
                                annotTypeModalService,
                                specimenGroupModalService,
                                ceventTypeRemoveService) {
    var vm = this;

    var helper = panelService.panel(
      'study.panel.collectionEventTypes',
      'admin.studies.study.collection.ceventTypeAdd');

    vm.study               = $scope.study;
    vm.ceventTypes         = $scope.ceventTypes;
    vm.annotTypes          = $scope.annotTypes;
    vm.specimenGroups      = $scope.specimenGroups;
    vm.annotationTypesById = [];
    vm.specimenGroupsById  = [];

    vm.update             = update;
    vm.remove             = ceventTypeRemoveService.remove;
    vm.add                = helper.add;
    vm.addButtonEnabled   = vm.study.status === 'Disabled';
    vm.information        = information;
    vm.showAnnotationType = showAnnotationType;
    vm.showSpecimenGroup  = showSpecimenGroup;
    vm.panelOpen          = helper.panelOpen;
    vm.panelToggle        = helper.panelToggle;

    init();

    vm.tableParams = helper.getTableParams(vm.ceventTypes);

    //--

    /**
     * Links the collection event with the specimen groups that they use.
     */
    function init() {
      vm.annotationTypesById = _.indexBy(vm.annotTypes, 'id');
      vm.specimenGroupsById = _.indexBy(vm.specimenGroups, 'id');

      _.each(vm.ceventTypes, function (cet) {
        cet.specimenGroups = [];
        _.each(cet.specimenGroupData, function (sgItem) {
          var sg = vm.specimenGroupsById[sgItem.specimenGroupId];
          cet.specimenGroups.push({ id: sgItem.specimenGroupId, name: sg.name });
        });

        cet.annotationTypes = [];
        _.each(cet.annotationTypeData, function (atItem) {
          var at = vm.annotationTypesById[atItem.annotationTypeId];
          cet.annotationTypes.push({ id: atItem.annotationTypeId, name: at.name });
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
     * Display a collection event annotation type in a modal.
     */
    function showAnnotationType(annotTypeId) {
      annotTypeModalService.show('Specimen Link Annotation Type', vm.annotationTypesById[annotTypeId]);
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
