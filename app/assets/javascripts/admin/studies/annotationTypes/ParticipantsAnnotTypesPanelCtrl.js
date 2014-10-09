define(['../../module'], function(module) {
  'use strict';

  module.controller('ParticipantsAnnotTypesPanelCtrl', ParticipantsAnnotTypesPanelCtrl);

  ParticipantsAnnotTypesPanelCtrl.$inject = [
    '$scope',
    '$state',
    '$stateParams',
    'ParticipantAnnotTypeService',
    'participantAnnotTypeRemoveService',
    'annotTypeModalService',
    'panelService'
  ];

  /**
   * A panel to display a study's participant annotation types.
   */
  function ParticipantsAnnotTypesPanelCtrl($scope,
                                           $state,
                                           $stateParams,
                                           ParticipantAnnotTypeService,
                                           participantAnnotTypeRemoveService,
                                           annotTypeModalService,
                                           panelService) {
    var vm = this;

    var helper = panelService.panel(
      'study.panel.participantAnnotationTypes',
      'admin.studies.study.participants.annotTypeAdd',
      annotTypeModalService,
      'Participant Annotation Type');

    vm.annotTypes  = $scope.annotTypes;
    vm.hasRequired = true;
    vm.update      = update;
    vm.remove      = participantAnnotTypeRemoveService.remove;
    vm.information = helper.information;
    vm.add         = helper.add;
    vm.panelOpen   = helper.panelOpen;
    vm.panelToggle = helper.panelToggle;

    vm.columns = [
      { title: 'Name', field: 'name', filter: { 'name': 'text' } },
      { title: 'Type', field: 'valueType', filter: { 'valueType': 'text' } },
      { title: 'Required', field: 'required', filter: { 'required': 'text' } },
      { title: 'Description', field: 'description', filter: { 'description': 'text' } }
    ];

    vm.tableParams = helper.getTableParams(vm.annotTypes);
    vm.tableParams.settings().$scope = $scope;  // kludge: see https://github.com/esvit/ng-table/issues/297#issuecomment-55756473

    //--

    /**
     * Switches state to update a participant annotation type.
     */
    function update(annotType) {
      $state.go(
      'admin.studies.study.participants.annotTypeUpdate',
        { annotTypeId: annotType.id });
    }

  }

});
