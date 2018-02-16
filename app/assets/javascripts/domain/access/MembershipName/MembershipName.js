/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2017 Canadian BioSample Repository (CBSR)
 */

/* @ngInject */
function MembershipNameFactory($q,
                               $log,
                               biobankApi,
                               EntityInfo,
                               DomainEntity,
                               DomainError) {

  /**
   * @classdesc A MembershipName contains the ID, and name for a membership.
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
  class MembershipName extends EntityInfo {

    constructor(obj = {}) {
      super(obj)
    }

    static url(...paths) {
      const allPaths = [ 'access/memberships/names' ].concat(paths);
      return super.url(...allPaths);
    }

    /**
     * Creates a MembershipName, but first it validates <code>obj</code> to ensure that it has a valid schema.
     *
     * @param {object} [obj={}] - An initialization object whose properties are the same as the members from
     * this class. Objects of this type are usually returned by the server's REST API.
     *
     * @returns {domain.access.Membership} A membership created from the given object.
     */
    static create(obj) {
      var validation = EntityInfo.isValid(obj);
      if (!validation.valid) {
        $log.error(validation.message);
        throw new DomainError(validation.message);
      }
      return new MembershipName(obj);
    }

    /**
     * Used to list MembershipNames.
     *
     * <p>A paged API is used to list studies. See below for more details.</p>
     *
     * @param {object} options - The options to use to list studies.
     *
     * @param {string} [options.filter] The filter to use on membership names. Default is empty string.
     *
     * @param {string} [options.sort=name] Studies can be sorted by <code>name</code> or by
     *        <code>state</code>. Values other than these two yield an error. Use a minus sign prefix to sort
     *        in descending order.
     *
     * @returns {Promise} A promise of {@link domain.PagedResult} with items of type {@link
     *          domain.access.Membership}.
     */
    static list(options, omit) {
      return EntityInfo.list(MembershipName.url(), options, omit)
        .then(entities => entities.map(entity => new MembershipName(entity)))
    }
  }

  return MembershipName;
}

export default ngModule => ngModule.factory('MembershipName', MembershipNameFactory)
