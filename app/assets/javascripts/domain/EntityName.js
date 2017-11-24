/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2017 Canadian BioSample Repository (CBSR)
 */

import _ from 'lodash'

/*
 * Angular factory for Studies.
 */
/* @ngInject */
function EntityNameFactory($q,
                           $log,
                           biobankApi,
                           DomainEntity,
                           DomainError) {

  /**
   * @classdesc A base class for domain entity name objects. A name object is a triplet of: ID, name, and
   * state.
   *
   * Please do not use this constructor. It is meant for internal use.
   *
   * @class
   * @memberOf domain
   * @extends domain.DomainEntity
   *
   * @param {object} [obj={}] - An initialization object whose properties are the same as the members from
   * this class. Objects of this type are usually returned by the server's REST API.
   */
  function EntityName(obj) {

    /**
     * The unique ID that identifies an object of this type.
     * @name domain.EntitNamey#id
     * @type string
     * @protected
     */
    this.id = null;

    /**
     * A short identifying name.
     *
     * @name domain.EntityName#name
     * @type {string}
     */
    this.name = null;

    DomainEntity.call(this, EntityName.SCHEMA, obj);
  }

  EntityName.prototype = Object.create(DomainEntity.prototype);
  EntityName.prototype.constructor = EntityName;

  /**
   * Used for validating plain objects.
   */
  EntityName.SCHEMA = {
    'id': 'EntityName',
    'type': 'object',
    'properties': {
      'id':   { 'type': 'string' },
      'name': { 'type': 'string' }
    },
    'required': [ 'id', 'name' ]
  };

  /**
   * Checks if <tt>obj</tt> has valid properties to construct a {@link domain.EntityName|EntityName}.
   *
   * @param {object} [obj={}] - An initialization object whose properties are the same as the members from
   * this class. Objects of this type are usually returned by the server's REST API.
   *
   * @returns {domain.Validation} The validation passes if <tt>obj</tt> has a valid schema.
   */
  EntityName.isValid = function (obj) {
    return DomainEntity.isValid(EntityName.SCHEMA, [], obj);
  };

  /**
   * Creates a EntityName, but first it validates <code>obj</code> to ensure that it has a valid schema.
   *
   * @param {object} [obj={}] - An initialization object whose properties are the same as the members from
   * this class. Objects of this type are usually returned by the server's REST API.
   *
   * @returns {domain.studies.Entity} A entity created from the given object.
   *
   * @see {@link domain.studies.EntityName.asyncCreate|asyncCreate()} when you need to create
   * a entity within asynchronous code.
   */
  EntityName.create = function (Constructor, obj) {
    var validation = EntityName.isValid(obj);
    if (!validation.valid) {
      $log.error(validation.message);
      throw new DomainError(validation.message);
    }
    return new Constructor(obj);
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
   * @returns {Promise<Array<objects>} A promise containing an array of objcts. The objects are created by
   * calling {@link createFunc}.
   */
  EntityName.list = function (url, options, createFunc, omit) {
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
          const validation = EntityName.isValid(obj);
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
        deferred.reject('invalid entity names from server: ' + e.message);
      }
      return deferred.promise;
    }
  };

  return EntityName;
}

export default ngModule => ngModule.factory('EntityName', EntityNameFactory)
