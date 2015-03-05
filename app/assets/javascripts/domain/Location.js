define(['./module', 'angular', 'underscore'], function(module, angular, _) {
  'use strict';

  module.factory('Location', LocationFactory);

  LocationFactory.$inject = ['validationService'];

  /**
   *
   */
  function LocationFactory(validationService) {

    var checkObject = validationService.checker(
      validationService.aMapValidator,
      validationService.hasKeys('centreId',
                                'locationId',
                                'name',
                                'street',
                                'city',
                                'province',
                                'postalCode',
                                'countryIsoCode'));

    /**
     * Location is a value object.
     */
    function Location(obj) {
      obj = obj || {};

      _.extend(this, _.defaults(obj, {
        id:             null,
        name:           '',
        street:         '',
        city:           '',
        province:       '',
        postalCode:     '',
        poBoxNumber:    null,
        countryIsoCode: ''
      }));

    }

    /**
     * Validates the object before creating it, ensuring that it contains the required fields.
     *
     * Should be used to create a location from the object returned by the server.
     */
    Location.create = function(obj) {
      var checks = checkObject(obj);

      if (checks.length) {
        throw new Error('invalid object from server: ' + checks.join(', '));
      }

      return new Location(obj);
    };

    return Location;
  }

});
