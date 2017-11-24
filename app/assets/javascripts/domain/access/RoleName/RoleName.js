/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2017 Canadian BioSample Repository (CBSR)
 */

/* @ngInject */
function RoleNameFactory($q,
                         $log,
                         biobankApi,
                         EntityName,
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
  class RoleName extends EntityName {

    constructor(obj = {}) {
      super(obj)
    }
  }

  /**
   * Creates a RoleName, but first it validates <code>obj</code> to ensure that it has a valid schema.
   *
   * @param {object} [obj={}] - An initialization object whose properties are the same as the members from
   * this class. Objects of this type are usually returned by the server's REST API.
   *
   * @returns {domain.access.Role} A role created from the given object.
   *
   * @see {@link domain.access.RoleName.asyncCreate|asyncCreate()} when you need to create
   * a role within asynchronous code.
   */
  RoleName.create = function (obj) {
    var validation = EntityName.isValid(obj);
    if (!validation.valid) {
      $log.error(validation.message);
      throw new DomainError(validation.message);
    }
    return new RoleName(obj);
  };

  RoleName.url = function () {
    return DomainEntity.url('access/roles/names');
  };

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
   * @param {int} [options.page=1] If the total results are longer than limit, then page selects which
   *        studies should be returned. If an invalid value is used then the response is an error.
   *
   * @param {int} [options.limit=10] The total number of studies to return per page. The maximum page size
   *        is 10. If a value larger than 10 is used then the response is an error.
   *
   * @param {Array<domain.EntityName>} omit - the list of names to filter out of the result returned
   *        from the server.
   *
   * @returns {Promise} A promise of {@link biobank.domain.PagedResult} with items of type {@link
   *          domain.access.Role}.
   */
  RoleName.list = function (options, omit) {
    const createFunc = (obj) => new RoleName(obj)
    return EntityName.list(RoleName.url(), options, createFunc, omit);
  };

  return RoleName;
}

export default ngModule => ngModule.factory('RoleName', RoleNameFactory)
