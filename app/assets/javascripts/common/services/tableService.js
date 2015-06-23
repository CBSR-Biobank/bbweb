/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
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
      var parameters = {},
          settings = {},
          defaultParameters = {
            page: 1,            // show first page
            count: 10,           // count per page
            sorting: {
              name: 'asc'       // initial sorting
            }
          },
          defaultSettings = {
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

      customParameters = customParameters || {};
      customSettings = customSettings || {};

      // merge defaults with those passed in the function arguments
      parameters = angular.extend({}, defaultParameters, customParameters);
      settings   = angular.extend({}, defaultSettings, customSettings);

      /* jshint -W055 */
      var tableParams =  new ngTableParams(parameters, settings);
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
