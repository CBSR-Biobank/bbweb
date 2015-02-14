define(['../../module', 'underscore'], function(module, _) {
  'use strict';

  module.directive('spcLinkTypesPanel', spcLinkTypesPanel);

  /**
   *
   */
  function spcLinkTypesPanel() {
    var directive = {
      require: '^tab',
      restrict: 'E',
      scope: {
        study: '=',
        processingDto: '='
      },
      templateUrl: '/assets/javascripts/admin/studies/processing/spcLinkTypesPanel.html',
      controller: 'SpcLinkTypesPanelCtrl as vm'
    };
    return directive;
  }

  module.controller('SpcLinkTypesPanelCtrl', SpcLinkTypesPanelCtrl);

  SpcLinkTypesPanelCtrl.$inject = [
    '$scope',
    '$state',
    'modalService',
    'SpecimenLinkType',
    'SpecimenGroupSet',
    'AnnotationTypeSet',
    'Panel',
    'SpcLinkTypeViewer',
    'spcLinkTypeRemoveService',
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
                                 SpecimenLinkType,
                                 SpecimenGroupSet,
                                 AnnotationTypeSet,
                                 Panel,
                                 SpcLinkTypeViewer,
                                 spcLinkTypeRemoveService,
                                 ProcessingTypeViewer,
                                 SpecimenGroupViewer,
                                 AnnotationTypeViewer) {
    var vm = this;

    var specimenGroupSet  = new SpecimenGroupSet($scope.processingDto.specimenGroups);
    var annotationTypeSet = new AnnotationTypeSet($scope.processingDto.specimenLinkAnnotationTypes);
    var panel = new Panel('study.panel.specimenLinkTypes');

    vm.study               = $scope.study;
    vm.tableData           = [];
    vm.update              = update;
    vm.remove              = spcLinkTypeRemoveService.remove;
    vm.add                 = add;
    vm.information         = information;
    vm.panelOpen           = panel.panelOpen;
    vm.panelToggle         = panelToggle;

    vm.processingTypesById = _.indexBy($scope.processingDto.processingTypes, 'id');
    vm.specimenGroupsById  = _.indexBy($scope.processingDto.specimenGroups, 'id');
    vm.annotTypesById      = _.indexBy($scope.processingDto.specimenLinkAnnotationTypes, 'id');

    vm.showProcessingType  = showProcessingType;
    vm.showSpecimenGroup   = showSpecimenGroup;
    vm.showAnnotationType  = showAnnotationType;

    vm.modificationsAllowed = vm.study.status === 'Disabled';

    vm.specimenLinkTypes = _.map($scope.processingDto.specimenLinkTypes, function (slt) {
      return new SpecimenLinkType(slt,
                                  vm.processingTypesById[slt.processingTypeId],
                                  specimenGroupSet,
                                  annotationTypeSet);
    });

    vm.tableParams = panel.getTableParams(vm.specimenLinkTypes);


    function panelToggle() {
      return panel.panelToggle();
    }

    /**
     * Displays a specimen link type in a modal.
     */
    function information(spcLinkType) {
      return new SpcLinkTypeViewer(spcLinkType);
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
     * Switches state to update a specimen link type.
     */
    function update(spcLinkType) {
      $state.go(
        'home.admin.studies.study.processing.spcLinkTypeUpdate',
        { procTypeId:spcLinkType.processingTypeId, spcLinkTypeId: spcLinkType.id });
    }

    /**
     * Displays a processing type in a modal.
     */
    function showProcessingType(processingTypeId) {
      return new ProcessingTypeViewer(vm.processingTypesById[processingTypeId]);
    }

    /**
     * Displays a specimen group in a modal.
     */
    function showSpecimenGroup(specimenGroupId) {
      return new SpecimenGroupViewer(vm.specimenGroupsById[specimenGroupId]);
    }

    /**
     * Display a specimen link annotation type in a modal.
     */
    function showAnnotationType(annotTypeId) {
      return new AnnotationTypeViewer(vm.annotTypesById[annotTypeId], 'Specimen Link Annotation Type');
    }
  }

});
