define(['../../module', 'underscore'], function(module, _) {
  'use strict';

  module.controller('CeventAnnotTypesPanelCtrl', CeventAnnotTypesPanelCtrl);

  CeventAnnotTypesPanelCtrl.$inject = [
    '$scope',
    '$state',
    'modalService',
    'ceventAnnotTypesService',
    'annotationTypeRemoveService',
    'annotTypeModalService',
    'panelService'
  ];

  /**
   * A panel to display a study's collection event annotation types.
   */
  function CeventAnnotTypesPanelCtrl($scope,
                                     $state,
                                     modalService,
                                     ceventAnnotTypesService,
                                     annotationTypeRemoveService,
                                     annotTypeModalService,
                                     panelService) {
    var vm = this;

    var helper = panelService.panel(
      'study.panel.participantAnnottionTypes',
      'admin.studies.study.collection.ceventAnnotTypeAdd',
      annotTypeModalService,
      'Collection Event Annotation Type');

    vm.annotTypes  = $scope.annotTypes;
    vm.ceventTypes = $scope.ceventTypes;
    vm.update      = update;
    vm.remove      = remove;
    vm.information = helper.information;
    vm.add         = helper.add;
    vm.panelOpen   = helper.panelOpen;
    vm.panelToggle = helper.panelToggle;

    vm.columns = [
      { title: 'Name', field: 'name', filter: { 'name': 'text' } },
      { title: 'Type', field: 'valueType', filter: { 'valueType': 'text' } },
      { title: 'Description', field: 'description', filter: { 'description': 'text' } }
    ];

    vm.tableParams = helper.getTableParams(vm.annotTypes);
    vm.annotTypesInUse = annotTypesInUse();

    //--

    /**
     * Returns the annotation types that are in use.
     */
    function annotTypesInUse() {
      var result = [];
      _.each(vm.ceventTypes, function(cet) {
        _.each(cet.annotationTypeData, function (atItem) {
          result.push(atItem.annotationTypeId);
        });
      });
      return result;
    }

    function annotTypeInUseModal() {
      var headerHtml = 'Cannot update this annotation type';
      var bodyHtml = 'This annotation type is in use by a collection event type. ' +
          'If you want to make changes to the annotation type, ' +
          'it must first be removed from the collection event type(s) that use it.';
      return modalService.modalOk(headerHtml, bodyHtml);
    }

    /**
     * Switches state to update a collection event annotation type.
     */
    function update(annotType) {
      if (_.contains(vm.annotTypesInUse, annotType.id)) {
        annotTypeInUseModal();
      } else {
        $state.go(
          'admin.studies.study.collection.ceventAnnotTypeUpdate',
          { annotTypeId: annotType.id });
      }
    }

    function remove(annotType) {
      if (_.contains(vm.annotTypesInUse, annotType.id)) {
        var headerHtml = 'Cannot remove this annotation type';
        var bodyHtml = 'This annotation type is in use by a collection event type. ' +
            'If you want to remove the annotation type, ' +
            'it must first be removed from the collection event type(s) that use it.';
        modalService.modalOk(headerHtml, bodyHtml);
      } else {
        annotationTypeRemoveService.remove(
          ceventAnnotTypesService.remove,
          annotType,
          'admin.studies.study.collection',
          {studyId: annotType.studyId});
      }
    }

  }

});
