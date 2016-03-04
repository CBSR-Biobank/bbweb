/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(['angular', 'underscore'], function(angular, _) {
  'use strict';

  /**
   *
   */
  function specimenGroupsPanelDirective() {
    var directive = {
      restrict: 'E',
      scope: {},
      bindToController: {
        study: '=',
        specimenGroups: '=',
        specimenGroupIdsInUse: '='
      },
      templateUrl: '/assets/javascripts/admin/studies/specimenGroups/directives/specimenGroupsPanel/specimenGroupsPanel.html',
      controller: SpecimenGroupsPanelCtrl,
      controllerAs: 'vm'
    };

    return directive;
  }

  SpecimenGroupsPanelCtrl.$inject = [
    '$scope',
    '$state',
    'Panel',
    'modalService',
    'domainEntityService',
    'SpecimenGroupViewer',
    'specimenGroupUtils'
  ];

  /**
   * A panel to display a study's specimen groups.
   */
  function SpecimenGroupsPanelCtrl($scope,
                                   $state,
                                   Panel,
                                   modalService,
                                   domainEntityService,
                                   SpecimenGroupViewer,
                                   specimenGroupUtils) {
    var vm = this,
        panel = new Panel('study.panel.specimenGroups',
                          'home.admin.studies.study.specimens.groupAdd');

    vm.update                = update;
    vm.remove                = remove;
    vm.add                   = add;
    vm.information           = information;
    vm.panelOpen             = panel.getPanelOpenState();
    vm.modificationsAllowed  = vm.study.isDisabled();

    $scope.$watch(angular.bind(vm, function() { return vm.panelOpen; }),
                  angular.bind(panel, panel.watchPanelOpenChangeFunc));


    //--

    function add() {
      return panel.add();
    }

    /**
     * Displays a specimen group in a modal.
     */
    function information(specimenGroup) {
      return new SpecimenGroupViewer(specimenGroup);
    }

    /**
     * Switches state to updte a specimen group.
     */
    function update(specimenGroup) {
      if (!vm.study.isDisabled()) {
        throw new Error('study is not disabled');
      }

      if (_.contains(vm.specimenGroupIdsInUse, specimenGroup.id)) {
        specimenGroupUtils.updateInUseModal(specimenGroup);
        return;
      }

      $state.go(
        'home.admin.studies.study.specimens.groupUpdate',
        { specimenGroupId: specimenGroup.id });
    }

    function remove(specimenGroup) {
      if (!vm.study.isDisabled()) {
        throw new Error('study is not disabled');
      }

      if (_.contains(vm.specimenGroupIdsInUse, specimenGroup.id)) {
        specimenGroupUtils.removeInUseModal(specimenGroup);
        return;
      }

      domainEntityService.removeEntity(
        specimenGroup.remove,
        'Remove Specimen Group',
        'Are you sure you want to remove specimen group ' + specimenGroup.name + '?',
        'Remove Failed',
        'Specimen group ' + specimenGroup.name + ' cannot be removed: '
      ).then(function () {
        vm.specimenGroups = _.without(vm.specimenGroups, specimenGroup);
      });
    }
  }

  return specimenGroupsPanelDirective;
});
