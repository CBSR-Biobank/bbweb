define(['./module'], function(module) {
  'use strict';

  module.service('centreLocationService', centreLocationService);

  centreLocationService.$inject = ['biobankXhrReqService'];

  /**
   *
   */
  function centreLocationService(biobankXhrReqService) {
    var service = {
      list:        list,
      query:       query,
      addOrUpdate: addOrUpdate
    };
    return service;

    //-------

    function list(centreId) {
      return biobankXhrReqService.call('GET', '/centres/locations/' + centreId);
    }

    function query(centreId, locationId) {
      return biobankXhrReqService.call(
        'GET',
        '/centres/locations/' + centreId + '?locationId=' + locationId);
    }

    function addOrUpdate(centre, location) {
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

        return biobankXhrReqService.call('PUT', '/centres/locations/' + centre.id, cmd);
      } else {
        return biobankXhrReqService.call('POST', '/centres/locations', cmd);
      }
    }

  }

});
