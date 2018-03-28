/*
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

var _ = require('lodash');

/* @ngInject */
function EntitySetFactory($q,
                          $log,
                          DomainEntity,
                          DomainError,
                          EntityInfo) {

  const SCHEMA = {
    'id': 'EntitySet',
    'type': 'object',
    'properties': {
      'allEntities': { 'type': 'boolean' },
      'entityData':  { 'type': 'array',  'items': { '$ref': 'EntityInfo' } }
    },
    'required': [ 'allEntities', 'entityData' ]
  };

  class EntitySet extends DomainEntity {

    constructor(obj) {
      super(Object.assign(
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

  return EntitySet;
}

export default ngModule => ngModule.factory('EntitySet', EntitySetFactory)
