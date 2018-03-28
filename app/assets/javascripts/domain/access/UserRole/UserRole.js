/*
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

/* @ngInject */
function UserRoleFactory($q,
                         $log,
                         biobankApi,
                         DomainEntity,
                         ConcurrencySafeEntity,
                         EntityInfo,
                         DomainError) {

  const SCHEMA = {
    id: 'UserRole',
    type: 'object',
    properties: {
      'id':           { 'type': 'string'},
      'version':      { 'type': 'integer', 'minimum': 0},
      'slug':         { 'type': 'string' },
      'name':         { 'type': 'string' },
      'description':  { 'type': [ 'string', 'null' ] },
      'childData':    { 'type': 'array', 'items': { '$ref': 'EntityInfo' } }
    },
    required: [
      'slug',
      'name',
      'childData'
    ]
  };

  /**
   * A Role represents the set of permissions that a user has.
   * @extends domain.access.AccessItem
   * @memberOf domain.access
   */
  class UserRole extends DomainEntity {

    /**
     * @private
     * @return {object} The JSON schema for this class.
     */
    static schema() {
      return SCHEMA;
    }

    /** @private */
    static additionalSchemas() {
      return [ EntityInfo.schema() ];
    }

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
    static create(obj) {
      var validation = UserRole.isValid(obj);
      if (!validation.valid) {
        $log.error(validation.message);
        throw new DomainError(validation.message);
      }
      return new UserRole(obj);
    }

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
    static asyncCreate(obj) {
      var result;

      try {
        result = UserRole.create(obj);
        return $q.when(result);
      } catch (e) {
        return $q.reject(e);
      }
    }

  }

  return UserRole;
}

export default ngModule => ngModule.factory('UserRole', UserRoleFactory)
