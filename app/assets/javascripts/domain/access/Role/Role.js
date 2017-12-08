/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2017 Canadian BioSample Repository (CBSR)
 */

import _ from 'lodash'

/*
 * Angular factory for a Role.
 */
/* @ngInject */
function RoleFactory($q,
                     $log,
                     biobankApi,
                     DomainEntity,
                     ConcurrencySafeEntity,
                     AccessItem,
                     EntityInfo,
                     DomainError) {
  class Role extends AccessItem {

    constructor(obj = {}) {
      super(Role.SCHEMA, obj)

      /**
       * The users that have this role.
       *
       * @name domain.users.Role#userData
       * @type {Array<EntityInfo>}
       */
      this.userData = []
      if (obj.userData) {
        this.userData = obj.userData.map(info => new EntityInfo(info))
      }
    }

    /**
     * Creates a Role from a server reply but first validates that it has a valid schema.
     *
     * <i>A wrapper for {@link domain.access.Role#asyncCreate}.</i>
     *
     * @see {@link domain.ConcurrencySafeEntity#update}
     */
    asyncCreate(obj) {
      return Role.asyncCreate(obj);
    }

    /**
     * Adds a role.
     *
     * @return {Promise<domain.access.Role>} A promise containing the role that was created.
     */
    add() {
      const getId = (entityInfo) => entityInfo.id,
            json     = _.pick(this, 'name', 'description')
      json.userIds   = this.userData.map(getId);
      json.parentIds = this.parentData.map(getId);
      json.childIds  = this.childData.map(getId);
      return biobankApi.post(Role.url(), json).then(Role.asyncCreate);
    }

    /**
     * Removes a role.
     *
     * @return {Promise<boolean>} A promise with boolean TRUE if successful.
     */
    remove() {
      var url;
      if (_.isNil(this.id)) {
        throw new DomainError('role has not been persisted');
      }
      url = Role.url(this.id, this.version);
      return biobankApi.del(url);
    }

    /**
     * Updates the name.
     *
     * @param {String} name - The new name to give this role.
     *
     * @returns {Promise<domain.access.Role>} A promise containing the role with the new name.
     */
    updateName(name) {
      return this.update(Role.url('name', this.id), { name: name });
    }

    /**
     * Updates the description.
     *
     * @param {String} name - The new description to give this role.
     *
     * @returns {Promise<domain.access.Role>} A promise containing the role with the new name.
     */
    updateDescription(description) {
      return this.update(Role.url('description', this.id),
                         description ? { description: description } : {});
    }

    addUser(id) {
      return this.update(Role.url('user', this.id), { userId: id });
    }

    removeUser(id) {
      if (_.isNil(this.id)) {
        throw new DomainError('role has not been persisted');
      }
      const url = Role.url('user', this.id, this.version, id);
      return biobankApi.del(url).then(Role.asyncCreate);
    }

    addParentRole(id) {
      return this.update(Role.url('parent', this.id), { parentRoleId: id });
    }

    removeParentRole(id) {
      if (_.isNil(this.id)) {
        throw new DomainError('role has not been persisted');
      }
      const url = Role.url('parent', this.id, this.version, id);
      return biobankApi.del(url).then(Role.asyncCreate);
    }

    addChildRole(id) {
      return this.update(Role.url('child', this.id), { childRoleId: id });
    }

    removeChildRole(id) {
      if (_.isNil(this.id)) {
        throw new DomainError('role has not been persisted');
      }
      const url = Role.url('child', this.id, this.version, id);
      return biobankApi.del(url).then(Role.asyncCreate);
    }

  }

  Role.SCHEMA = AccessItem.createDerivedSchema({
    id: 'Role',
    properties: {
      'userData': { 'type': 'array', 'items': { '$ref': 'EntityInfo' } }
    },
    required: [ 'userData' ]
  });

  Role.url = function (...pathItems) {
    return DomainEntity.url.apply(null, [ 'access/roles' ].concat(pathItems));
  };

  /**
   * Checks if <tt>obj</tt> has valid properties to construct a {@link domain.access.Role|Role}.
   *
   * @param {object} [obj={}] - An initialization object whose properties are the same as the members from
   * this class. Objects of this type are usually returned by the server's REST API.
   *
   * @returns {domain.Validation} The validation passes if <tt>obj</tt> has a valid schema.
   */
  Role.isValid = function (obj) {
    return ConcurrencySafeEntity.isValid(Role.SCHEMA, [ EntityInfo.SCHEMA ], obj);
  };

  /**
   * Creates a Role, but first it validates <code>obj</code> to ensure that it has a valid schema.
   *
   * @param {object} [obj={}] - An initialization object whose properties are the same as the members from
   * this class. Objects of this type are usually returned by the server's REST API.
   *
   * @returns {domain.access.Role} A Role created from the given object.
   *
   * @see {@link domain.access.Role.asyncCreate|asyncCreate()} when you need to create
   * a Role within asynchronous code.
   */
  Role.create = function (obj) {
    var validation = Role.isValid(obj);
    if (!validation.valid) {
      $log.error(validation.message);
      throw new DomainError(validation.message);
    }
    return new Role(obj);
  };

  /**
   * Creates a Role from a server reply, but first validates that <tt>obj</tt> has a valid schema.
   * <i>Meant to be called from within promise code.</i>
   *
   * @param {object} [obj={}] - An initialization object whose properties are the same as the members from
   * this class. Objects of this type are usually returned by the server's REST API.
   *
   * @returns {Promise<domain.access.Role>} A Role wrapped in a promise.
   *
   * @see {@link domain.access.Role.create|create()} when not creating a Role within asynchronous code.
   */
  Role.asyncCreate = function (obj) {
    var result;

    try {
      result = Role.create(obj);
      return $q.when(result);
    } catch (e) {
      return $q.reject(e);
    }
  };

  /**
   * Retrieves a Role from the server.
   *
   * @param {string} id the ID of the role to retrieve.
   *
   * @returns {Promise<domain.access.Role>} The role within a promise.
   */
  Role.get = function(id) {
    return biobankApi.get(Role.url(id)).then(Role.asyncCreate);
  };

  /**
   * Used to list Roles.
   *
   * @param {object} options - The options to use.
   *
   * @param {string} options.filter The filter expression to use on role to refine the list.
   *
   * @param {int} options.page If the total results are longer than limit, then page selects which
   * roles should be returned. If an invalid value is used then the response is an error.
   *
   * @param {int} options.limit The total number of roles to return per page. The maximum page size is
   * 10. If a value larger than 10 is used then the response is an error.
   *
   * @returns {Promise} A promise of {@link biobank.domain.PagedResult} with items of type {@link
   * domain.access.Role}.
   */
  Role.list = function(options) {
    var validKeys = [ 'filter', 'page', 'limit' ],
        params;

    options = options || {};
    params = _.omitBy(_.pick(options, validKeys), (value) => value === '');

    return biobankApi.get(Role.url(), params).then(function(reply) {
      // reply is a paged result
      var deferred = $q.defer();
      try {
        reply.items = reply.items.map((obj) => Role.create(obj));
        deferred.resolve(reply);
      } catch (e) {
        deferred.reject('invalid roles from server');
      }
      return deferred.promise;
    });
  };

  return Role;
}

export default ngModule => ngModule.factory('Role', RoleFactory)
