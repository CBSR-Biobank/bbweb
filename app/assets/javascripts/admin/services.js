/**
 * User service, exposes user model to the rest of the app.
 */
define(['angular', 'common'], function(angular) {
  'use strict';

  var mod = angular.module('admin.services', ['biobank.common']);

  mod.factory('AdminService', ['$http', function($http) {
    return {
      aggregateCounts : function() {
        return $http.get('/counts').then(function(response) {
          return response.data.data;
        });
      }
    };
  }]);

});
