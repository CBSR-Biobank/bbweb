define(['../module', 'angular'], function(module, angular) {
  'use strict';

  module.service('tableService', TableService);

  TableService.$inject = ['ngTableParams'];

  /**
   * Service to create ng-tables.
   */
  function TableService(ngTableParams) {
    var service = {
      getTableParams: getTableParams
    };
    return service;

    //---

    /**
     * Creates an ng-table and the data is provided via a callback function.
     */
    function getTableParams(data, customParameters, customSettings) {
      var defaultParameters = {
        page: 1,            // show first page
        count: 10           // count per page
      };

      var defaultSettings = {
        total: function () { return data.length; },
        getData: function($defer, params) {
          $defer.resolve(data.slice((params.page() - 1) * params.count(), params.page() * params.count()));
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


  }

});
