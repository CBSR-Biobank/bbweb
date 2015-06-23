/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(['jquery', 'underscore'], function($, _) {
  'use strict';

  //queryStringService.$inject = [];

  function queryStringService() { //
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
      var paramsStr = '';

      if (options) {
        paramsStr = $.param(_.pick(options, callback));
      }

      if (paramsStr === '') {
        return '';
      }

      return '?' + paramsStr;
    }
  }

  return queryStringService;
});
