define(['../module'], function(module) {
  'use strict';

  module.controller('CentresCtrl', CentresCtrl);

  CentresCtrl.$inject = ['$scope', 'centresService', 'centreCounts'];

  /**
   * Displays a list of centres with each in its own mini-panel.
   *
   */
  function CentresCtrl($scope, centresService, centreCounts) {
    var vm = this;

    vm.centreCounts     = centreCounts;
    vm.pageSize         = 5;
    vm.updateCentres    = updateCentres;
    vm.possibleStatuses = [
      { id: 'all',      label: 'All' },
      { id: 'disabled', label: 'Disabled' },
      { id: 'enabled',  label: 'Enabled' }
    ];

    function updateCentres(options) {
      return centresService.getCentres(options);
    }
  }
});
