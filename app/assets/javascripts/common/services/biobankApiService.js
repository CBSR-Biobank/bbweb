define(['../module'], function(module) {
  'use strict';

  module.service('biobankApi', biobankApi);

  biobankApi.$inject = ['$http', '$q', '$log'];

  /**
   * Makes a request to the Biobank server REST API. All REST responses from the server have a similar
   * response JSON object. This service returns the 'data' field if the call was successful.
   */
  function biobankApi($http, $q, $log) {
    var service = {
      call: call
    };
    return service;

    //-------------

    function call(method, url, data) {
      var config = { method: method, url: url };

      if (data) {
        config.data = data;
      }

      return $http(config)
        .then(function(response) {
          // TODO: check status here and log it if it not 'success'
          if (method === 'DELETE') {
            return response.data;
          } else {
            return response.data.data;
          }
        })
        .catch(function(response) {
          $log.error(response);
          $q.reject(response);
        }
      );
    }
  }

});
