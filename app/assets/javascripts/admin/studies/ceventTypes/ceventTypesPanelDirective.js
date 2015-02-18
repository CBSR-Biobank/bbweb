define(['../../module', 'underscore'], function(module, _) {
  'use strict';

  module.directive('ceventTypesPanel', ceventTypesPanelDirective);

  /**
   *
   */
  function ceventTypesPanelDirective() {
    var directive = {
      require: '^tab',
      restrict: 'E',
      scope: {
        study: '=',
        ceventTypes: '=',
        annotTypes: '=',
        specimenGroups: '='
      },
      templateUrl: '/assets/javascripts/admin/studies/ceventTypes/ceventTypesPanel.html',
      controller: 'CeventTypesPanelCtrl as vm'
    };
    return directive;
  }

  module.controller('CeventTypesPanelCtrl', CeventTypesPanelCtrl);

  CeventTypesPanelCtrl.$inject = [
    '$scope',
    '$state',
    '$stateParams',
    'modalService',
    'CollectionEventType',
    'SpecimenGroupSet',
    'AnnotationTypeSet',
    'Panel',
    'ceventTypesService',
    'ceventAnnotTypesService',
    'CeventTypeViewer',
    'AnnotationTypeViewer',
    'SpecimenGroupViewer',
    'ceventTypeRemoveService'
  ];

  /**
   * A panel to display a study's collection event types.
   */
  function CeventTypesPanelCtrl($scope,
                                $state,
                                $stateParams,
                                modalService,
                                CollectionEventType,
                                SpecimenGroupSet,
                                AnnotationTypeSet,
                                Panel,
                                ceventTypesService,
                                ceventAnnotTypesService,
                                CeventTypeViewer,
                                AnnotationTypeViewer,
                                SpecimenGroupViewer,
                                ceventTypeRemoveService) {
    var vm = this;

    var helper = new Panel('study.panel.collectionEventTypes',
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
    vm.remove               = ceventTypeRemoveService.remove;
    vm.add                  = add;
    vm.information          = information;
    vm.showAnnotationType   = showAnnotationType;
    vm.showSpecimenGroup    = showSpecimenGroup;
    vm.panelOpen            = helper.panelOpen;
    vm.panelToggle          = panelToggle;
    vm.modificationsAllowed = vm.study.status === 'Disabled';

    vm.tableParams = helper.getTableParams(vm.ceventTypes);

    //--

    function panelToggle() {
      return helper.panelToggle();
    }

    function add() {
      if (vm.specimenGroups.length <= 0) {
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
      $state.go('home.admin.studies.study.collection.ceventTypeUpdate', { ceventTypeId: ceventType.id });
    }
  }

});
