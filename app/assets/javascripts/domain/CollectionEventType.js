/* global define */
define(['./module', 'underscore'], function(module, _) {
  'use strict';

  module.factory('CollectionEventType', CollectionEventTypeFactory);

  CollectionEventTypeFactory.$inject = ['SpecimenGroupSet', 'AnnotationTypeSet'];

  /**
   * Factory for collectionEventTypes.
   */
  function CollectionEventTypeFactory(SpecimenGroupSet, AnnotationTypeSet) {


    /**
     * Creates a collection event type object with helper methods.
     *
     * @param {Study} study the study this collection even type belongs to.
     *
     * @param {CollectionEventType} collectionEventType the collection event type returned by the server.
     *
     * @param {SpecimenGroup array} options.specimenGroups all specimen groups for the study. Should be a list returned by the server.
     *
     * @param {AnnotationType array} options.annotationTypes all the collection event annotation types for the study. Should be a list
     * returned by the server.
     *
     * @param {SpecimenGroupSet} options.specimenGroupSet all specimen groups for the study.
     *
     * @param {AnnotationTypeSet} options.annotationTypeSet all the collection event annotation types for the study.
     */
    function CollectionEventType(study, collectionEventType, options) {
      var self = this;

      self.specimenGroupData  = []; // initialize in case collectionEventType has not attribute
      self.annotationTypeData = []; // initialize in case collectionEventType has not attribute

      _.extend(self, collectionEventType);

      self.isNew               = !self.id;
      self.study               = study;
      self.studyId             = study.id;

      options = options || {};

      if (options.specimenGroups && options.specimenGroupSet) {
        throw new Error('cannot create with both specimenGroups and specimenGroupSet');
      }

      if (options.annotationTypes && options.annotationTypeSet) {
        throw new Error('cannot create with both annotationTypes and annotationTypeSet');
      }

      if (options.specimenGroups) {
        options.specimenGroupSet = new SpecimenGroupSet(options.specimenGroups);
      }

      if (options.annotationTypes) {
        options.annotationTypeSet = new AnnotationTypeSet(options.annotationTypes);
      }

      if (options.specimenGroupSet) {
        _.each(self.specimenGroupData, function (sgDataItem) {
          sgDataItem.specimenGroup = options.specimenGroupSet.get(sgDataItem.specimenGroupId);
        });
      }

      if (options.annotationTypeSet) {
        _.each(self.annotationTypeData, function (atDataItem) {
          atDataItem.annotationType = options.annotationTypeSet.get(atDataItem.annotationTypeId);
        });
      }
    }

    /**
     * Allows adding multiple items with a empty ID (i.e. ''). If id is not empty then duplicate items
     * are not allowed.
     */
    CollectionEventType.prototype.addSpecimenGroupData = function (sgDataItem) {
      if (sgDataItem.id && (sgDataItem.id !== '')) {
        var foundItem = _.find(this.specimenGroupData, function(sgItem) {
          return sgItem.id === sgDataItem.id;
        });
        if (foundItem !== undefined) {
          throw new Error('specimen group data already exists: ' + sgDataItem.id);
        }
      }
      this.specimenGroupData.push(sgDataItem);
    };

    /**
     * Removes a specimen group data item. Note that there can be multiple items with an empty ID.
     *
     * @param {string} sgDataItemId the ID of the specimen group to remove.
     */
    CollectionEventType.prototype.removeSpecimenGroupData = function (sgDataItemId) {
      if (this.specimenGroupData.length < 1) {
        throw new Error('invalid length for specimen group data');
      }

      var foundItem = _.findWhere(this.specimenGroupData, {specimenGroupId: sgDataItemId});
      if (foundItem === undefined) {
        throw new Error('specimen group data with id not foundItem: ' + sgDataItemId);
      }

      this.specimenGroupData = _.without(this.specimenGroupData, foundItem);
    };

    /**
     * Allows adding multiple items with a empty ID (i.e. ''). If id is not empty then duplicate items
     * are not allowed.
     */
    CollectionEventType.prototype.addAnnotationTypeData = function (atDataItem) {
      if (atDataItem.id && (atDataItem.id !== '')) {
        var foundItem = _.find(this.annotationTypeData, function(atItem) {
          return atItem.id === atDataItem.id;
        });
        if (foundItem !== undefined) {
          throw new Error('annotation type data already exists: ' + atDataItem.id);
        }
      }
      this.annotationTypeData.push(atDataItem);
    };

    /**
     * Removes a annotation type data item. Note that there can be multiple items with an empty ID.
     *
     * @param {string} atDataItemId the ID of the annotation type to remove.
     */
    CollectionEventType.prototype.removeAnnotationTypeData = function (atDataItemId) {
      if (this.annotationTypeData.length < 1) {
        throw new Error('invalid length for annotation type data');
      }

      var foundItem = _.findWhere(this.annotationTypeData, {annotationTypeId: atDataItemId});
      if (foundItem === undefined) {
        throw new Error('annotation type data with id not foundItem: ' + atDataItemId);
      }

      this.annotationTypeData = _.without(this.annotationTypeData, foundItem);
    };

    CollectionEventType.prototype.getSpecimenGroupsAsString = function () {
      if (this.specimenGroupData.length === 0) {
        return '';
      }
      return _.map(this.specimenGroupData, function (sgItem) {
        return sgItem.specimenGroup.name + ' (' + sgItem.maxCount + ', ' + sgItem.amount +
          ' ' + sgItem.specimenGroup.units + ')';
      }, this).join(', ');
    };

    CollectionEventType.prototype.getAnnotationTypesAsString = function () {
      if (this.annotationTypeData.length === 0) {
        return '';
      }
      return _.map(this.annotationTypeData, function (atItem) {
        return atItem.annotationType.name + (atItem.required ? ' (Req)' : ' (N/R)');
      }, this).join(', ');
    };


    /** return constructor function */
    return CollectionEventType;
  }

});
