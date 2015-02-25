define(['./module'], function(module) {
  'use strict';

  module.service('centreLocationsService', centreLocationsService);

  centreLocationsService.$inject = ['biobankApi', 'Location'];

  /**
   *
   */
  function centreLocationsService(biobankApi, Location) {
    var service = {
      list:   list,
      query:  query,
      add:    add,
      remove: remove
    };
    return service;

    //-------

    function uri(centreId, locationId) {
      var result;

      if (arguments.length <= 0) {
        throw new Error('invalid arguments');
      }

      result = '/centres/' + centreId + '/locations';

      if (arguments.length > 1) {
        result += '/' + locationId;
      }

      return result;
    }

    function list(centreId) {
      return biobankApi.call('GET', uri(centreId));
    }

    function query(centreId, locationId) {
      return biobankApi.call('GET', uri(centreId) + '?locationId=' + locationId);
    }

    function add(centre, location) {
      return biobankApi.call('POST', uri(centre.id), location.getAddCommand());
    }

    function remove(centreId, locationId) {
      return biobankApi.call('DELETE', uri(centreId, locationId));
    }

  }

});
