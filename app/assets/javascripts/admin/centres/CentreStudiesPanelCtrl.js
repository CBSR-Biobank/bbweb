define(['../module'], function(module) {
  'use strict';

  module.controller('CentreStudiesPanelCtrl', CentreStudiesPanelCtrl);

  CentreStudiesPanelCtrl.$inject = [
    '$scope',
    '$state',
    'panelService',
  ];

  /**
   *
   */
  function CentreStudiesPanelCtrl($scope,
                                  $state,
                                  panelService) {

    var vm = this;

    var helper = panelService.panel(
      'centre.panel.studies');

    vm.centre      = $scope.centre;
    vm.studies     = $scope.studies;
    vm.update      = update;
    vm.remove      = remove;
    vm.add         = add;
    vm.information = information;
    vm.panelOpen   = helper.panelOpen;
    vm.panelToggle = helper.panelToggle;

    //--

    function add() {
    }

    function information(){ //study) {
    }

    function update(){ //study) {
    }

    function remove(){ //study) {
    }
  }

});
