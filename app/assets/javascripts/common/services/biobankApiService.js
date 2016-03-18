/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(function (){
  'use strict';

  biobankApiService.$inject = ['$http', '$q', '$log'];

  /**
   * Makes a request to the Biobank server REST API. All REST responses from the server have a similar
   * response JSON object. This service returns the 'data' field if the call was successful.
   */
  function biobankApiService($http, $q, $log) {
    var service = {
      get:  get,
      post: post,
      put:  put,
      del:  del
    };
    return service;

    //-------------

    function call(method, url, data) {
      var deferred = $q.defer();
      var config = { method: method, url: url };

      if (data && ((method === 'POST') || (method === 'PUT'))) {
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
        });
      return deferred.promise;
    }

    function get(url) {
      return call('GET', url);
    }

    function post(url, cmd) {
      return call('POST', url, cmd);
    }

    function put(url, cmd) {
      return call('PUT', url, cmd);
    }

    function del(url) {
      return call('DELETE', url);
    }
  }

  return biobankApiService;
});
