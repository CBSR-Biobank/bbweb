/**
 *
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
