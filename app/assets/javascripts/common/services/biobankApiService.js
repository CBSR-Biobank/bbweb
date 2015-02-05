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
      var deferred = $q.defer();
      var config = { method: method, url: url };

      if (data) {
        config.data = data;
      }

      $http(config)
        .then(function(response) {
          // TODO: check status here and log it if it not 'success'
          if (method === 'DELETE') {
            deferred.resolve(response.data);
          } else {
            deferred.resolve(response.data.data);
          }
        })
        .catch(function(response) {
          $log.error(response);
          deferred.reject(response);
        }
      );
      return deferred.promise;
    }
  }

});
