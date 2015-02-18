/* global define */
define(['./module', 'underscore'], function(module, _) {
  'use strict';

  module.factory('CollectionEventType', CollectionEventTypeFactory);

  CollectionEventTypeFactory.$inject = [
    'SpecimenGroupSet',
    'AnnotationTypeSet',
    'AnnotationTypeDataSet']
  ;

  /**
   * Factory for collectionEventTypes.
   */
  function CollectionEventTypeFactory(SpecimenGroupSet,
                                      AnnotationTypeSet,
                                      AnnotationTypeDataSet) {


    /**
     * Creates a collection event type object with helper methods.
     *
     * @param {Study} study the study this collection even type belongs to.
     *
     * @param {CollectionEventType} collectionEventType the collection event type returned by the server.
     *
     * @param {SpecimenGroup array} options.studySpecimenGroups all specimen groups for the study. Should be a
     * list returned by the server.
     *
     * @param {AnnotationType array} options.studyAnnotationTypes all the collection event annotation types
     * for the study. Should be a list returned by the server.
     *
     * @param {SpecimenGroupSet} options.studySpecimenGroupSet all specimen groups for the study.
     *
     * @param {AnnotationTypeSet} options.studyAnnotationTypeSet all the collection event annotation types for the
     * study.
     */
    function CollectionEventType(study, collectionEventType, options) {
      var self = this;

      self.specimenGroupData = [];

      _.extend(self, collectionEventType);

      self.isNew               = !self.id;
      self.study               = study;
      self.studyId             = study.id;

      options = options || {};

      if (options.studySpecimenGroups && options.studySpecimenGroupSet) {
        throw new Error('cannot create with both specimenGroups and specimenGroupSet');
      }

      if (options.studySpecimenGroups) {
        options.specimenGroupSet = new SpecimenGroupSet(options.specimenGroups);
      }

      if (options.studySpecimenGroupSet) {
        _.each(self.specimenGroupData, function (sgDataItem) {
          sgDataItem.specimenGroup = options.specimenGroupSet.get(sgDataItem.specimenGroupId);
        });
      }

      self.annotationTypeDataSet = new AnnotationTypeDataSet(
        self.annotationTypeData,
        _.pick(options, 'studyAnnotationTypes', 'studyAnnotationTypeSet'));
      self.annotationTypeData = undefined; // replaced by AnnotationTypeDataSet
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

    CollectionEventType.prototype.getSpecimenGroupsAsString = function () {
      if (this.specimenGroupData.length === 0) {
        return '';
      }
      return _.map(this.specimenGroupData, function (sgItem) {
        return sgItem.specimenGroup.name + ' (' + sgItem.maxCount + ', ' + sgItem.amount +
          ' ' + sgItem.specimenGroup.units + ')';
      }, this).join(', ');
    };

    CollectionEventType.prototype.annotationTypeDataSize = function () {
      return this.annotationTypeDataSet.size();
    };


    CollectionEventType.prototype.getAnnotationTypeData = function (annotationTypeId) {
      return this.annotationTypeDataSet.get(annotationTypeId);
    };

    CollectionEventType.prototype.addAnnotationTypeData = function (atDataItem) {
      this.annotationTypeDataSet.add(atDataItem);
    };

    CollectionEventType.prototype.removeAnnotationTypeData = function (atDataItem) {
      this.annotationTypeDataSet.remove(atDataItem);
    };

    CollectionEventType.prototype.getAnnotationTypesAsString = function () {
      this.annotationTypeDataSet.getAsString();
    };

    /** return constructor function */
    return CollectionEventType;
  }

});
