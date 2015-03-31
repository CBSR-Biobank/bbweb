define(['angular', 'underscore'], function(angular, _) {
  'use strict';

  /**
   *
   */
  function spcLinkTypesPanelDirective() {
    return {
      require: '^tab',
      restrict: 'E',
      scope: {
        study: '=',
        processingDto: '='
      },
      templateUrl: '/assets/javascripts/admin/studies/processing/spcLinkTypesPanel.html',
      controller: 'SpcLinkTypesPanelCtrl as vm'
    };
  }

  SpcLinkTypesPanelCtrl.$inject = [
    '$scope',
    '$state',
    'modalService',
    'tableService',
    'SpecimenLinkType',
    'Panel',
    'SpecimenLinkAnnotationType',
    'SpcLinkTypeViewer',
    'specimenLinkTypeUtils',
    'ProcessingTypeViewer',
    'SpecimenGroupViewer',
    'AnnotationTypeViewer'
  ];

  /**
   * A panel that displays a study's specimen link types.
   */
  function SpcLinkTypesPanelCtrl($scope,
                                 $state,
                                 modalService,
                                 tableService,
                                 SpecimenLinkType,
                                 Panel,
                                 SpecimenLinkAnnotationType,
                                 SpcLinkTypeViewer,
                                 specimenLinkTypeUtils,
                                 ProcessingTypeViewer,
                                 SpecimenGroupViewer,
                                 AnnotationTypeViewer) {
    var vm = this,
        panel = new Panel('study.panel.specimenLinkTypes');

    vm.study               = $scope.study;
    vm.tableData           = [];
    vm.update              = update;
    vm.remove              = remove;
    vm.add                 = add;
    vm.information         = information;
    vm.panelOpen           = panel.getPanelOpenState();

    vm.processingTypes     = _.indexBy($scope.processingDto.processingTypes, 'id');
    vm.specimenGroups      = _.indexBy($scope.processingDto.specimenGroups, 'id');
    vm.annotationTypes     = _.indexBy($scope.processingDto.specimenLinkAnnotationTypes, 'id');

    vm.showProcessingType  = showProcessingType;
    vm.showSpecimenGroup   = showSpecimenGroup;
    vm.showAnnotationType  = showAnnotationType;

    vm.modificationsAllowed = vm.study.isDisabled();

    vm.specimenLinkTypes = _.map($scope.processingDto.specimenLinkTypes, function (slt) {
      return new SpecimenLinkType(
        slt, {
          studySpecimenGroups:  $scope.processingDto.specimenGroups,
          studyAnnotationTypes: $scope.processingDto.specimenLinkAnnotationTypes
        });
    });

    vm.tableParams = tableService.getTableParamsWithCallback(getTableData,
                                                             {},
                                                             { counts: [] });

    $scope.$watch(angular.bind(vm, function() { return vm.panelOpen; }),
                  angular.bind(panel, panel.watchPanelOpenChangeFunc));

    /**
     * Displays a specimen link type in a modal.
     */
    function information(spcLinkType) {
      return new SpcLinkTypeViewer(spcLinkType,
                                   vm.processingTypes[spcLinkType.processingTypeId]);
    }

    function add() {
      if ($scope.processingDto.specimenGroups.length <= 0) {
        var headerHtml = 'Cannot add a specimen link type';
        var bodyHtml = 'No <em>specimen groups</em> have been added to this study yet. ' +
            'Please add specimen groups first and then add a specimen link type.';
        return modalService.modalOk(headerHtml, bodyHtml);
      } else {
        return $state.go('home.admin.studies.study.processing.spcLinkTypeAdd');
      }
    }

    /**
     * Displays a processing type in a modal.
     */
    function showProcessingType(processingTypeId) {
      return new ProcessingTypeViewer(vm.processingTypeSet.get(processingTypeId));
    }

    /**
     * Displays a specimen group in a modal.
     */
    function showSpecimenGroup(specimenGroupId) {
      return new SpecimenGroupViewer(vm.specimenGroups[specimenGroupId]);
    }

    /**
     * Display a specimen link annotation type in a modal.
     */
    function showAnnotationType(annotTypeId) {
      return new AnnotationTypeViewer(vm.annotationTypes[annotTypeId], 'Specimen Link Annotation Type');
    }

    /**
     * Switches state to update a specimen link type.
     */
    function update(spcLinkType) {
      if (!vm.study.isDisabled()) {
        throw new Error('study is not disabled');
      }
      $state.go(
        'home.admin.studies.study.processing.spcLinkTypeUpdate',
        { procTypeId:spcLinkType.processingTypeId, spcLinkTypeId: spcLinkType.id });
    }

    function remove(slt) {
      if (!vm.study.isDisabled()) {
        throw new Error('study is not disabled');
      }
      specimenLinkTypeUtils.remove(slt)
        .then(function () {
          vm.specimenLinkTypes = _.without(vm.specimenLinkTypes, slt);
          vm.tableParams.reload();
        });
    }

    function getTableData() {
      return vm.specimenLinkTypes;
    }

  }

  return {
    directive: spcLinkTypesPanelDirective,
    controller: SpcLinkTypesPanelCtrl
  };
});
