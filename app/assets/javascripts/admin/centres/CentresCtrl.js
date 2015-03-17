define(['angular', 'underscore'], function(angular, _) {
  'use strict';

  CentresCtrl.$inject = ['$scope', 'Centre', 'CentreStatus', 'centreCounts'];

  /**
   * Displays a list of centres with each in its own mini-panel.
   *
   * @param {Centre} Centre - The constructor for the Centre class.
   *
   * @param {CentreSatus} CentreStatus - The CentreStatus service that enumarates a centres status values.
   *
   * @param {CentreCounts} centreCounts - the counts of centres broken down by status.
   */
  function CentresCtrl($scope, Centre, CentreStatus, centreCounts) {
    var vm = this;

    vm.centreCounts     = centreCounts;
    vm.pageSize         = 5;
    vm.updateCentres    = Centre.list;

    vm.possibleStatuses = [{ id: 'all', label: 'All' }];

    _.each(CentreStatus.values(), function(status) {
      vm.possibleStatuses.push({id: status.toLowerCase(), label: status});
    });
  }

  return CentresCtrl;
});
