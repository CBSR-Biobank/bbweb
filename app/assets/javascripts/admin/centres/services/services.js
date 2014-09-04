/**
 * User service, exposes user model to the rest of the app.
 */
define(['angular', 'common'], function(angular) {
  'use strict';

  var mod = angular.module('centres.services', ['biobank.common']);

  /**
   * Service to acccess centres.
   */
  mod.factory('CentreService', ['BbwebRestApi', function(BbwebRestApi) {
    var changeStatus = function(status, centre) {
      var cmd = {
        id: centre.id,
        expectedVersion: centre.version
      };
      return BbwebRestApi.call('POST', '/centres/' + status, cmd);
    };

    return {
      list : function() {
        return BbwebRestApi.call('GET','/centres');
      },
      query: function(id) {
        return BbwebRestApi.call('GET','/centres/' + id);
      },
      addOrUpdate: function(centre) {
        var cmd = {
          name: centre.name,
          description: centre.description
        };

        if (centre.id) {
          cmd.id = centre.id;
          cmd.expectedVersion = centre.version;

          return BbwebRestApi.call('PUT', '/centres/' + centre.id, cmd);
        } else {
          return BbwebRestApi.call('POST', '/centres', cmd);
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

  mod.factory('CentreLocationService', ['BbwebRestApi', function(BbwebRestApi) {
    return {
      list : function() {
        return BbwebRestApi.call('GET', '/centres/locations');
      },
      query: function(id) {
        return BbwebRestApi.call('GET', '/centres/locations/' + id);
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

          return BbwebRestApi.call('PUT', '/centres/locations/' + centre.id, cmd);
        } else {
          return BbwebRestApi.call('POST', '/centres/locations', cmd);
        }
      }
    };
  }]);

});

