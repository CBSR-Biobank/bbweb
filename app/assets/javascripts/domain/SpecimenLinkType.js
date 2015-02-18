/* global define */
define(['./module', 'underscore'], function(module, _) {
  'use strict';

  module.factory('SpecimenLinkType', SpecimenLinkTypeFactory);

  SpecimenLinkTypeFactory.$inject = [
    'SpecimenGroupSet',
    'AnnotationTypeSet',
    'AnnotationTypeDataSet'
  ];

  /**
   * Factory for specimenLinkTypes.
   */
  function SpecimenLinkTypeFactory(SpecimenGroupSet, AnnotationTypeSet, AnnotationTypeDataSet) {

    /**
     * Creates a specimen link type object with helper methods.
     *
     * @param specimenLinkType the specimen link type returned by the server.
     *
     * @param processing type processing type this specimenLinkType belongs. Returned by server.
     *
     * @param specimenGroupSet all specimen groups for the study the processing type belongs to. Should be an
     * instance of SpecimenGroupSet.
     *
     * @param annotationTypes all the specimen link annotation types for the study. Should be an instance of
     * AnnotationTypeSet.
     */
    function SpecimenLinkType(processingType, specimenLinkType, options) {
      var self = this;

      _.extend(self, _.omit(specimenLinkType, 'annotationTypeData'));

      self.isNew              = !self.id;
      self.processingType     = processingType;

      options = options || {};

      if (options.studySpecimenGroups && options.studySpecimenGroupSet) {
        throw new Error('cannot create with both specimenGroups and specimenGroupSet');
      }

      if (options.studySpecimenGroups) {
        options.studySpecimenGroupSet = new SpecimenGroupSet(options.studySpecimenGroups);
      }

      if (options.studySpecimenGroupSet) {
        self.inputGroup  = options.studySpecimenGroupSet.get(self.inputGroupId);
        self.outputGroup = options.studySpecimenGroupSet.get(self.outputGroupId);
      }

      self.annotationTypeDataSet = new AnnotationTypeDataSet(
        specimenLinkType.annotationTypeData,
        _.pick(options, 'studyAnnotationTypes', 'studyAnnotationTypeSet'));

    }

    SpecimenLinkType.prototype.annotationTypeDataSize = function () {
      return this.annotationTypeDataSet.size();
    };

    SpecimenLinkType.prototype.allAnnotationTypeDataIds = function () {
      return this.annotationTypeDataSet.allIds();
    };

    SpecimenLinkType.prototype.getAnnotationTypeData = function (annotationTypeId) {
      return this.annotationTypeDataSet.get(annotationTypeId);
    };

    SpecimenLinkType.prototype.addAnnotationTypeData = function (atDataItem) {
      this.annotationTypeDataSet.add(atDataItem);
    };

    SpecimenLinkType.prototype.removeAnnotationTypeData = function (atDataItem) {
      this.annotationTypeDataSet.remove(atDataItem);
    };

    SpecimenLinkType.prototype.getAnnotationTypesAsString = function () {
      this.annotationTypeDataSet.getAsString();
    };


    /** return constructor function */
    return SpecimenLinkType;
  }

});
