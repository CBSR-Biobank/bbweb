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

  function EntityInfo(obj) {
    this.id = null;
    this.name = null;

    DomainEntity.call(this, EntityInfo.SCHEMA, obj);
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

  EntityInfo.isValid = function (obj) {
    return DomainEntity.isValid(EntityInfo.SCHEMA, [], obj);
  };

  EntityInfo.create = function (obj) {
    var validation = EntityInfo.isValid(obj);
    if (!validation.valid) {
      $log.error(validation.message);
      throw new DomainError(validation.message);
    }
    return new EntityInfo(obj);
  };

  EntityInfo.asyncCreate = function (obj) {
    var result;

    try {
      result = EntityInfo.create(obj);
      return $q.when(result);
    } catch (e) {
      return $q.reject(e);
    }
  };

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
  EntityInfo.list = function (url, options, createFunc, omit) {
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
        const names = items.map((obj) => {
          const validation = EntityInfo.isValid(obj);
          if (!validation.valid) {
            throw new DomainError(validation.message);
          }
          return createFunc(obj);
        });

        const difference = _.differenceWith(names, omit, function (name, omitName) {
          return name.id === omitName.id;
        });
        deferred.resolve(difference);
      } catch (e) {
        deferred.reject('invalid entity info from server: ' + e.message);
      }
      return deferred.promise;
    }
  };

  return EntityInfo;
}

export default ngModule => ngModule.factory('EntityInfo', EntityInfoFactory)
