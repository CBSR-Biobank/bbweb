/*
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

import _ from 'lodash'

/**
 * A domain entity in the system that allows for optimistic concurrency versioning.
 */
/* @ngInject */
function ConcurrencySafeEntityFactory($q,
                                      DomainEntity,
                                      DomainError,
                                      biobankApi) {

  /**
   * @classdesc Used to manage surrogate identity and optimistic concurrency versioning.
   *
   * @class
   * @memberOf domain
   */
  function ConcurrencySafeEntity(schema, obj) {
    /**
     * The unique ID that identifies an object of this type.
     * @name domain.ConcurrencySafeEntity#id
     * @type string
     * @protected
     */

    /**
     * The current version for the object. Used for optimistic concurrency versioning.
     * @name domain.ConcurrencySafeEntity#version
     * @type number
     * @protected
     */

    /**
     * The date and time, in ISO time format, when this entity was added to the system.
     * @name domain.ConcurrencySafeEntity#timeAdded
     * @type Date
     * @protected
     */

    /**
     * The date and time, in ISO time format, when this entity was last updated.
     * @name domain.ConcurrencySafeEntity#timeModified
     * @type Date
     * @protected
     */

    obj = Object.assign({ id: null, version: 0 }, obj)

    DomainEntity.call(this, schema, obj);

    obj = obj || {};
    if (obj.timeAdded) {
      this.timeAdded = new Date(obj.timeAdded);
    }
    if (obj.timeModified) {
      this.timeModified = new Date(obj.timeModified);
    }
  }

  ConcurrencySafeEntity.prototype = Object.create(DomainEntity.prototype);
  ConcurrencySafeEntity.prototype.constructor = ConcurrencySafeEntity;

  ConcurrencySafeEntity.SCHEMA = {
    'id': 'ConcurrencySafeEntity',
    'type': 'object',
    'properties': {
      'id':           { 'type': 'string'},
      'version':      { 'type': 'integer', 'minimum': 0},
      'timeAdded':    { 'type': 'string'},
      'timeModified': { 'type': [ 'string', 'null' ] }
    },
    'required': [ 'id', 'version', 'timeAdded' ]
  };

  ConcurrencySafeEntity.createDerivedSchema = function ({ id,
                                                          type = 'object',
                                                          properties = {},
                                                          required = [] } = {}) {
    return Object.assign(
      {},
      ConcurrencySafeEntity.SCHEMA,
      {
        'id': id,
        'type': type,
        'properties': Object.assign(
          {},
          ConcurrencySafeEntity.SCHEMA.properties,
          properties
        ),
        'required': ConcurrencySafeEntity.SCHEMA.required.slice().concat(required)
      }
    );
  }

  /**
   * If the object does not have an ID it is new and is not yet present in the system.
   *
   * @returns {boolean}
   */
  ConcurrencySafeEntity.prototype.isNew = function() {
    return (this.id === null);
  };

  /**
   * Checks if `obj` has valid properties to construct an object with the given schemas.
   */
  ConcurrencySafeEntity.isValid = function(schema, additionalSchemas, obj) {
    return DomainEntity.isValid(schema, additionalSchemas, obj);
  };


  /** @protected */
  /*
   * Sends a request to the server to update this entity.
   */
  ConcurrencySafeEntity.prototype.update = function (url, additionalJson = {}) {
    if (_.isNil(this.id)) {
      throw new DomainError('entity has not been persisted');
    }
    const json = _.extend({ expectedVersion: this.version }, additionalJson);
    return biobankApi.post(url, json);
  };

  return ConcurrencySafeEntity;
}

export default ngModule => ngModule.factory('ConcurrencySafeEntity', ConcurrencySafeEntityFactory)
