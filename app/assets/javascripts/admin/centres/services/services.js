/**
 * User service, exposes user model to the rest of the app.
 */
define(['angular', 'common'], function(angular) {
  'use strict';

  var mod = angular.module('centres.services', ['biobank.common']);

  /**
   * Service to acccess centres.
   */
  mod.factory('CentreService', ['$http', function($http) {
    var changeStatus = function(status, centre) {
        var cmd = {
          id: centre.id,
          expectedVersion: centre.version
        };
        return $http.post('/centres/' + status, cmd);
    };

    return {
      list : function() {
        return $http.get('/centres');
      },
      query: function(id) {
        return $http.get('/centres/' + id);
      },
      addOrUpdate: function(centre) {
        var cmd = {
          name: centre.name,
          description: centre.description
        };

        if (centre.id) {
          cmd.id = centre.id;
          cmd.expectedVersion = centre.version;

          return $http.put('/centres/' + centre.id, cmd);
        } else {
          return $http.post('/centres', cmd);
        }
      },
      enable: function(centre) {
        return changeStatus('enabled', centre);
      },
      disable: function(centre) {
        return changeStatus('disabled', centre);
      }
    };
  }]);

});

