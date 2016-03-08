/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(['angular', 'underscore', 'tv4'], function(angular, _, tv4) {
  'use strict';

  //LocationFactory.$inject = [ ];

  /**
   *
   */
  function LocationFactory() {

    var schema = {
      'id': 'Location',
      'type': 'object',
      'properties': {
        'uniqueId':       { 'type': 'string'},
        'name':           { 'type': 'string'},
        'street':         { 'type': 'string'},
        'city':           { 'type': 'string'},
        'province':       { 'type': 'string'},
        'postalCode':     { 'type': 'string'},
        'poBoxNumber':    { 'type': 'string'},
        'countryIsoCode': { 'type': 'string'}
      },
      'required': [
        'uniqueId',
        'name',
        'street',
        'city',
        'province',
        'postalCode',
        'countryIsoCode',
      ]
    };


    /**
     * Location is a value object.
     */
    function Location(obj) {
      var defaults = {
        uniqueId:       null,
        name:           '',
        street:         '',
        city:           '',
        province:       '',
        postalCode:     '',
        poBoxNumber:    null,
        countryIsoCode: ''
      };

      obj = obj || {};
      _.extend(this, defaults, _.pick(obj, _.keys(defaults)));
    }

    Location.valid = function(obj) {
      return tv4.validate(obj, schema);
    };

    /**
     * Validates the object before creating it, ensuring that it contains the required fields.
     *
     * Should be used to create a location from the object returned by the server.
     */
    Location.create = function(obj) {
      if (!tv4.validate(obj, schema)) {
        console.error('invalid object from server: ' + tv4.error);
        throw new Error('invalid object from server: ' + tv4.error);
      }
      return new Location(obj);
    };

    return Location;
  }

  return LocationFactory;
});
