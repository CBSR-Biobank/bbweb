/* global define */
define(['./module', 'underscore'], function(module, _) {
  'use strict';

  module.factory('CollectionEventType', CollectionEventTypeFactory);

  //CollectionEventTypeFactory.$inject = [''];

  /**
   * Factory for collectionEventTypes.
   */
  function CollectionEventTypeFactory() {

    function CollectionEventType(study,
                                 collectionEventType,
                                 specimenGroups,
                                 annotationTypes) {
      var self = this;

      self.specimenGroupData = [];
      self.annotationTypeData = [];

      _.extend(self, collectionEventType);

      self.isNew               = !self.id;
      self.study               = study;
      self.studyId             = study.id;
      self.specimenGroups      = specimenGroups;
      self.annotationTypes     = annotationTypes;
      self.specimenGroupsById  = _.indexBy(specimenGroups, 'id');
      self.annotationTypesById = _.indexBy(annotationTypes, 'id');
    }

    CollectionEventType.prototype.getSpecimenGroupById = function (sgId) {
      return this.specimenGroupsById[sgId];
    };

    CollectionEventType.prototype.addSpecimenGroupData = function (sgData) {
      this.specimenGroupData.push(sgData);
    };

    CollectionEventType.prototype.removeSpecimenGroupData = function (sgData) {
      if (this.specimenGroupData.length < 1) {
        throw new Error('invalid length for specimen group data');
      }

      var index = this.specimenGroupData.indexOf(sgData);
      if (index > -1) {
        this.specimenGroupData.splice(index, 1);
      }
    };

    CollectionEventType.prototype.addAnnotationTypeData = function (atData) {
      this.annotationTypeData.push(atData);
    };

    CollectionEventType.prototype.removeAnnotationTypeData = function (atData) {
      if (this.annotationTypeData.length < 1) {
        throw new Error('invalid length for annotation type data');
      }

      var index = this.annotationTypeData.indexOf(atData);
      if (index > -1) {
        this.annotationTypeData.splice(index, 1);
      }
    };

    CollectionEventType.prototype.getSpecimenGroupsAsString = function () {
      var self = this;
      return _.map(self.specimenGroupData, function (sgItem) {
        var specimenGroup = self.specimenGroupsById[sgItem.specimenGroupId];
        if (!specimenGroup) {
          throw new Error('specimen group not found: ' + sgItem.specimenGroupId);
        }
        return specimenGroup.name + ' (' + sgItem.maxCount + ', ' + sgItem.amount +
          ' ' + specimenGroup.units + ')';
      }).join(', ');
    };

    CollectionEventType.prototype.getAnnotationTypesAsString = function () {
      var self = this;
      return _.map(self.annotationTypeData, function (atItem) {
        var annotType = self.annotationTypesById[atItem.annotationTypeId];
        if (!annotType) {
          throw new Error('annotation type not found: ' + atItem.annotationTypeId);
        }
        return annotType.name + (atItem.required ? ' (Req)' : ' (N/R)');
      }).join(', ');
    };


    /** return constructor function */
    return CollectionEventType;
  }

});
