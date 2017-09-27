/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(['angular', 'jquery'], function (angular, $) {
  'use strict';

  biobankApiService.$inject = ['$http', '$q', '$log', 'AppConfig' ];

  /**
   * Makes a request to the Biobank server REST API. All REST responses from the server have a similar
   * response JSON object. This service returns the 'data' field if the call was successful.
   */
  function biobankApiService($http, $q, $log, AppConfig) {
    var service = {
      get:  get,
      post: post,
      put:  put,
      del:  del
    };
    return service;

    //-------------

    function apiCall(method, url, config) {
      if (url.indexOf(AppConfig.restApiUrlPrefix) < 0) {
        throw new Error('invalid REST API url: ' + url);
      }

      config = config || {};
      angular.extend(config, { method: method, url: url });

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
      return apiCall('GET', url, { params: params });
    }

    function post(url, data) {
      return apiCall('POST', url, { data: data });
    }

    function put(url, data) {
      return apiCall('PUT', url, { data: data });
    }

    function del(url) {
      return apiCall('DELETE', url);
    }
  }

  return biobankApiService;
});
