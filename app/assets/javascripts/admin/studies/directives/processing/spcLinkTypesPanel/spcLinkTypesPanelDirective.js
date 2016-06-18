/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(['angular', 'lodash'], function(angular, _) {
  'use strict';

  /**
   *
   */
  function spcLinkTypesPanelDirective() {
    var directive = {
      restrict: 'E',
      scope: {},
      bindToController: {
        study: '=',
        processingDto: '='
      },
      templateUrl: '/assets/javascripts/admin/studies/directives/processing/spcLinkTypesPanel/spcLinkTypesPanel.html',
      controller: SpcLinkTypesPanelCtrl,
      controllerAs: 'vm'
    };

    return directive;
  }

  SpcLinkTypesPanelCtrl.$inject = [
    '$scope',
    '$state',
    'modalService',
    'domainEntityService',
    'Panel',
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
                                 domainEntityService,
                                 Panel,
                                 SpcLinkTypeViewer,
                                 ProcessingTypeViewer,
                                 SpecimenGroupViewer,
                                 AnnotationTypeViewer) {
    var vm = this,
        panel = new Panel('study.panel.specimenLinkTypes');

    vm.specimenLinkTypes    = vm.processingDto.specimenLinkTypes;
    vm.processingTypesById  = _.keyBy(vm.processingDto.processingTypes, 'id');
    vm.specimenGroupsById   = _.keyBy(vm.processingDto.specimenGroups, 'id');
    vm.annotationTypesById  = _.keyBy(vm.processingDto.specimenLinkAnnotationTypes, 'id');

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
      if (vm.processingDto.specimenGroups.length <= 0) {
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
        promiseFunc,
        'Remove Specimen Link Type',
        'Are you sure you want to remove this specimen link type?',
        'Remove Failed',
        'specimen link type ' + slt.name + ' cannot be removed: '
      ).then(function () {
        vm.specimenLinkTypes = _.without(vm.specimenLinkTypes, slt);
      });

      function promiseFunc() {
        return slt.remove();
      }
    }

  }

  return spcLinkTypesPanelDirective;
});
