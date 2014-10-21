define(['../../module', 'underscore'], function(module, _) {
  'use strict';

  module.controller('SpcLinkAnnotTypesPanelCtrl', SpcLinkAnnotTypesPanelCtrl);

  SpcLinkAnnotTypesPanelCtrl.$inject = [
    '$scope',
    '$state',
    '$stateParams',
    'modalService',
    'SpcLinkAnnotTypeService',
    'spcLinkAnnotTypeRemoveService',
    'annotTypeModalService',
    'panelService'
  ];

  /**
   * A panel to display a study's specimen link annotation types.
   */
  function SpcLinkAnnotTypesPanelCtrl($scope,
                                      $state,
                                      $stateParams,
                                      modalService,
                                      SpcLinkAnnotTypeService,
                                      spcLinkAnnotTypeRemoveService,
                                      annotTypeModalService,
                                      panelService) {
    var vm = this;

    var helper = panelService.panel(
      'study.panel.specimenLinkAnnotationTypes',
      'admin.studies.study.processing.spcLinkAnnotTypeAdd',
      annotTypeModalService,
      'Specimen Link Annotation Type');

    vm.annotTypes   = $scope.annotTypes;
    vm.spcLinkTypes = $scope.spcLinkTypes;
    vm.update       = update;
    vm.remove       = remove;
    vm.information  = helper.information;
    vm.add          = helper.add;
    vm.panelOpen    = helper.panelOpen;
    vm.panelToggle  = helper.panelToggle;

    vm.columns = [
      { title: 'Name', field: 'name', filter: { 'name': 'text' } },
      { title: 'Type', field: 'valueType', filter: { 'valueType': 'text' } },
      { title: 'Description', field: 'description', filter: { 'description': 'text' } }
    ];

    vm.tableParams = helper.getTableParams(vm.annotTypes);
    vm.tableParams.settings().$scope = $scope;  // kludge: see https://github.com/esvit/ng-table/issues/297#issuecomment-55756473

    vm.annotTypesInUse = annotTypesInUse();

    //--

    /**
     * Returns the annotation types that are in use.
     */
    function annotTypesInUse() {
      var result = [];
      _.each(vm.spcLinkTypes, function(cet) {
        _.each(cet.annotationTypeData, function (atItem) {
          result.push(atItem.annotationTypeId);
        });
      });
      return result;
    }

    function annotTypeInUseModal() {
      var headerHtml = 'Cannot update this annotation type';
      var bodyHtml = 'This annotation type is in use by a specimen link type. ' +
          'If you want to make changes to the annotation type, ' +
          'it must first be removed from the specimen link type(s) that use it.';
      return modalService.modalOk(headerHtml, bodyHtml);
    }

    /**
     * Switches state to update a specimen link annotation type.
     */
    function update(annotType) {
      if (_.contains(vm.annotTypesInUse, annotType.id)) {
        annotTypeInUseModal();
      } else {
        $state.go(
          'admin.studies.study.processing.spcLinkAnnotTypeUpdate',
          { annotTypeId: annotType.id });
      }
    }

    function remove(annotType) {
      spcLinkAnnotTypeRemoveService.remove(annotType, vm.annotTypesInUse);
    }

  }

});
