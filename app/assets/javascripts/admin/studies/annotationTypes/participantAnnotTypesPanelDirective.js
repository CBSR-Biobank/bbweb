define(['angular', 'underscore'], function(angular, _) {
  'use strict';

  /**
   *
   */
  function participantsAnnotTypesPanelDirective() {
    return {
      require: '^tab',
      restrict: 'E',
      scope: {
        study: '=',
        annotTypes: '='
      },
      templateUrl: '/assets/javascripts/admin/studies/annotationTypes/participantAnnotTypesPanel.html',
      controller: 'ParticipantAnnotTypesPanelCtrl as vm'
    };
  }

  ParticipantAnnotTypesPanelCtrl.$inject = [
    '$scope',
    '$state',
    'modalService',
    'participantAnnotTypeRemoveService',
    'Panel',
    'AnnotationTypeViewer'
  ];

  /**
   * A panel to display a study's participant annotation types.
   */
  function ParticipantAnnotTypesPanelCtrl($scope,
                                          $state,
                                          modalService,
                                          participantAnnotTypeRemoveService,
                                          Panel,
                                          AnnotationTypeViewer) {
    var vm = this;

    var panel = new Panel('study.panel.participantAnnotationTypes',
                           'home.admin.studies.study.participants.annotTypeAdd');

    vm.study       = $scope.study;
    vm.annotTypes  = $scope.annotTypes;
    vm.hasRequired = true;
    vm.update      = update;
    vm.remove      = remove;
    vm.information = information;
    vm.add         = add;
    vm.panelOpen   = panel.getPanelOpenState();

    $scope.$watch(angular.bind(vm, function() { return vm.panelOpen; }),
                  angular.bind(panel, panel.watchPanelOpenChangeFunc));

    vm.modificationsAllowed = vm.study.status === 'Disabled';

    vm.columns = [
      { title: 'Name', field: 'name', filter: { 'name': 'text' } },
      { title: 'Type', field: 'valueType', filter: { 'valueType': 'text' } },
      { title: 'Required', field: 'required', filter: { 'required': 'text' } },
      { title: 'Description', field: 'description', filter: { 'description': 'text' } }
    ];

    vm.tableParams = panel.getTableParams(vm.annotTypes);

    // FIXME this is set to empty array for now, but will have to call the correct method in the future
    vm.annotTypesInUse = [];

    //--

    function add() {
      return panel.add();
    }

    function information(annotationType) {
      return new AnnotationTypeViewer(annotationType, 'Participant Annotation Type');
    }

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
          'home.admin.studies.study.participants.annotTypeUpdate',
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

  return {
    directive: participantsAnnotTypesPanelDirective,
    controller: ParticipantAnnotTypesPanelCtrl
  };
});
