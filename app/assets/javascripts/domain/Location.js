define(['angular', 'underscore'], function(angular, _) {
  'use strict';

  LocationFactory.$inject = ['funutils', 'validationService'];

  /**
   *
   */
  function LocationFactory(funutils, validationService) {

    var validateObject = validationService.condition1(
      validationService.validator('must be a map', _.isObject),
      validationService.validator('has the correct keys',
                                  validationService.hasKeys('id',
                                                            'name',
                                                            'street',
                                                            'city',
                                                            'province',
                                                            'postalCode',
                                                            'countryIsoCode')));

    var createObject = funutils.partial(validateObject, _.identity);

    /**
     * Location is a value object.
     */
    function Location(obj) {
      this.id             = null;
      this.name           = '';
      this.street         = '';
      this.city           = '';
      this.province       = '';
      this.postalCode     = '';
      this.poBoxNumber    = null;
      this.countryIsoCode = '';

      obj = obj || {};
      _.extend(this, obj);
    }

    /**
     * Validates the object before creating it, ensuring that it contains the required fields.
     *
     * Should be used to create a location from the object returned by the server.
     */
    Location.create = function(obj) {
      var validatedObj = createObject(obj);

      if (!_.isObject(validatedObj)) {
        throw new Error('invalid object from server: ' + validatedObj);
      }

      return new Location(obj);
    };

    return Location;
  }

  return LocationFactory;
});
