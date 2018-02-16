/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */

import _ from 'lodash'
import tv4 from 'tv4'

/*
 * AngularJS factory.
 *
 */
/* @ngInject */
function DomainEntityFactory($log, UrlService) {

  /**
   * An abstract class for an entity in the domain.
   * @memberOf domain
   */
  class DomainEntity {

    /**
     *
     * @param {object} schema - the tv4 schema for this class.
     *
     * @param {object} [obj={}] - An initialization object whose properties are the same as the members from
     * the derived class. Objects of this type are usually returned by the server's REST API.
     */
    constructor(schema, obj = {}) {
      /**
       * The unique ID that identifies an object of this type.
       * @name domain.DomainEntity#id
       * @type string
       * @protected
       */

      Object.assign(this, _.pick(obj, _.keys(schema.properties)));
    }

    /**
     *
     * @param {object} schema - the tv4 schema for this class.
     *
     * @param {object} additionalSchemas - the tv4 schemas this class depends on.
     *
     * @param {object} obj - An initialization object whose properties are the same as the members from
     * the derived class. Objects of this type are usually returned by the server's REST API.
     *
     * @return {domain.Validation} A validation object.
     */
    static isValid(schema, additionalSchemas, obj) {
      if (additionalSchemas) {
        additionalSchemas.forEach(schema => {
          tv4.addSchema(schema.id, schema);
        });
      }

      if (!tv4.validate(obj, schema)) {
        $log.error('validation error:', schema.id, tv4.error.dataPath + ':' + tv4.error.message);
        return { valid: false, message: tv4.error.dataPath + ':' + tv4.error.message};
      }
      return { valid: true, message: null };
    }

    /**
     * A utility function that builds a URL to the REST API for the server.
     *
     * @param {string[]} paths - The paths to the REST API URL.
     *
     * @return {string} A URL to the server's REST API joined with slashes.
     */
    static url(...paths) {
      return UrlService.url.apply(UrlService, paths);
    }
  }

  return DomainEntity;
}

export default ngModule => ngModule.factory('DomainEntity', DomainEntityFactory)
