/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2017 Canadian BioSample Repository (CBSR)
 */
define(function(require) {
  'use strict';

  var _ = require('lodash');

  EntitySetFactory.$inject = [
    '$q',
    '$log',
    'DomainEntity',
    'DomainError',
    'EntityInfo'
  ];

  function EntitySetFactory($q,
                            $log,
                            DomainEntity,
                            DomainError,
                            EntityInfo) {

    function EntitySet(obj) {
      this.allEntities = false;
      this.entityData = [];

      DomainEntity.call(this, EntitySet.SCHEMA, obj);
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

    EntitySet.isValid = function (obj) {
       return DomainEntity.isValid(EntitySet.SCHEMA, [ EntityInfo.SCHEMA ], obj);
    };

    EntitySet.create = function (obj) {
      var validation = EntitySet.isValid(obj);
      if (!validation.valid) {
        $log.error(validation.message);
        throw new DomainError(validation.message);
      }
      obj.entityData = obj.entityData.map(function (info) {
        return EntityInfo.create(info);
      });
      return new EntitySet(obj);
    };

    EntitySet.asyncCreate = function (obj) {
      var result;

      try {
        result = EntitySet.create(obj);
        return $q.when(result);
      } catch (e) {
        return $q.reject(e);
      }
    };

    EntitySet.prototype.isForAllEntities = function () {
      return this.allEntities;
    };

    EntitySet.prototype.isMemberOf = function (name) {
      var result;
      if (this.allEntities) {
        return true;
      }
      result = _.find(this.entityData, function (info) {
        return info.name === name;
      });
      return !_.isUndefined(result);
    };

    EntitySet.prototype.addEntity = function (id, name) {
      this.entityData.push({ id: id, name: name});
    };

    EntitySet.prototype.removeEntity = function (name) {
      _.remove(this.entityData, function (info) {
        return info.name === name;
      });
    };

    EntitySet.prototype.getEntityIds = function (name) {
      return this.entityData.map(function (entityInfo) {
        return entityInfo.id;
      });
    };

    return EntitySet;
  }

  return EntitySetFactory;

});
