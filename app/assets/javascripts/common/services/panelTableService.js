define(['../module', 'angular'], function(module, angular) {
  'use strict';

  module.service('panelTableService', PanelTableService);

  PanelTableService.$inject = ['$filter', 'ngTableParams'];

  /**
   * Service to create ng-tables.
   */
  function PanelTableService($filter, ngTableParams) {
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
        total: function () { return tableDataFn().length; },
        getData: function($defer, params) {
          var filteredData = tableDataFn();
          var orderedData = params.sorting() ?
              $filter('orderBy')(filteredData, params.orderBy()) : filteredData;
          params.total(filteredData.length);
          $defer.resolve(
            orderedData.slice(
              (params.page() - 1) * params.count(),
              params.page() * params.count()));
        }
      };

      var parameters = {};
      var settings = {};

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
      return getTableParamsWithCallback(getTableData, customParameters, customSettings);

      //--

      function getTableData() {
        return data;
      }
    }


  }

});
