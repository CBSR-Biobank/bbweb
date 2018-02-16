/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2017 Canadian BioSample Repository (CBSR)
 */

import _ from 'lodash'

/* @ngInject */
function EntityInfoFactory($q,
                           $log,
                           DomainEntity,
                           DomainError,
                           biobankApi) {

  /**
   * A base class for domain entity info objects.
   * @extends domain.DomainEntity
   * @memberOf domain
   */
  class EntityInfo extends DomainEntity {

    constructor(obj) {

      /**
       * An ID for this entity that can be used in URLs.
       *
       * @name domain.EntityInfo#slug
       * @type {string}
       */

      /**
       * A short identifying name.
       *
       * @name domain.EntityInfo#name
       * @type {string}
       */

      super(EntityInfo.SCHEMA, obj);
    }

    static isValid(obj) {
      return super.isValid(EntityInfo.SCHEMA, [], obj);
    }

    static create(obj) {
      var validation = EntityInfo.isValid(obj);
      if (!validation.valid) {
        $log.error(validation.message);
        throw new DomainError(validation.message);
      }
      return new EntityInfo(obj);
    }

    static asyncCreate(obj) {
      var result;

      try {
        result = EntityInfo.create(obj);
        return $q.when(result);
      } catch (e) {
        return $q.reject(e);
      }
    }

    /**
     * Used to list EntityNames.
     *
     * <p>Uses a REST API to retrieve a list of entity names. See below for more details.</p>
     *
     * @param {object} options - The options to use to list studies.
     *
     * @param {string} [options.filter] The filter to use on entity names. Default is empty string.
     *
     * @param {string} [options.sort=name] Studies can be sorted by <code>name</code> or by
     *        <code>state</code>. Values other than these two yield an error. Use a minus sign prefix to sort
     *        in descending order.
     *
     * @param {function} createFunc The function called to create the desired entity name object.
     *
     * @param {Array<domain.EntityName>} omit - the list of names to filter out of the result returned
     *        from the server.
     *
     * @returns {Promise<Array<objects>>} A promise containing an array of objcts. The objects are created by
     * calling {@link createFunc}.
     */
    static list(url, options, omit) {
      var params,
          validKeys = [
            'filter',
            'sort',
            'page',
            'limit'
          ];

      options = options || {};
      params = _.omitBy(_.pick(options, validKeys), function (value) {
        return value === '';
      });

      return biobankApi.get(url, params).then(createFromReply);

      function createFromReply(items) {
        var deferred = $q.defer();
        try {
          items.forEach((obj) => {
            const validation = EntityInfo.isValid(obj);
            if (!validation.valid) {
              throw new DomainError(validation.message);
            }
          });

          const difference = _.differenceWith(items, omit, (name, omitName) => name.id === omitName.id);
          deferred.resolve(difference);
        } catch (e) {
          deferred.reject('invalid entity info from server: ' + e.message);
        }
        return deferred.promise;
      }
    }
  }

  EntityInfo.SCHEMA = {
    'id': 'EntityInfo',
    'type': 'object',
    'properties': {
      'id':   { 'type': 'string' },
      'slug': { 'type': 'string' },
      'name': { 'type': 'string' }
    },
    'required': [ 'id', 'slug', 'name' ]
  };

  return EntityInfo;
}

export default ngModule => ngModule.factory('EntityInfo', EntityInfoFactory)
