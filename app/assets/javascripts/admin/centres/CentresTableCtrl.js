define(['../module', 'underscore'], function(module, _) {
  'use strict';

  module.controller('CentresTableCtrl', CentresTableCtrl);

  CentresTableCtrl.$inject = [
    'centresService',
    'tableService',
    'centreCount'
  ];

  /**
   * Displays a list of centres in an ng-table.
   */
  function CentresTableCtrl(centresService,
                            tableService,
                            centreCount) {
    var vm = this;
    vm.centres          = [];
    vm.paginatedCentres = {};
    vm.haveConfiguredCentres = (centreCount > 0);
    vm.nameFilter       = '';
    vm.possibleStatuses = [
      { id: 'all',      title: 'All' },
      { id: 'disabled', title: 'Disabled' },
      { id: 'enabled',  title: 'Enabled' },
      { id: 'retired',  title: 'Retired' }
    ];
    vm.status           = vm.possibleStatuses[0];

    var tableParams = {
      page: 1,
      count: 5,
      sorting: {
        name: 'asc'
      }
    };

    var tableSettings = {
      total: 0,
      getData: getData
    };

    vm.tableParams = tableService.getTableParams(vm.centres, tableParams, tableSettings);
    vm.nameFilterUpdated = nameFilterUpdated;
    vm.statusFilterUpdated = statusFilterUpdated;

    function updateMessage() {
      if ((vm.nameFilter === '') && (vm.status.id === 'all')) {
        vm.message = 'The following centres have been configured.';
      } else {
        vm.message = 'The following centres match the criteria:';
      }
    }

    function getData($defer, params) {
      var sortObj = params.sorting();
      var sortKeys = _.keys(sortObj);
      var options = {
        filter:   vm.nameFilter,
        status:   vm.status.id,
        page:     params.page(),
        pageSize: params.count(),
        sort:     sortKeys[0],
        order:    sortObj[sortKeys[0]]
      };

      centresService.getCentres(options)
        .then(function (paginatedCentres) {
          vm.centres = paginatedCentres.items;
          vm.paginatedCentres = paginatedCentres;
          params.total(paginatedCentres.total);
          $defer.resolve(vm.centres);
          updateMessage();
        });
    }

    function tableReloadCommon() {
      vm.tableParams.page(1);
      vm.tableParams.reload();
    }

    /**
     * Called when user enters text into the 'name filter'.
     */
    function nameFilterUpdated() {
      tableReloadCommon();
    }

    /**
     * Called when user selects a status from the 'status filter' select.
     */
    function statusFilterUpdated() {
      tableReloadCommon();
    }

  }

});
