define(['../module', 'underscore'], function(module, _) {
  'use strict';

  module.controller('StudiesCtrl', StudiesCtrl);

  StudiesCtrl.$inject = ['studiesService', 'studyCount'];

  /**
   * Displays a list of studies with each in its own mini-panel.
   *
   */
  function StudiesCtrl(studiesService, studyCount) {
    var vm = this;
    vm.studyCount = studyCount;
    vm.studyRows = [];
    vm.paginatedStudies = {};
    vm.rowSize = 3;
    vm.pageSize = 6;

    vm.haveConfiguredStudies = (studyCount > 0);
    vm.nameFilter       = '';
    vm.possibleStatuses = [
      { id: 'all',      title: 'All' },
      { id: 'disabled', title: 'Disabled' },
      { id: 'enabled',  title: 'Enabled' },
      { id: 'retired',  title: 'Retired' }
    ];
    vm.status           = vm.possibleStatuses[0];

    vm.nameFilterUpdated = nameFilterUpdated;
    vm.statusFilterUpdated = statusFilterUpdated;

    updateStudies();

    function updateMessage() {
      if (vm.paginatedStudies.total <= 0) {
        vm.message = 'No studies match the criteria. ';
      } else if (vm.paginatedStudies.total === 1) {
        vm.message = 'There is 1 study that matches the criteria. ';
      } else {
        vm.message = 'There are ' + vm.paginatedStudies.total + ' studies that match the criteria. ';
        if (vm.paginatedStudies.total > vm.studyCount) {
          vm.message += 'Displaying the first ' + vm.studyCount + '.';
        }
      }
    }

    function updateStudies() {
      studiesService.getStudies(vm.nameFilter,
                                vm.status.id,
                                1,
                                vm.pageSize,
                                'name',
                                'ascending')
        .then(function (paginatedStudies) {
          vm.paginatedStudies = paginatedStudies;

          // split studies into array of rows of vm.rowSize items
          vm.studyRows = _.groupBy(paginatedStudies.items, function (item, index) {
            return Math.floor(index / vm.rowSize);
          });

          updateMessage();
        });
    }

    /**
     * Called when user enters text into the 'name filter'.
     */
    function nameFilterUpdated() {
      updateStudies();
    }

    /**
     * Called when user selects a status from the 'status filter' select.
     */
    function statusFilterUpdated() {
      updateStudies();
    }
  }

});
