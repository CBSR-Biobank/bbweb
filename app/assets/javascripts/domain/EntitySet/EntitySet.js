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

  /**
   * A base class for domain entity set objects.
   * @extends domain.DomainEntity
   * @memberOf domain
   */
  class EntitySet extends DomainEntity {

    constructor(obj) {

      /**
       * When `TRUE` this entity set is for all entities in the system.
       *
       * @name domain.EntitySet#allEntities
       * @type {boolean}
       */

      /**
       * When `allEntities` is `FALSE` this array holds the entities this set is for.
       *
       * @name domain.EntitySet#name
       * @type {Array<domain.EntityInfo>}
       */

      super(Object.assign(
        {
          allEntities: false,
          entityData:  []
        },
        obj
      ));
    }

    /**
     * @return {boolean} `TRUE` this entity set is for all entities in the system.
     */
    isForAllEntities() {
      return this.allEntities;
    }

    /**
     * @param {string} name - the name of the entity.
     *
     * @return {boolean} `TRUE` when `allEntities` is `TRUE`, or the entity named `name` is present in the
     * set.
     */
    isMemberOf(name) {
      if (this.allEntities) {
        return true;
      }
      const result = _.find(this.entityData, function (info) {
        return info.name === name;
      });
      return !_.isUndefined(result);
    }

    getEntityIds() {
      return this.entityData.map((entityInfo) =>  entityInfo.id);
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

    /**
     * Creates an object of thsi type, but first it validates #obj to ensure that it has a valid schema.
     *
     * @param {object} obj={} - An initialization object whose properties are the same as the members from
     * this class. Objects of this type are usually returned by the server's REST API.
     *
     * @returns {domain.EntitySet}
     */
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

    /**
     * Creates an object of thsi type, but first it validates #obj to ensure that it has a valid schema.
     *
     * @param {object} obj={} - An initialization object whose properties are the same as the members from
     * this class. Objects of this type are usually returned by the server's REST API.
     *
     * @returns {Promise<domain.EntitySet>}
     */
    static asyncCreate(obj) {
      try {
        return $q.when(EntitySet.create(obj));
      } catch (e) {
        return $q.reject(e);
      }
    }
  }

  return EntitySet;
}

export default ngModule => ngModule.factory('EntitySet', EntitySetFactory)
