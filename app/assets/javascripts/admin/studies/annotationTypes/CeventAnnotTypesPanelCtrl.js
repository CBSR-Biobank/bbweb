define(['../../module'], function(module) {
  'use strict';

  module.controller('CeventAnnotTypesPanelCtrl', CeventAnnotTypesPanelCtrl);

  CeventAnnotTypesPanelCtrl.$inject = [
    '$scope',
    '$state',
    '$stateParams',
    'CeventAnnotTypeService',
    'ceventAnnotTypeRemoveService',
    'annotTypeModalService',
    'panelService'
  ];

  /**
   *
   */
  function CeventAnnotTypesPanelCtrl($scope,
                                     $state,
                                     $stateParams,
                                     CeventAnnotTypeService,
                                     ceventAnnotTypeRemoveService,
                                     annotTypeModalService,
                                     panelService) {
    var vm = this;

    var helper = panelService.panel(
      'study.panel.participantAnnottionTypes',
      'admin.studies.study.collection.annotTypeAdd',
      annotTypeModalService,
      'Collection Event Annotation Type');

    vm.annotTypes  = $scope.annotTypes;
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

    //--

    function update(annotType) {
      $state.go(
        'admin.studies.study.collection.annotTypeUpdate',
        { annotTypeId: annotType.id });
    }

  }

});
