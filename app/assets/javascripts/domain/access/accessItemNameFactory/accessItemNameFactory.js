/**
 * A factory that helps to retrieve {@link domain.access.AccessItem AccessItem}s from the server.
 * @memberOf domain.access
 */
class AccessItemNameFactory {

  constructor(biobankApi,
              DomainEntity,
              DomainError,
              EntityInfo,
              RoleName,
              PermissionName) {
    Object.assign(this, {
      biobankApi,
      DomainEntity,
      DomainError,
      EntityInfo,
      RoleName,
      PermissionName
    })
  }

  /**
   * Used to retrieve {@link domain.access.AccessItem}s from the server.
   *
   * @param {object} options - The options to use.
   *
   * @param {string} options.filter The filter expression to use on user to refine the list.
   *
   * @param {string} options.sort AccessItems can be sorted by 'name'. Values other
   * than these yield an error. Use a minus sign prefix to sort in descending order.
   *
   * @param {int} options.page If the total results are longer than limit, then page selects which
   * accessItems should be returned. If an invalid value is used then the response is an error.
   *
   * @param {int} options.limit The total number of accessItems to return per page. The maximum page size is
   * 10. If a value larger than 10 is used then the response is an error.
   *
   * @param {Array<domain.access.AccessItem>} The items to omit from the result.
   *
   * @returns {Promise<biobank.domain.PagedResult>} A promise containing a {@link biobank.domain.PagedResult}
   * with items of type {@link domain.access.AccessItem}s.
   */
  list(options, omit) {
    const url = this.DomainEntity.url('access/items/names'),
          createFunc = (obj) => {
            switch (obj.accessItemType) {
            case 'role':       return this.RoleName.create(obj);
            case 'permission': return this.PermissionName.create(obj);
            }
            throw new this.DomainError('access item name type is invalid: ' + obj.accessItemType)
          }
    return this.EntityInfo.list(url, options, createFunc, omit)
  }

}

export default ngModule => ngModule.service('accessItemNameFactory', AccessItemNameFactory)
