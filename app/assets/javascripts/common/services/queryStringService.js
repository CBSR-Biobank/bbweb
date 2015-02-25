define(['../module', 'jquery', 'underscore'], function(module, $, _) {
  'use strict';

  module.service('queryStringService', QueryStringService);

  //QueryStringService.$inject = [];

  function QueryStringService() {
    var service = {
      param: param
    };
    return service;

    //-------

    /**
     * Creates a query string to be appended to a URI. Function 'callback' is invoked to determine if the key
     * in the options object is valid. Function callback signature is: function(value, key, object).
     */
    function param(options, callback) {
      var paramsStr = null;

      if (options) {
        paramsStr = '?' + $.param(_.pick(options, callback));
      }

      return paramsStr;
    }

  }

});
