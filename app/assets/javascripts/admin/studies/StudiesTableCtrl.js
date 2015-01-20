define(['../module', 'underscore'], function(module, _) {
  'use strict';

  module.controller('StudiesTableCtrl', StudiesTableCtrl);

  StudiesTableCtrl.$inject = [
    '$q',
    'studiesService',
    'tableService',
    'paginatedStudies'
  ];

  /**
   * Displays a list of studies in an ng-table.
   *
   * This page uses a form to perform filtering on the table. This was done because filter using ng-table
   * was very slow since the data is re-loaded using the REST API.
   */
  function StudiesTableCtrl($q,
                            studiesService,
                            tableService,
                            paginatedStudies) {
    var vm = this;
    vm.studies          = paginatedStudies.items;
    vm.nameFilter       = '';
    vm.paginatedStudies = paginatedStudies;
    vm.possibleStatuses = [
      { id: 'all',      title: 'All' },
      { id: 'disabled', title: 'Disabled' },
      { id: 'enabled',  title: 'Enabled' },
      { id: 'retired',  title: 'Retired' }
    ];
    vm.status           = vm.possibleStatuses[0];

    var customParameters = {
      page: 1,
      count: paginatedStudies.pageSize,
      sorting: {
        name: 'asc'
      }
    };

    var customSettings = {
      total: 0,
      getData: getData
    };

    vm.tableParams = tableService.getTableParams(vm.studies, customParameters, customSettings);
    vm.nameFilterUpdated = nameFilterUpdated;
    vm.statusFilterUpdated = statusFilterUpdated;

    function getData($defer, params) {
      var sortObj = params.sorting();
      var sortKeys = _.keys(sortObj);

      studiesService.getStudies(vm.nameFilter,
                                vm.status.id,
                                params.page(),
                                params.count(),
                                sortKeys[0],
                                sortObj[sortKeys[0]])
        .then(function (paginatedStudies) {
          vm.studies = paginatedStudies.items;
          vm.paginatedStudies = paginatedStudies;
          params.total(paginatedStudies.total);
          $defer.resolve(vm.studies);
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
