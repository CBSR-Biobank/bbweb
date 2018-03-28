/*
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

/**
 *
 */
/* @ngInject */
function LocationFactory($log, DomainEntity, DomainError) {

  const SCHEMA = {
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

      super(Object.assign(
        {
          id :             null,
          name :           '',
          street :         '',
          city :           '',
          province :       '',
          postalCode :     '',
          poBoxNumber :    null,
          countryIsoCode : ''
        },
        obj));
    }

    /**
     * @return {object} The JSON schema for this class.
     */
    static schema() {
      return SCHEMA;
    }

    /** @private */
    static additionalSchemas() {
      return [];
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

  return Location;
}

export default ngModule => ngModule.factory('Location', LocationFactory)
