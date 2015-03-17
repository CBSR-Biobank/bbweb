define(['underscore'], function(_) {
  'use strict';

  centreLocationsService.$inject = ['biobankApi'];

  /**
   *
   */
  function centreLocationsService(biobankApi) {
    var service = {
      list:   list,
      query:  query,
      add:    add,
      remove: remove
    };
    return service;

    //-------

    function getAddCommand(centre, location) {
      var cmd = _.pick(location,
                      'name', 'street', 'city', 'province', 'postalCode', 'countryIsoCode');

      _.extend(cmd, {centreId: centre.id});

      _.each(['poBoxNumber'], function(attr){
        if (location[attr] !== null) {
          cmd[attr] = location[attr];
        }
      });

      return cmd;
    }

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
      return biobankApi.get(uri(centreId));
    }

    function query(centreId, locationId) {
      return biobankApi.get(uri(centreId) + '?locationId=' + locationId);
    }

    function add(centre, location) {
      return biobankApi.post(uri(centre.id), getAddCommand(centre, location));
    }

    function remove(centre, location) {
      return biobankApi.del(uri(centre.id, location.id));
    }

  }

  return centreLocationsService;
});
