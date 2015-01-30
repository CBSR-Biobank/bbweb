define(['../../module', 'underscore'], function(module, _) {
  'use strict';

  module.directive('participantsAnnotTypesPanel', participantsAnnotTypesPanel);

  /**
   *
   */
  function participantsAnnotTypesPanel() {
    var directive = {
      require: '^tab',
      restrict: 'E',
      scope: {
        study: '=',
        annotTypes: '='
      },
      templateUrl: '/assets/javascripts/admin/studies/annotationTypes/participantAnnotTypesPanel.html',
      controller: 'ParticipantAnnotTypesPanelCtrl as vm'
    };
    return directive;
  }

  module.controller('ParticipantAnnotTypesPanelCtrl', ParticipantAnnotTypesPanelCtrl);

  ParticipantAnnotTypesPanelCtrl.$inject = [
    '$scope',
    '$state',
    '$stateParams',
    'modalService',
    'participantAnnotTypesService',
    'participantAnnotTypeRemoveService',
    'annotTypeModalService',
    'panelService'
  ];

  /**
   * A panel to display a study's participant annotation types.
   */
  function ParticipantAnnotTypesPanelCtrl($scope,
                                          $state,
                                          $stateParams,
                                          modalService,
                                          participantAnnotTypesService,
                                          participantAnnotTypeRemoveService,
                                          annotTypeModalService,
                                          panelService) {
    var vm = this;

    var helper = panelService.panel(
      'study.panel.participantAnnotationTypes',
      'admin.studies.study.participants.annotTypeAdd',
      annotTypeModalService,
      'Participant Annotation Type');

    vm.study            = $scope.study;
    vm.annotTypes       = $scope.annotTypes;
    vm.hasRequired      = true;
    vm.update           = update;
    vm.remove           = remove;
    vm.information      = helper.information;
    vm.add              = helper.add;
    vm.panelOpen        = helper.panelOpen;
    vm.panelToggle      = helper.panelToggle;

    vm.modificationsAllowed = vm.study.status === 'Disabled';

    vm.columns = [
      { title: 'Name', field: 'name', filter: { 'name': 'text' } },
      { title: 'Type', field: 'valueType', filter: { 'valueType': 'text' } },
      { title: 'Required', field: 'required', filter: { 'required': 'text' } },
      { title: 'Description', field: 'description', filter: { 'description': 'text' } }
    ];

    vm.tableParams = helper.getTableParams(vm.annotTypes);

    // FIXME this is set to empty array for now, but will have to call the correct method in the future
    vm.annotTypesInUse = [];

    //--

    function annotTypeInUseModal() {
      var headerHtml = 'Cannot update this annotation type';
      var bodyHtml = 'This annotation type is in use by participants. ' +
          'If you want to make changes to the annotation type, ' +
          'it must first be removed from the participants that use it.';
      return modalService.modalOk(headerHtml, bodyHtml);
    }

    /**
     * Switches state to update a participant annotation type.
     */
    function update(annotType) {
      if (vm.study.status !== 'Disabled') {
        throw new Error('study is not disabled');
      }

      if (_.contains(vm.annotTypesInUse, annotType.id)) {
        annotTypeInUseModal();
      } else {
        $state.go(
          'admin.studies.study.participants.annotTypeUpdate',
          { annotTypeId: annotType.id });
      }
    }

    function remove(annotType) {
      if (vm.study.status !== 'Disabled') {
        throw new Error('study is not disabled');
      }

      participantAnnotTypeRemoveService.remove(annotType, vm.annotTypesInUse);
    }

  }
});
