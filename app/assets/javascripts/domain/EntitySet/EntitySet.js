/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2017 Canadian BioSample Repository (CBSR)
 */

var _ = require('lodash');

/* @ngInject */
function EntitySetFactory($q,
                          $log,
                          DomainEntity,
                          DomainError,
                          EntityInfo) {

  class EntitySet extends DomainEntity {

    constructor(obj) {
      super(EntitySet.SCHEMA,
            Object.assign(
              {
                allEntities: false,
                entityData:  []
              },
              obj
            )
           );
    }

    isForAllEntities() {
      return this.allEntities;
    }

    isMemberOf(name) {
      if (this.allEntities) {
        return true;
      }
      const result = _.find(this.entityData, function (info) {
        return info.name === name;
      });
      return !_.isUndefined(result);
    }

    addEntity(id, name) {
      this.entityData.push({ id: id, name: name});
    }

    removeEntity(name) {
      _.remove(this.entityData, function (info) {
        return info.name === name;
      });
    }

    getEntityIds() {
      return this.entityData.map(function (entityInfo) {
        return entityInfo.id;
      });
    }

    static isValid(obj) {
      return DomainEntity.isValid(EntitySet.SCHEMA, [ EntityInfo.SCHEMA ], obj);
    }

    static create(obj) {
      const validation = EntitySet.isValid(obj);
      if (!validation.valid) {
        $log.error(validation.message);
        throw new DomainError(validation.message);
      }
      obj.entityData = obj.entityData.map(function (info) {
        return EntityInfo.create(info);
      });
      return new EntitySet(obj);
    }

    static asyncCreate(obj) {
      var result;

      try {
        result = EntitySet.create(obj);
        return $q.when(result);
      } catch (e) {
        return $q.reject(e);
      }
    }
  }

  EntitySet.SCHEMA = {
    'id': 'EntitySet',
    'type': 'object',
    'properties': {
      'allEntities': { 'type': 'boolean' },
      'entityData':  { 'type': 'array',  'items': { '$ref': 'EntityInfo' } }
    },
    'required': [ 'allEntities', 'entityData' ]
  };

  return EntitySet;
}

export default ngModule => ngModule.factory('EntitySet', EntitySetFactory)
