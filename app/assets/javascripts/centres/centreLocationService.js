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

    function list(centreId) {
      return biobankXhrReqService.call('GET', '/centres/centre/' + centreId + '/locations');
    }

    function query(centreId, locationId) {
      return biobankXhrReqService.call(
        'GET',
        '/centres/centre/' + centreId + '/locations/?locationId=' + locationId);
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

      return biobankXhrReqService.call('POST', '/centres/centre/locations', cmd);
    }

    function remove(centreId, locationId) {
      return biobankXhrReqService.call('DELETE', '/centres/centre/' + centreId + '/locations/' + locationId);
    }

  }

});
