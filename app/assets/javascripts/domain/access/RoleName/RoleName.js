/*
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

/* @ngInject */
function RoleNameFactory($q,
                         $log,
                         biobankApi,
                         EntityInfo,
                         DomainEntity,
                         DomainError) {

  /**
   * @classdesc A RoleName contains the ID, and name for a role.
   *
   * Please do not use this constructor. It is meant for internal use.
   *
   * @class
   * @memberOf domain.access
   * @extends domain.DomainEntity
   *
   * @param {object} [obj={}] - An initialization object whose properties are the same as the members from
   * this class. Objects of this type are usually returned by the server's REST API.
   */
  class RoleName extends EntityInfo {

    constructor(obj = {}) {
      super(obj)
    }

    static url(...paths) {
      const allPaths = [ 'access/roles/names' ].concat(paths);
      return super.url(...allPaths);
    }

    /**
     * Creates a RoleName, but first it validates <code>obj</code> to ensure that it has a valid schema.
     *
     * @param {object} [obj={}] - An initialization object whose properties are the same as the members from
     * this class. Objects of this type are usually returned by the server's REST API.
     *
     * @returns {domain.access.Role} A role created from the given object.
     */
    static create(obj) {
      const validation = EntityInfo.isValid(obj);
      if (!validation.valid) {
        $log.error(validation.message);
        throw new DomainError(validation.message);
      }
      return new RoleName(obj);
    }

    /**
     * Used to list RoleNames.
     *
     * <p>A paged API is used to list studies. See below for more details.</p>
     *
     * @param {object} options - The options to use to list studies.
     *
     * @param {string} [options.filter] The filter to use on role names. Default is empty string.
     *
     * @param {string} [options.sort=name] Studies can be sorted by <code>name</code> or by
     *        <code>state</code>. Values other than these two yield an error. Use a minus sign prefix to sort
     *        in descending order.
     *
     * @param {Array<domain.RoleName>} omit - the list of names to filter out of the result returned
     *        from the server.
     *
     * @returns {Promise<Array<domain.access.RoleName>>}
     */
    static list(options, omit) {
      return EntityInfo.list(RoleName.url(), options, omit)
        .then(entities => entities.map(entity => new RoleName(entity)))
    }
  }

  return RoleName;
}

export default ngModule => ngModule.factory('RoleName', RoleNameFactory)
