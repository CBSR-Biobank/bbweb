define(['./module'], function(module) {
  'use strict';

  module.service('centreLocationService', centreLocationService);

  centreLocationService.$inject = ['biobankApi'];

  /**
   *
   */
  function centreLocationService(biobankApi) {
    var service = {
      list:   list,
      query:  query,
      add:    add,
      remove: remove
    };
    return service;

    //-------

    function uri(centreId, locationId) {
      var result = '/centres/' + centreId + '/locations';

      if (arguments.length <= 0) {
        throw new Error('invalid arguments');
      }

      if (arguments.length > 1) {
        result += '/' + locationId;
      }

      return result;
    }

    function list(centreId) {
      return biobankApi.call('GET', uri(centreId));
    }

    function query(centreId, locationId) {
      return biobankApi.call(
        'GET',
        uri(centreId) + '?locationId=' + locationId);
    }

    function add(centre, location) {
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

      return biobankApi.call('POST', uri(centre.id), cmd);
    }

    function remove(centreId, locationId) {
      return biobankApi.call('DELETE', uri(centreId, locationId));
    }

  }

});
