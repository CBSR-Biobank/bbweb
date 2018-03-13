/*
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

/**
 *
 */
/* @ngInject */
function LocationFactory($log, DomainEntity, DomainError) {

  /**
   * Represents a location for an entity in the real world.
   *
   * This is a value object.
   *
   * @memberOf domain
   */
  class Location extends DomainEntity {

    constructor(obj) {
      /**
       * Identifies a unique {@link domain.Location Location} for the domain entity using this location.
       *
       * @name domain.Location#id
       * @type {string}
       */

      /**
       * A short identifying name that is unique.
       *
       * @name domain.Location#name
       * @type {string}
       */

      /**
       * The street address for this location.
       *
       * @name domain.Location#street
       * @type {string}
       */

      /**
       * The city the location is in.
       *
       * @name domain.Location#city
       * @type {string}
       */


      /**
       * The province or territory the location is in.
       *
       * @name domain.Location#province
       * @type {string}
       */

       /**
       * The postal code for the location.
       *
       * @name domain.Location#postalCode
       * @type {string}
       */

       /**
       * The postal office box number this location receives mail at.
       *
       * @name domain.Location#poBoxNumber
       * @type {string}
       */

       /**
       * The ISO country code for the country the location is in.
       *
       * @name domain.Location#countryIsoCode
       * @type {string}
       */


      const defaults = {
        id :             null,
        name :           '',
        street :         '',
        city :           '',
        province :       '',
        postalCode :     '',
        poBoxNumber :    null,
        countryIsoCode : ''
      };

      obj = Object.assign({}, defaults, obj)

      super(Location.SCHEMA, obj);
    }

    /**
     * Checks if `obj` has valid properties to construct a {@link domain.Location Location}.
     *
     * @param {object} [obj={}] - An initialization object whose properties are the same as the members from
     * this class. Objects of this type are usually returned by the server's REST API.
     *
     * @returns {domain.Validation} The validation passes if `obj` has a valid schema.
     */
    static isValid(obj) {
      return super.isValid(Location.SCHEMA, null, obj);
    }

    /**
     * Validates the object before creating it, ensuring that it contains the required fields.
     *
     * Should be used to create a location from the object returned by the server.
     *
     * @returns {domain.Location} A location object.
     *
     * @throws {domain.DomainError} When `obj` has an invalid schema. The message contains the path that is
     * invalid.
     */
    static create(obj) {
      var validation = this.isValid(obj);

      if (!validation.valid) {
        $log.error('invalid object from server: ' + validation.message);
        throw new DomainError('invalid object from server: ' + validation.message);
      }
      return new Location(obj);
    }
  }

  Location.SCHEMA = {
    'id': 'Location',
    'type': 'object',
    'properties': {
      'id':             { 'type': 'string'},
      'slug':           { 'type': 'string'},
      'name':           { 'type': 'string'},
      'street':         { 'type': 'string'},
      'city':           { 'type': 'string'},
      'province':       { 'type': 'string'},
      'postalCode':     { 'type': 'string'},
      'poBoxNumber':    { 'type': 'string'},
      'countryIsoCode': { 'type': 'string'}
    },
    'required': [
      'id',
      'slug',
      'name',
      'street',
      'city',
      'province',
      'postalCode',
      'countryIsoCode',
    ]
  };

  return Location;
}

export default ngModule => ngModule.factory('Location', LocationFactory)
