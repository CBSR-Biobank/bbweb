define(['../../module', 'underscore'], function(module, _) {
  'use strict';

  module.controller('SpcLinkTypesPanelCtrl', SpcLinkTypesPanelCtrl);

  SpcLinkTypesPanelCtrl.$inject = [
    '$scope',
    '$state',
    '$stateParams',
    'modalService',
    'panelService',
    'studiesService',
    'spcLinkTypeModalService',
    'spcLinkTypeRemoveService',
    'processingTypeModalService',
    'specimenGroupModalService',
    'annotTypeModalService'
  ];

  /**
   * A panel that displays a study's specimen link types.
   */
  function SpcLinkTypesPanelCtrl($scope,
                                 $state,
                                 $stateParams,
                                 modalService,
                                 panelService,
                                 studiesService,
                                 spcLinkTypeModalService,
                                 spcLinkTypeRemoveService,
                                 processingTypeModalService,
                                 specimenGroupModalService,
                                 annotTypeModalService) {
    var vm = this;

    var helper = panelService.panel('study.panel.specimenLinkTypes');

    vm.study               = $scope.study;
    vm.tableData           = [];
    vm.update              = update;
    vm.remove              = spcLinkTypeRemoveService.remove;
    vm.add                 = add;
    vm.information         = information;
    vm.panelOpen           = helper.panelOpen;
    vm.panelToggle         = helper.panelToggle;

    vm.processingTypesById = _.indexBy($scope.processingDto.processingTypes, 'id');
    vm.specimenGroupsById  = _.indexBy($scope.processingDto.specimenGroups, 'id');
    vm.annotTypesById      = _.indexBy($scope.processingDto.specimenLinkAnnotationTypes, 'id');

    vm.showProcessingType  = showProcessingType;
    vm.showSpecimenGroup   = showSpecimenGroup;
    vm.showAnnotationType  = showAnnotationType;

    vm.modificationsAllowed = vm.study.status === 'Disabled';

    init();

    vm.tableParams = helper.getTableParams(vm.tableData);

    //--

    function init() {
      _.each($scope.processingDto.specimenLinkTypes, function (slt) {
        var annotationTypes = [];
        _.each(slt.annotationTypeData, function (annotTypeItem) {
          var at = vm.annotTypesById[annotTypeItem.annotationTypeId];
          annotationTypes.push({id: annotTypeItem.annotationTypeId, name: at.name });
        });

        vm.tableData.push({
          specimenLinkType:   slt,
          processingTypeName: vm.processingTypesById[slt.processingTypeId].name,
          inputGroupName:     vm.specimenGroupsById[slt.inputGroupId].name,
          outputGroupName:    vm.specimenGroupsById[slt.outputGroupId].name,
          annotationTypes:    annotationTypes
        });
      });
    }

    /**
     * Displays a specimen link type in a modal.
     */
    function information(spcLinkType) {
      spcLinkTypeModalService.show(
        spcLinkType, vm.processingTypesById, vm.specimenGroupsById, vm.annotTypesById);
    }

    /**
     * Switches state to add a specimen link type.
     */
    function add() {
      if ($scope.processingDto.specimenGroups.length <= 0) {
        var headerHtml = 'Cannot add specimen link type';
        var bodyHtml = 'No <em>specimen groups</em> have been added to this study yet. ' +
            'Please add specimen groups first.';
        return modalService.modalOk(headerHtml, bodyHtml);
      } else {
        return $state.go('admin.studies.study.processing.spcLinkTypeAdd');
      }
    }

    /**
     * Switches state to update a specimen link type.
     */
    function update(spcLinkType) {
      $state.go(
        'admin.studies.study.processing.spcLinkTypeUpdate',
        { procTypeId:spcLinkType.processingTypeId, spcLinkTypeId: spcLinkType.id });
    }

    /**
     * Displays a processing type in a modal.
     */
    function showProcessingType(processingTypeId) {
      processingTypeModalService.show(vm.processingTypesById[processingTypeId]);
    }

    /**
     * Displays a specimen group in a modal.
     */
    function showSpecimenGroup(specimenGroupId) {
      specimenGroupModalService.show(vm.specimenGroupsById[specimenGroupId]);
    }

    /**
     * Display a specimen link annotation type in a modal.
     */
    function showAnnotationType(annotTypeId) {
      annotTypeModalService.show('Specimen Link Annotation Type', vm.annotTypesById[annotTypeId]);
    }

  }

});
