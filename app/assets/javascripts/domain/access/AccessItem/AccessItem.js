/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2017 Canadian BioSample Repository (CBSR)
 */

import _ from 'lodash'

/*
 * Angular factory for a AccessItem.
 */
/* @ngInject */
function AccessItemFactory($q,
                           $log,
                           biobankApi,
                           DomainEntity,
                           ConcurrencySafeEntity,
                           EntityInfo,
                           DomainError) {
  class AccessItem extends ConcurrencySafeEntity {

    constructor(schema = AccessItem.SCHEMA, obj = {}) {
      super(schema, obj)

      /**
       * A short identifying name that is unique.
       *
       * @name domain.users.AccessItem#name
       * @type {string}
       */

      /**
       * An optional description that can provide additional details on the name.
       *
       * @name domain.users.AccessItem#description
       * @type {string}
       * @default null
       */

      /**
       * This AccessItem's parents.
       *
       * @name domain.users.AccessItem#parentData
       * @type {Array<EntityInfo>}
       */
      this.parentData = []
      if (obj.parentData) {
        this.parentData = obj.parentData.map(info => new EntityInfo(info))
      }

      /**
       * This AccessItem's children.
       *
       * @name domain.users.AccessItem#userData
       * @type {Array<EntityInfo>}
       */
      this.childData = []
      if (obj.childData) {
        this.childData = obj.childData.map(info => new EntityInfo(info))
      }
    }

  }

  AccessItem.SCHEMA = {
    'id': 'AccessItem',
    'type': 'object',
    'properties': {
      'id':           { 'type': 'string' },
      'version':      { 'type': 'integer', 'minimum': 0 },
      'timeAdded':    { 'type': 'string' },
      'timeModified': { 'type': [ 'string', 'null' ] },
      'name':         { 'type': 'string' },
      'description':  { 'type': [ 'string', 'null' ] },
      'parentData':   { 'type': 'array', 'items': { '$ref': 'EntityInfo' } },
      'childData':    { 'type': 'array', 'items': { '$ref': 'EntityInfo' } }
    },
    'required': [
      'id',
      'version',
      'timeAdded',
      'name',
      'parentData',
      'childData'
    ]
  };

  /**
   * Checks if <tt>obj</tt> has valid properties to construct a {@link domain.access.AccessItem|AccessItem}.
   *
   * @param {object} [obj={}] - An initialization object whose properties are the same as the members from
   * this class. Objects of this type are usually returned by the server's REST API.
   *
   * @returns {domain.Validation} The validation passes if <tt>obj</tt> has a valid schema.
   */
  AccessItem.isValid = function (obj) {
    return ConcurrencySafeEntity.isValid(AccessItem.SCHEMA, [ EntityInfo.SCHEMA ], obj);
  };

  /**
   * Creates a AccessItem, but first it validates <code>obj</code> to ensure that it has a valid schema.
   *
   * @param {object} [obj={}] - An initialization object whose properties are the same as the members from
   * this class. Objects of this type are usually returned by the server's REST API.
   *
   * @returns {domain.access.AccessItem} A AccessItem created from the given object.
   *
   * @see {@link domain.access.AccessItem.asyncCreate|asyncCreate()} when you need to create
   * a AccessItem within asynchronous code.
   */
  AccessItem.create = function (obj) {
    var validation = AccessItem.isValid(obj);
    if (!validation.valid) {
      $log.error(validation.message);
      throw new DomainError(validation.message);
    }
    return new AccessItem(obj);
  };

  /**
   * Creates a AccessItem from a server reply, but first validates that <tt>obj</tt> has a valid schema.
   * <i>Meant to be called from within promise code.</i>
   *
   * @param {object} [obj={}] - An initialization object whose properties are the same as the members from
   * this class. Objects of this type are usually returned by the server's REST API.
   *
   * @returns {Promise<domain.access.AccessItem>} A AccessItem wrapped in a promise.
   *
   * @see {@link domain.access.AccessItem.create|create()} when not creating a AccessItem within asynchronous code.
   */
  AccessItem.asyncCreate = function (obj) {
    var result;

    try {
      result = AccessItem.create(obj);
      return $q.when(result);
    } catch (e) {
      return $q.reject(e);
    }
  };

  /**
   * Retrieves a AccessItem from the server.
   *
   * @param {string} id the ID of the AccessItem to retrieve.
   *
   * @returns {Promise<domain.access.AccessItem>} The AccessItem within a promise.
   */
  AccessItem.get = function(id) {
    return biobankApi.get(AccessItem.url(id)).then(AccessItem.asyncCreate);
  };

  /**
   * Used to list AccessItems.
   *
   * @param {object} options - The options to use.
   *
   * @param {string} options.filter The filter expression to use on AccessItem to refine the list.
   *
   * @param {int} options.page If the total results are longer than limit, then page selects which
   * AccessItems should be returned. If an invalid value is used then the response is an error.
   *
   * @param {int} options.limit The total number of AccessItems to return per page. The maximum page size is
   * 10. If a value larger than 10 is used then the response is an error.
   *
   * @returns {Promise} A promise of {@link biobank.domain.PagedResult} with items of type {@link
   * domain.access.AccessItem}.
   */
  AccessItem.list = function(options) {
    var validKeys = [ 'filter', 'page', 'limit' ],
        params;

    options = options || {};
    params = _.omitBy(_.pick(options, validKeys), (value) => value === '');

    return biobankApi.get(AccessItem.url(), params).then(function(reply) {
      // reply is a paged result
      var deferred = $q.defer();
      try {
        reply.items = reply.items.map((obj) => AccessItem.create(obj));
        deferred.resolve(reply);
      } catch (e) {
        deferred.reject('invalid AccessItems from server');
      }
      return deferred.promise;
    });
  };

  return AccessItem;
}

export default ngModule => ngModule.factory('AccessItem', AccessItemFactory)
