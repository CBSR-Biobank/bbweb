define(['angular', 'underscore'], function(angular, _) {
  'use strict';

  /**
   *
   */
  function ceventTypesPanelDirective() {
    var directive = {
      require: '^tab',
      restrict: 'E',
      scope: {
        study:                  '=',
        ceventTypes:            '=',
        annotationTypes:        '=',
        annotationTypeIdsInUse: '=',
        specimenGroups:         '='
      },
      templateUrl: '/assets/javascripts/admin/studies/ceventTypes/ceventTypesPanel.html',
      controller: 'CeventTypesPanelCtrl as vm'
    };
    return directive;
  }

  CeventTypesPanelCtrl.$inject = [
    '$scope',
    '$state',
    'modalService',
    'tableService',
    'CollectionEventType',
    'Panel',
    'CeventTypeViewer',
    'AnnotationTypeViewer',
    'SpecimenGroupViewer',
    'domainEntityService'
  ];

  /**
   * A panel to display a study's collection event types.
   */
  function CeventTypesPanelCtrl($scope,
                                $state,
                                modalService,
                                tableService,
                                CollectionEventType,
                                Panel,
                                CeventTypeViewer,
                                AnnotationTypeViewer,
                                SpecimenGroupViewer,
                                domainEntityService
                               ) {
    var vm = this,
        panel = new Panel('study.panel.collectionEventTypes',
                          'home.admin.studies.study.collection.ceventTypeAdd');

    vm.study               = $scope.study;
    vm.specimenGroupsById  = _.indexBy($scope.specimenGroups, 'id');
    vm.annotationTypesById = _.indexBy($scope.annotationTypes, 'id');
    vm.ceventTypes         = $scope.ceventTypes;

    vm.update               = update;
    vm.remove               = remove;
    vm.add                  = add;
    vm.information          = information;
    vm.viewAnnotationType   = viewAnnotationType;
    vm.viewSpecimenGroup    = viewSpecimenGroup;
    vm.panelOpen            = panel.getPanelOpenState();
    vm.modificationsAllowed = vm.study.isDisabled();
    vm.tableParams          = tableService.getTableParamsWithCallback(getTableData,
                                                                      {},
                                                                      { counts: [] });

    $scope.$watch(angular.bind(vm, function() { return vm.panelOpen; }),
                  angular.bind(panel, panel.watchPanelOpenChangeFunc));

    //--

    function add() {
      if ($scope.specimenGroups.length <= 0) {
        var headerHtml = 'Cannot add a collection event type';
        var bodyHtml = 'No <em>specimen groups</em> have been added to this study yet. ' +
            'Please add specimen groups first and then add a collection event type.';
        return modalService.modalOk(headerHtml, bodyHtml);
      } else {
        return $state.go('home.admin.studies.study.collection.ceventTypeAdd');
      }
    }

    /**
     * Display a collection event type in a modal.
     */
    function information(ceventType) {
      return new CeventTypeViewer(vm.study, ceventType, vm.specimenGroupsById, vm.annotationTypesById);
    }

    /**
     * Displays a specimen group in a modal.
     *
     * @param {String} id the ID for the specimen group.
     */
    function viewSpecimenGroup(id) {
      return new SpecimenGroupViewer(vm.specimenGroupsById[id]);
    }

    /**
     * Display a collection event annotation type in a modal.
     *
     * @param {String} id the ID for the annotation type.
     */
    function viewAnnotationType(id) {
      return new AnnotationTypeViewer(vm.annotationTypesById[id],
                                      'Collection Event Annotation Type');
    }

    /**
     * Switches to the state to update a collection event type.
     */
    function update(ceventType) {
      if (!vm.study.isDisabled()) {
        throw new Error('study is not disabled');
      }
      $state.go('home.admin.studies.study.collection.ceventTypeUpdate',
                { ceventTypeId: ceventType.id });
    }

    function remove(ceventType) {
      if (!vm.study.isDisabled()) {
        throw new Error('study is not disabled');
      }
      domainEntityService.removeEntity(
        ceventType,
        'Remove Collection Event Type',
        'Are you sure you want to remove collection event type ' + ceventType.name + '?',
        'Remove Failed',
        'Collection event type ' + ceventType.name + ' cannot be removed: '
      ).then(function () {
        vm.ceventTypes = _.without(vm.ceventTypes, ceventType);
        vm.tableParams.reload();
      });
    }

    function getTableData() {
      return vm.ceventTypes;
    }

  }

  return {
    directive: ceventTypesPanelDirective,
    controller: CeventTypesPanelCtrl
  };
});
