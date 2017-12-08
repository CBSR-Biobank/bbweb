/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2017 Canadian BioSample Repository (CBSR)
 */

/* @ngInject */
function EntityInfoFactory($q,
                           $log,
                           DomainEntity,
                           DomainError) {

  function EntityInfo(obj) {
    this.id = null;
    this.name = null;

    DomainEntity.call(this, EntityInfo.SCHEMA, obj);
  }

  EntityInfo.SCHEMA = {
    'id': 'EntityInfo',
    'type': 'object',
    'properties': {
      'id':   { 'type': 'string' },
      'slug': { 'type': 'string' },
      'name': { 'type': 'string' }
    },
    'required': [ 'id', 'slug', 'name' ]
  };

  EntityInfo.isValid = function (obj) {
    return DomainEntity.isValid(EntityInfo.SCHEMA, [], obj);
  };

  EntityInfo.create = function (obj) {
    var validation = EntityInfo.isValid(obj);
    if (!validation.valid) {
      $log.error(validation.message);
      throw new DomainError(validation.message);
    }
    return new EntityInfo(obj);
  };

  EntityInfo.asyncCreate = function (obj) {
    var result;

    try {
      result = EntityInfo.create(obj);
      return $q.when(result);
    } catch (e) {
      return $q.reject(e);
    }
  };

  return EntityInfo;
}

export default ngModule => ngModule.factory('EntityInfo', EntityInfoFactory)
