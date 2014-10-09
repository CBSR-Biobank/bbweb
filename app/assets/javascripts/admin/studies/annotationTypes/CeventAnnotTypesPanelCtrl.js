define(['../../module', 'underscore'], function(module, _) {
  'use strict';

  module.controller('CeventAnnotTypesPanelCtrl', CeventAnnotTypesPanelCtrl);

  CeventAnnotTypesPanelCtrl.$inject = [
    '$scope',
    '$state',
    '$stateParams',
    'modalService',
    'CeventAnnotTypeService',
    'ceventAnnotTypeRemoveService',
    'annotTypeModalService',
    'panelService'
  ];

  /**
   * A panel to display a study's collection event annotation types.
   */
  function CeventAnnotTypesPanelCtrl($scope,
                                     $state,
                                     $stateParams,
                                     modalService,
                                     CeventAnnotTypeService,
                                     ceventAnnotTypeRemoveService,
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
    vm.remove      = ceventAnnotTypeRemoveService.remove;
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
    vm.tableParams.settings().$scope = $scope;  // kludge: see https://github.com/esvit/ng-table/issues/297#issuecomment-55756473

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
      var modalDefaults = {
        templateUrl: '/assets/javascripts/common/modalOk.html'
      };
      var modalOptions = {
        headerText: 'Cannot update this annotation type',
        bodyText: 'This annotation type is in use by a collection event type. ' +
          'If you want to make changes to the annotation type, ' +
          'it must first be removed from the collection even type(s) that use it.'
      };
      return modalService.showModal(modalDefaults, modalOptions);
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

  }

});
