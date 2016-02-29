/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(['angular', 'underscore'], function(angular, _) {
  'use strict';

  /**
   *
   */
  function ceventTypesPanelDirective() {
    var directive = {
      restrict: 'E',
      scope: {},
      bindToController: {
        study:                  '=',
        ceventTypes:            '=',
        annotationTypeIdsInUse: '='
      },
      templateUrl: '/assets/javascripts/admin/studies/ceventTypes/directives/ceventTypesPanel/ceventTypesPanel.html',
      controller: CeventTypesPanelCtrl,
      controllerAs: 'vm'
    };
    return directive;
  }

  CeventTypesPanelCtrl.$inject = [
    '$scope',
    '$state',
    'modalService',
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
                                Panel,
                                CeventTypeViewer,
                                AnnotationTypeViewer,
                                SpecimenGroupViewer,
                                domainEntityService) {
    var vm = this,
        panel = new Panel('study.panel.collectionEventTypes',
                          'home.admin.studies.study.collection.ceventTypeAdd');

    vm.update               = update;
    vm.remove               = remove;
    vm.add                  = add;
    vm.information          = information;
    vm.viewAnnotationType   = viewAnnotationType;
    vm.viewSpecimenGroup    = viewSpecimenGroup;
    vm.panelOpen            = panel.getPanelOpenState();
    vm.modificationsAllowed = vm.study.isDisabled();

    $scope.$watch(angular.bind(vm, function() { return vm.panelOpen; }),
                  angular.bind(panel, panel.watchPanelOpenChangeFunc));

    //--

    function add() {
      return $state.go('home.admin.studies.study.collection.ceventTypeAdd');
    }

    /**
     * Display a collection event type in a modal.
     */
    function information(ceventType) {
      return new CeventTypeViewer(vm.study, ceventType);
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
      });
    }

  }

  return ceventTypesPanelDirective;
});
