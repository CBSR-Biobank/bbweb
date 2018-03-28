/*
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

import _ from 'lodash'

/*
 * Angular factory for Studies.
 */
/* @ngInject */
function EntityNameAndStateFactory($q,
                                   $log,
                                   biobankApi,
                                   DomainEntity,
                                   DomainError) {

  /*
   * Used for validating plain objects.
   */
  const SCHEMA = {
    'id': 'EntityNameAndState',
    'type': 'object',
    'properties': {
      'id':    { 'type': 'string' },
      'slug':  { 'type': 'string' },
      'name':  { 'type': 'string' },
      'state': { 'type': 'string' }
    },
    'required': [ 'id', 'name', 'state' ]
  };

  /**
   * A base class for domain entity name objects.
   * @extends domain.DomainEntity
   * @memberOf domain
   */
  class EntityNameAndState extends DomainEntity {

    /**
     * An ID for this entity that can be used in URLs.
     *
     * @name domain.EntityNameAndState#slug
     * @type {string}
     */

    /**
     * A short identifying name.
     *
     * @name domain.EntityNameAndState#name
     * @type {string}
     */

    /**
     * This entity's current state.
     *
     * @name domain.EntityNameAndState#state
     * @type {string}
     */

    /** @private */
    static schema() {
      return SCHEMA;
    }

    /** @private */
    static additionalSchemas() {
      return [];
    }

    /**
     * Creates a EntityNameAndState, but first it validates `obj` to ensure that it has a valid schema.
     *
     * @param {object} [obj={}] - An initialization object whose properties are the same as the members from
     * this class. Objects of this type are usually returned by the server's REST API.
     *
     * @returns {domain.EntityNameAndState} A entity created from the given object.
     */
    static create(obj) { // eslint-disable-line no-unused-vars
      throw new DomainError('should be done by derived class');
    }

    /**
     * Used to list entities that support the NamesAndState REST API.
     *
     * @param {Object} options={} - The options to use in the request to the server.
     *
     * @param {string} options.filter='' - the filter to use on entity names.
     *
     * @param {string} options.sort='name' Entities can be sorted by `name` or by `state`. Values other than
     * these two yield an error. Use a minus sign prefix to sort in descending order.
     *
     * @param {Array<domain.EntityNameAndState>} omit=[] the list of names to filter out of the result
     * returned from the server.
     *
     * @returns {Promise<Array<domain.EntityNameAndState>>}
     */
    static list(url, options = {}, omit = []) {
      var params,
          validKeys = [
            'filter',
            'sort',
            'page',
            'limit'
          ];

      params = _.omitBy(_.pick(options, validKeys), value => value.length === 0);

      return biobankApi.get(url, params)
        .then(items => {
          try {
            items.forEach((obj) => {
              const validation = EntityNameAndState.isValid(obj);
              if (!validation.valid) {
                throw new DomainError(validation.message);
              }
            });

            const difference = _.differenceWith(items, omit, (name, omitName) => name.id === omitName.id);
            return $q.when(difference);
          } catch (e) {
            return $q.reject(e.message);
          }
        });
    }
  }

  return EntityNameAndState;
}

export default ngModule => ngModule.factory('EntityNameAndState', EntityNameAndStateFactory)
