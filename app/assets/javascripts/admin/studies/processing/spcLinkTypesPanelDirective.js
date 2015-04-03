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
    'domainEntityService',
    'Panel',
    'SpecimenLinkType',
    'SpecimenLinkAnnotationType',
    'SpcLinkTypeViewer',
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
                                 domainEntityService,
                                 Panel,
                                 SpecimenLinkType,
                                 SpecimenLinkAnnotationType,
                                 SpcLinkTypeViewer,
                                 ProcessingTypeViewer,
                                 SpecimenGroupViewer,
                                 AnnotationTypeViewer) {
    var vm = this,
        panel = new Panel('study.panel.specimenLinkTypes');

    vm.study                = $scope.study;
    vm.specimenLinkTypes    = $scope.processingDto.specimenLinkTypes;
    vm.processingTypesById  = _.indexBy($scope.processingDto.processingTypes, 'id');
    vm.specimenGroupsById   = _.indexBy($scope.processingDto.specimenGroups, 'id');
    vm.annotationTypesById  = _.indexBy($scope.processingDto.specimenLinkAnnotationTypes, 'id');

    vm.viewProcessingType   = viewProcessingType;
    vm.viewSpecimenGroup    = viewSpecimenGroup;
    vm.viewAnnotationType   = viewAnnotationType;
    vm.update               = update;
    vm.remove               = remove;
    vm.add                  = add;
    vm.information          = information;

    vm.tableData            = [];
    vm.panelOpen            = panel.getPanelOpenState();
    vm.modificationsAllowed = vm.study.isDisabled();

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
                                   vm.processingTypesById[spcLinkType.processingTypeId]);
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
    function viewProcessingType(processingTypeId) {
      return new ProcessingTypeViewer(vm.processingTypesById[processingTypeId]);
    }

    /**
     * Displays a specimen group in a modal.
     */
    function viewSpecimenGroup(specimenGroupId) {
      return new SpecimenGroupViewer(vm.specimenGroupsById[specimenGroupId]);
    }

    /**
     * Display a specimen link annotation type in a modal.
     */
    function viewAnnotationType(annotationTypeId) {
      return new AnnotationTypeViewer(vm.annotationTypesById[annotationTypeId],
                                      'Specimen Link Annotation Type');
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
      domainEntityService.removeEntity(
        slt,
        'Remove Specimen Link Type',
        'Are you sure you want to remove this specimen link type?',
        'Remove Failed',
        'specimen link type ' + slt.name + ' cannot be removed: '
      ).then(function () {
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
