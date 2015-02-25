define(['./module', 'angular'], function(module, angular) {
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

    Location.prototype.getAddCommand = function() {
      var self = this;
      var cmd = {
        name:           this.name,
        street:         this.street,
        city:           this.city,
        province:       this.province,
        postalCode:     this.postalCode,
        countryIsoCode: this.countryIsoCode
      };

      _.each(['id', 'poBoxNumber'], function(attr){
        if (self[attr] !== null) {
          cmd[attr] = self[attr];
        }
      });

      return cmd;
    };

    return Location;
  }

});
