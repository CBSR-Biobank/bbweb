define(['angular'], function(angular) {
  'use strict';

  tableService.$inject = ['$filter', 'ngTableParams'];

  /**
   * Service to create ng-tables.
   */
  function tableService($filter, ngTableParams) {
    var service = {
      getTableParamsWithCallback: getTableParamsWithCallback,
      getTableParams: getTableParams
    };
    return service;

    //---

    /**
     * Creates an ng-table and the data is provided via a callback function.
     */
    function getTableParamsWithCallback(tableDataFn, customParameters, customSettings) {
      var defaultParameters = {
        page: 1,            // show first page
        count: 10,           // count per page
        sorting: {
          name: 'asc'       // initial sorting
        }
      };

      var defaultSettings = {
        total: 0,
        getData: function($defer, params) {
          var filteredData = tableDataFn();
          var orderedData = params.sorting() ?
              $filter('orderBy')(filteredData, params.orderBy()) : filteredData;
          var page = params.page();
          var slice = orderedData.slice((page - 1) * params.count(), page * params.count());

          if ((page > 1) && (slice.length <= 0)) {
            params.page(page - 1);
          }
          params.total(filteredData.length);
          $defer.resolve(slice);
        }
      };

      var parameters = {};
      var settings = {};

      customParameters = customParameters || {};
      customSettings = customSettings || {};

      // merge defaults with those passed in the function arguments
      angular.extend(parameters, defaultParameters, customParameters);
      angular.extend(settings, defaultSettings, customSettings);

      /* jshint -W055 */
      var tableParams =  new ngTableParams(parameters, settings);
      /* jshint +W055 */

      return tableParams;
    }

    /**
     * Creates an ng-table for the data passed in.
     */
    function getTableParams(data, customParameters, customSettings) {
      customParameters = customParameters || {};
      customSettings = customSettings || {};
      return getTableParamsWithCallback(getTableData, customParameters, customSettings);

      //--

      function getTableData() {
        return data;
      }
    }
  }

  return tableService;
});
