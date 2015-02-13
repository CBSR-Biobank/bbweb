/* global define */
define(['./module', 'underscore'], function(module, _) {
  'use strict';

  module.service('CollectionEventType', CollectionEventTypeFactory);

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

    /** return constructor function */
    return CollectionEventType;
  }

});
