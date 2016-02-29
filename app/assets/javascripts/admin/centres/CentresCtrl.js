/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(['underscore'], function(_) {
  'use strict';

  CentresCtrl.$inject = ['$scope', 'Centre', 'CentreStatus', 'CentreCounts'];

  /**
   * Displays a list of centres with each in its own mini-panel.
   *
   * @param {Centre} Centre - The constructor for the Centre class.
   *
   * @param {CentreSatus} CentreStatus - The CentreStatus service that enumarates a centres status values.
   *
   * @param {CentreCounts} centreCounts - the counts of centres broken down by status.
   */
  function CentresCtrl($scope, Centre, CentreStatus, CentreCounts) {
    var vm = this;

    vm.centreCounts     = {};
    vm.pageSize         = 5;
    vm.updateCentres    = Centre.list;
    vm.possibleStatuses = [{ id: 'all', label: 'All' }];

    _.each(CentreStatus.values(), function(status) {
      vm.possibleStatuses.push({id: status, label: CentreStatus.label(status)});
    });

    init();

    //--

    function init() {
      CentreCounts.get().then(function (counts) {
        vm.centreCounts = counts;
      });
    }
  }

  return CentresCtrl;
});
