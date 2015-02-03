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
    'panelService',
    'ceventTypesService',
    'ceventAnnotTypesService',
    'ceventTypeModalService',
    'annotTypeModalService',
    'specimenGroupModalService',
    'ceventTypeRemoveService'
  ];

  /**
   * A panel to display a study's collection event types.
   */
  function CeventTypesPanelCtrl($scope,
                                $state,
                                $stateParams,
                                modalService,
                                panelService,
                                ceventTypesService,
                                ceventAnnotTypesService,
                                ceventTypeModalService,
                                annotTypeModalService,
                                specimenGroupModalService,
                                ceventTypeRemoveService) {
    var vm = this;

    var helper = panelService.panel(
      'study.panel.collectionEventTypes',
      'home.admin.studies.study.collection.ceventTypeAdd');

    vm.study                = $scope.study;
    vm.ceventTypes          = $scope.ceventTypes;
    vm.annotTypes           = $scope.annotTypes;
    vm.specimenGroups       = $scope.specimenGroups;
    vm.annotationTypesById  = [];
    vm.specimenGroupsById   = [];

    vm.update               = update;
    vm.remove               = ceventTypeRemoveService.remove;
    vm.add                  = add;
    vm.information          = information;
    vm.showAnnotationType   = showAnnotationType;
    vm.showSpecimenGroup    = showSpecimenGroup;
    vm.panelOpen            = helper.panelOpen;
    vm.panelToggle          = helper.panelToggle;
    vm.modificationsAllowed = vm.study.status === 'Disabled';

    init();

    vm.tableParams = helper.getTableParams(vm.ceventTypes);

    //--

    /**
     * Links the collection event with the specimen groups that they use.
     */
    function init() {
      vm.annotationTypesById = _.indexBy(vm.annotTypes, 'id');
      vm.specimenGroupsById = _.indexBy(vm.specimenGroups, 'id');

      _.each(vm.ceventTypes, function (cet) {
        cet.specimenGroups = [];
        _.each(cet.specimenGroupData, function (sgItem) {
          var sg = vm.specimenGroupsById[sgItem.specimenGroupId];
          cet.specimenGroups.push({ id: sgItem.specimenGroupId, name: sg.name });
        });

        cet.annotationTypes = [];
        _.each(cet.annotationTypeData, function (atItem) {
          var at = vm.annotationTypesById[atItem.annotationTypeId];
          cet.annotationTypes.push({ id: atItem.annotationTypeId, name: at.name });
        });
      });
    }

    function add() {
      if (vm.specimenGroups.length <= 0) {
        var headerHtml = 'Cannot add a collection event type';
        var bodyHtml = 'No <em>specimen groups</em> have been added to this study yet. ' +
            'Please add specimen groups first.';
        return modalService.modalOk(headerHtml, bodyHtml);
      } else {
        return $state.go('home.admin.studies.study.collection.ceventTypeAdd');
      }
    }

    /**
     * Display a collection event type in a modal.
     */
    function information(ceventType) {
      ceventTypeModalService.show(ceventType, vm.specimenGroups, vm.annotTypes);
    }

    /**
     * Display a collection event annotation type in a modal.
     */
    function showAnnotationType(annotTypeId) {
      annotTypeModalService.show('Specimen Link Annotation Type', vm.annotationTypesById[annotTypeId]);
    }

    /**
     * Displays a specimen group in a modal.
     */
    function showSpecimenGroup(specimenGroupId) {
      specimenGroupModalService.show(vm.specimenGroupsById[specimenGroupId]);
    }

    /**
     * Switches to the state to update a collection event type.
     */
    function update(ceventType) {
      $state.go(
        'home.admin.studies.study.collection.ceventTypeUpdate',
        { ceventTypeId: ceventType.id });
    }
  }

});
