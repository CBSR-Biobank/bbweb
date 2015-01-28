define(['../module', 'underscore'], function(module, _) {
  'use strict';

  module.controller('CentresCtrl', CentresCtrl);

  CentresCtrl.$inject = ['centresService', 'centreCount'];

  /**
   * Displays a list of centres with each in its own mini-panel.
   */
  function CentresCtrl(centresService, centreCount) {
    var vm = this;
    vm.centreCount = centreCount;
    vm.centreRows = [];
    vm.paginatedCentres = {};
    vm.rowSize = 3;
    vm.pageSize = 6;

    vm.haveConfiguredCentres = (centreCount > 0);
    vm.nameFilter       = '';
    vm.possibleStatuses = [
      { id: 'all',      title: 'All' },
      { id: 'disabled', title: 'Disabled' },
      { id: 'enabled',  title: 'Enabled' }
    ];
    vm.status           = vm.possibleStatuses[0];

    vm.nameFilterUpdated = nameFilterUpdated;
    vm.statusFilterUpdated = statusFilterUpdated;

    updateCentres();

    function updateMessage() {
      if (vm.paginatedCentres.total <= 0) {
        vm.message = 'No centres match the criteria. ';
      } else if (vm.paginatedCentres.total === 1) {
        vm.message = 'There is 1 centre that matches the criteria. ';
      } else {
        vm.message = 'There are ' + vm.paginatedCentres.total + ' centres that match the criteria. ';
        if (vm.paginatedCentres.total > vm.centreCount) {
          vm.message += 'Displaying the first ' + vm.centreCount + '.';
        }
      }
    }

    function updateCentres() {
      var options = {
        filter: vm.nameFilter,
        status: vm.status.id,
        page: 1,
        pageSize: vm.pageSize,
        sort: 'name',
        order: 'asc'
      };
      centresService.getCentres(options)
        .then(function (paginatedCentres) {
          vm.paginatedCentres = paginatedCentres;

          // split centres into array of rows of vm.rowSize items
          vm.centreRows = _.groupBy(paginatedCentres.items, function (item, index) {
            return Math.floor(index / vm.rowSize);
          });

          updateMessage();
        });
    }

    /**
     * Called when user enters text into the 'name filter'.
     */
    function nameFilterUpdated() {
      updateCentres();
    }

    /**
     * Called when user selects a status from the 'status filter' select.
     */
    function statusFilterUpdated() {
      updateCentres();
    }
  }

});
