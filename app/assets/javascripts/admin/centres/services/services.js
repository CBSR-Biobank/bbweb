/**
 * User service, exposes user model to the rest of the app.
 */
define(['angular', 'common'], function(angular) {
  'use strict';

  var mod = angular.module('centres.services', ['biobank.common']);

  var onHttpPromiseSuccess = function(data) {
    return data.data;
  };

  var onHttpPromiseError = function(data) {
    return data.message;
  };

  /**
   * Service to acccess centres.
   */
  mod.factory('CentreService', ['$http', function($http) {
    var changeStatus = function(status, centre) {
      var cmd = {
        id: centre.id,
        expectedVersion: centre.version
      };
      return $http.post('/centres/' + status, cmd).success(onHttpPromiseSuccess).error(onHttpPromiseError);
    };

    return {
      list : function() {
        return $http.get('/centres').success(onHttpPromiseSuccess).error(onHttpPromiseError);
      },
      query: function(id) {
        return $http.get('/centres/' + id).success(onHttpPromiseSuccess).error(onHttpPromiseError);
      },
      addOrUpdate: function(centre) {
        var cmd = {
          name: centre.name,
          description: centre.description
        };

        if (centre.id) {
          cmd.id = centre.id;
          cmd.expectedVersion = centre.version;

          return $http.put('/centres/' + centre.id, cmd).success(onHttpPromiseSuccess).error(onHttpPromiseError);
        } else {
          return $http.post('/centres', cmd).success(onHttpPromiseSuccess).error(onHttpPromiseError);
        }
      },
      enable: function(centre) {
        return changeStatus('enabled', centre).success(onHttpPromiseSuccess).error(onHttpPromiseError);
      },
      disable: function(centre) {
        return changeStatus('disabled', centre).success(onHttpPromiseSuccess).error(onHttpPromiseError);
      }
    };
  }]);

  mod.factory('CentreLocationService', ['$http', function($http) {
    return {
      list : function() {
        return $http.get('/centres/locations').success(onHttpPromiseSuccess).error(onHttpPromiseError);
      },
      query: function(id) {
        return $http.get('/centres/locations/' + id).success(onHttpPromiseSuccess).error(onHttpPromiseError);
      },
      addOrUpdate: function(centre, location) {
        var cmd = {
          centreId:       centre.id,
          name:           location.name,
          street:         location.street,
          city:           location.city,
          province:       location.province,
          postalCode:     location.postalCode,
          poBoxNumber:    location.poBoxNumber,
          countryIsoCode: location.countryIsoCode
        };

        if (centre.id) {
          cmd.id = centre.id;
          cmd.expectedVersion = centre.version;

          return $http.put('/centres/locations/' + centre.id, cmd)
            .success(onHttpPromiseSuccess).error(onHttpPromiseError);
        } else {
          return $http.post('/centres/locations', cmd).success(onHttpPromiseSuccess).error(onHttpPromiseError);
        }
      }
    };
  }]);

});

