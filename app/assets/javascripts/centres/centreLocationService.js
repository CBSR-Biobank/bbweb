define(['./module'], function(module) {
  'use strict';

  module.service('centreLocationService', centreLocationService);

  centreLocationService.$inject = ['biobankXhrReqService'];

  /**
   *
   */
  function centreLocationService(biobankXhrReqService) {
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
      return biobankXhrReqService.call('GET', uri(centreId));
    }

    function query(centreId, locationId) {
      return biobankXhrReqService.call(
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

      return biobankXhrReqService.call('POST', uri(centre.id), cmd);
    }

    function remove(centreId, locationId) {
      return biobankXhrReqService.call('DELETE', uri(centreId, locationId));
    }

  }

});
