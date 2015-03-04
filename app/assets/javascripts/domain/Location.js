define(['./module', 'angular', 'underscore'], function(module, angular, _) {
  'use strict';

  module.factory('Location', LocationFactory);

  LocationFactory.$inject = [];

  /**
   *
   */
  function LocationFactory() {

    /**
     * Location is a value object.
     */
    function Location(obj) {
      obj = obj || {};

      this.id             = obj.id             || null;
      this.name           = obj.name           || '';
      this.street         = obj.street         || '';
      this.city           = obj.city           || '';
      this.province       = obj.province       || '';
      this.postalCode     = obj.postalCode     || '';
      this.poBoxNumber    = obj.poBoxNumber    || null;
      this.countryIsoCode = obj.countryIsoCode || '';
    }

    return Location;
  }

});
