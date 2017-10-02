/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(['angular', 'jquery'], function (angular, $) {
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

    function call(method, url, config) {
      config = config || {};
      angular.extend(config, { method: method, url: '/api' + url });

      return $http(config)
        .then(function(response) {
          // TODO: check status here and log it if it not 'success'
          if (response.data) {
            if (response.data.status === 'success'){
              return $q.when(response.data.data);
            } else {
              return $q.when(response.data);
            }
          }
          return $q.when({});
        })
        .catch(function(response) {
          if (response.data) {
            $log.error(response.status, response.data.message);
            return $q.reject(response.data);
          }
          return $q.reject(response);
        });
    }

    function get(url, params) {
      return call('GET', url, { params: params });
    }

    function post(url, data) {
      return call('POST', url, { data: data });
    }

    function put(url, data) {
      return call('PUT', url, { data: data });
    }

    function del(url) {
      return call('DELETE', url);
    }
  }

  return biobankApiService;
});
