/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
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
    this.id = null;

    /**
     * The current version for the object. Used for optimistic concurrency versioning.
     * @name domain.ConcurrencySafeEntity#version
     * @type number
     * @protected
     */
    this.version = 0;

    /**
     * The date and time, in ISO time format, when this entity was added to the system.
     * @name domain.ConcurrencySafeEntity#timeAdded
     * @type Date
     * @protected
     */
    this.timeAdded = null;

    /**
     * The date and time, in ISO time format, when this entity was last updated.
     * @name domain.ConcurrencySafeEntity#timeModified
     * @type Date
     * @protected
     */
    this.timeModified = null;

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

  /**
   * If the object does not have an ID it is new and is not yet present in the system.
   *
   * @returns {boolean}
   */
  ConcurrencySafeEntity.prototype.isNew = function() {
    return (this.id === null);
  };

  ConcurrencySafeEntity.isValid = function(schema, additionalSchemas, obj) {
    return DomainEntity.isValid(schema, additionalSchemas, obj);
  };

  /** @protected */
  ConcurrencySafeEntity.prototype.asyncCreate = function (obj) { // eslint-disable-line no-unused-vars
    var deferred = $q.defer();
    deferred.reject('the subclass should override this method');
    return deferred.promise;
  };

  /** @protected */
  ConcurrencySafeEntity.prototype.update = function (url, additionalJson) {
    var self = this,
        json;
    if (_.isNil(this.id)) {
      throw new DomainError('entity has not been persisted');
    }
    json = _.extend({ expectedVersion: self.version }, additionalJson || {});
    return biobankApi.post(url, json).then(function(reply) {
      return self.asyncCreate(reply);
    });
  };

  return ConcurrencySafeEntity;
}

export default ngModule => ngModule.factory('ConcurrencySafeEntity', ConcurrencySafeEntityFactory)
