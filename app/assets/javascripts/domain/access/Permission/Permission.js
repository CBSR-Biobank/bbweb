/*
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

/*
 * Angular factory for a Permission.
 */
/* @ngInject */
function PermissionFactory($q,
                           $log,
                           biobankApi,
                           DomainEntity,
                           ConcurrencySafeEntity,
                           AccessItem,
                           EntityInfo,
                           DomainError) {
  /**
   * A Permission allows a user to perform a certain task in the system.
   * <p>
   * Permissions are assigned to {@link domain.access.Role Role}s.
   *
   * @extends domain.access.AccessItem
   * @memberOf domain.access
   */
  class Permission extends AccessItem {

    constructor(obj = {}) {
      super(Permission.SCHEMA, obj)
    }

    static url(...pathItems) {
      return DomainEntity.url.apply(null, [ 'access/permissions' ].concat(pathItems));
    }

    /*
     * Checks if <tt>obj</tt> has valid properties to construct a {@link domain.access.Permission|Permission}.
     *
     * @param {object} [obj={}] - An initialization object whose properties are the same as the members from
     * this class. Objects of this type are usually returned by the server's REST API.
     *
     * @returns {domain.Validation} The validation passes if <tt>obj</tt> has a valid schema.
     */
    /** @protected */
    static isValid(obj) {
      return ConcurrencySafeEntity.isValid(Permission.SCHEMA, [ EntityInfo.SCHEMA ], obj);
    }

    /**
     * Creates a Permission, but first it validates <code>obj</code> to ensure that it has a valid schema.
     *
     * @param {object} [obj={}] - An initialization object whose properties are the same as the members from
     * this class. Objects of this type are usually returned by the server's REST API.
     *
     * @returns {domain.access.Permission} A Permission created from the given object.
     *
     * @see {@link domain.access.Permission.asyncCreate|asyncCreate()} when you need to create
     * a Permission within asynchronous code.
     */
    static create(obj) {
      var validation = Permission.isValid(obj);
      if (!validation.valid) {
        $log.error(validation.message);
        throw new DomainError(validation.message);
      }
      return new Permission(obj);
    }

    /**
     * Creates a Permission from a server reply, but first validates that <tt>obj</tt> has a valid schema.
     * <i>Meant to be called from within promise code.</i>
     *
     * @param {object} [obj={}] - An initialization object whose properties are the same as the members from
     * this class. Objects of this type are usually returned by the server's REST API.
     *
     * @returns {Promise<domain.access.Permission>} A Permission wrapped in a promise.
     *
     * @see {@link domain.access.Permission.create|create()} when not creating a Permission within
     * asynchronous code.
     */
    static asyncCreate(obj) {
      var result;

      try {
        result = Permission.create(obj);
        return $q.when(result);
      } catch (e) {
        return $q.reject(e);
      }
    }

    /**
     * Retrieves a Permission from the server.
     *
     * @param {string} id the ID of the permission to retrieve.
     *
     * @returns {Promise<domain.access.Permission>} The permission within a promise.
     */
    static get(id) {
      return biobankApi.get(Permission.url(id)).then(Permission.asyncCreate);
    }

  }

  Permission.SCHEMA = AccessItem.createDerivedSchema({ 'id': 'Permission' });

  return Permission;
}

export default ngModule => ngModule.factory('Permission', PermissionFactory)
