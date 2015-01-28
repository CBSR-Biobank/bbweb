define(['../module', 'underscore'], function(module, _) {
  'use strict';

  module.controller('StudiesTableCtrl', StudiesTableCtrl);

  StudiesTableCtrl.$inject = [
    'studiesService',
    'tableService',
    'studyCount'
  ];

  /**
   * Displays a list of studies in an ng-table.
   *
   * This page uses a form to perform filtering on the table. This was done because filter using ng-table
   * was very slow since the data is re-loaded using the REST API.
   */
  function StudiesTableCtrl(studiesService,
                            tableService,
                            studyCount) {
    var vm = this;
    vm.studies          = [];
    vm.paginatedStudies = {};
    vm.haveConfiguredStudies = (studyCount > 0);
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

    vm.tableParams = tableService.getTableParams(vm.studies, tableParams, tableSettings);
    vm.nameFilterUpdated = nameFilterUpdated;
    vm.statusFilterUpdated = statusFilterUpdated;

    function updateMessage() {
      if ((vm.nameFilter === '') && (vm.status.id === 'all')) {
        vm.message = 'The following studies have been configured.';
      } else {
        vm.message = 'The following studies match the criteria:';
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

      studiesService.getStudies(options)
        .then(function (paginatedStudies) {
          vm.studies = paginatedStudies.items;
          vm.paginatedStudies = paginatedStudies;
          params.total(paginatedStudies.total);
          $defer.resolve(vm.studies);
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
