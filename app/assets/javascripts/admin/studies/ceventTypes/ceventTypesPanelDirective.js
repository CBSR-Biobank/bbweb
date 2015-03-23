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
        study:           '=',
        ceventTypes:     '=',
        annotTypes:      '=',
        annotTypesInUse: '=',
        specimenGroups:  '='
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
    'SpecimenGroupSet',
    'AnnotationTypeSet',
    'Panel',
    'ceventTypesService',
    'CeventTypeViewer',
    'AnnotationTypeViewer',
    'SpecimenGroupViewer',
    'ceventTypeUtils'
  ];

  /**
   * A panel to display a study's collection event types.
   */
  function CeventTypesPanelCtrl($scope,
                                $state,
                                modalService,
                                tableService,
                                CollectionEventType,
                                SpecimenGroupSet,
                                AnnotationTypeSet,
                                Panel,
                                ceventTypesService,
                                CeventTypeViewer,
                                AnnotationTypeViewer,
                                SpecimenGroupViewer,
                                ceventTypeUtils
                               ) {
    var vm = this,
        panel = new Panel('study.panel.collectionEventTypes',
                          'home.admin.studies.study.collection.ceventTypeAdd');

    vm.study = $scope.study;
    vm.specimenGroupSet  = new SpecimenGroupSet($scope.specimenGroups);
    vm.annotationTypeSet  = new AnnotationTypeSet($scope.annotTypes);

    vm.ceventTypes = _.map($scope.ceventTypes, function (ceventType) {
      return new CollectionEventType(
        vm.study,
        ceventType,
        {
          studySpecimenGroups:  $scope.specimenGroups,
          studyAnnotationTypes: $scope.annotTypes
        });
    });

    vm.update               = update;
    vm.remove               = remove;
    vm.add                  = add;
    vm.information          = information;
    vm.showAnnotationType   = showAnnotationType;
    vm.showSpecimenGroup    = showSpecimenGroup;
    vm.panelOpen            = panel.getPanelOpenState();
    vm.modificationsAllowed = vm.study.isDisabled();
    vm.tableParams           = tableService.getTableParamsWithCallback(getTableData,
                                                                       {},
                                                                       { counts: [] });

    $scope.$watch(angular.bind(vm, function() { return vm.panelOpen; }),
                  angular.bind(panel, panel.watchPanelOpenChangeFunc));

    //--

    function add() {
      if (vm.specimenGroupSet.isEmpty()) {
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
      return new CeventTypeViewer(vm.study, ceventType, vm.specimenGroups, vm.annotTypes);
    }

    /**
     * Display a collection event annotation type in a modal.
     *
     * @param id the ID for the annotation type.
     */
    function showAnnotationType(id) {
      return new AnnotationTypeViewer(vm.annotationTypeSet.get(id),
                                      'Collection Event Annotation Type');
    }

    /**
     * Displays a specimen group in a modal.
     */
    function showSpecimenGroup(specimenGroupId) {
      return new SpecimenGroupViewer(vm.specimenGroupSet.get(specimenGroupId));
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
      ceventTypeUtils.remove(ceventType, vm.study)
        .then(function () {
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
