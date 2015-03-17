define(['angular', 'underscore'], function(angular, _) {
  'use strict';

  /**
   *
   */
  function ceventAnnotTypesPanelDirective() {
    return {
      require: '^tab',
      restrict: 'E',
      scope: {
        study: '=',
        annotTypes: '=',
        annotTypesInUse: '='
      },
      templateUrl: '/assets/javascripts/admin/studies/annotationTypes/ceventAnnotTypesPanel.html',
      controller: 'CeventAnnotTypesPanelCtrl as vm'
    };
  }

  CeventAnnotTypesPanelCtrl.$inject = [
    '$scope',
    '$state',
    'modalService',
    'ceventAnnotTypesService',
    'annotationTypeRemoveService',
    'Panel',
    'AnnotationTypeViewer'
  ];

  /**
   * A panel to display a study's collection event annotation types.
   */
  function CeventAnnotTypesPanelCtrl($scope,
                                     $state,
                                     modalService,
                                     ceventAnnotTypesService,
                                     annotationTypeRemoveService,
                                     Panel,
                                     AnnotationTypeViewer) {
    var vm = this;

    var panel = new Panel('study.panel.collectionEventAnnotationTypes',
                           'home.admin.studies.study.collection.ceventAnnotTypeAdd');

    vm.study            = $scope.study;
    vm.annotTypes       = $scope.annotTypes;
    vm.annotTypesInUse  = $scope.annotTypesInUse;
    vm.update           = update;
    vm.remove           = remove;
    vm.information      = information;
    vm.add              = add;
    vm.panelOpen        = panel.getPanelOpenState();

    $scope.$watch(angular.bind(vm, function() { return vm.panelOpen; }),
                  angular.bind(panel, panel.watchPanelOpenChangeFunc));

    vm.modificationsAllowed = vm.study.status === 'Disabled';

    vm.columns = [
      { title: 'Name', field: 'name', filter: { 'name': 'text' } },
      { title: 'Type', field: 'valueType', filter: { 'valueType': 'text' } },
      { title: 'Description', field: 'description', filter: { 'description': 'text' } }
    ];

    vm.tableParams = panel.getTableParams(vm.annotTypes);

    //--

    function add() {
      return panel.add();
    }

    function information(annotType) {
      return new AnnotationTypeViewer(annotType, 'Collection Event Annotation Type');
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
          'home.admin.studies.study.collection.ceventAnnotTypeUpdate',
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
          'home.admin.studies.study.collection',
          {studyId: annotType.studyId});
      }
    }

  }

  return {
    directive: ceventAnnotTypesPanelDirective,
    controller: CeventAnnotTypesPanelCtrl
  };
});
