/* global define */
define(['underscore'], function(_) {
  'use strict';

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
        if (self.inputGroupId) {
          self.inputGroup  = options.studySpecimenGroupSet.get(self.inputGroupId);
        }
        if (self.outputGroupId) {
          self.outputGroup = options.studySpecimenGroupSet.get(self.outputGroupId);
        }
      }

      specimenLinkType = specimenLinkType || {};

      self.annotationTypeDataSet = new AnnotationTypeDataSet(
        specimenLinkType.annotationTypeData,
        _.pick(options, 'studyAnnotationTypes', 'studyAnnotationTypeSet'));
    }

    SpecimenLinkType.prototype.annotationTypeDataSize = function () {
      return this.annotationTypeDataSet.size();
    };

    SpecimenLinkType.prototype.allAnnotationTypeDataIds  = function () {
      return this.annotationTypeDataSet.allIds();
    };

    SpecimenLinkType.prototype.getAnnotationTypeData = function (annotationTypeId) {
      return this.annotationTypeDataSet.get(annotationTypeId);
    };

    SpecimenLinkType.prototype.getAnnotationTypesAsString = function () {
      return this.annotationTypeDataSet.getAsString();
    };

    /**
     * Returns a collection event type as expected by the server.
     */
    SpecimenLinkType.prototype.getServerSpecimenLinkType = function () {
      var serverSpecimenLinkType = _.pick(this,
                                          'processingTypeId',
                                          'id',
                                          'version',
                                          'timeAdded',
                                          'timeModified',
                                          'expectedInputChange',
                                          'expectedOutputChange',
                                          'inputCount',
                                          'outputCount',
                                          'inputGroupId',
                                          'outputGroupId');
      if (this.inputContainerTypeId) {
        serverSpecimenLinkType.inputContainerTypeId = this.inputContainerTypeId;
      }
      if (this.outputContainerTypeId) {
        serverSpecimenLinkType.outputContainerTypeId = this.outputContainerTypeId;
      }
      serverSpecimenLinkType.annotationTypeData = this.annotationTypeDataSet.getAnnotationTypeData();
      return serverSpecimenLinkType;
    };


    /** return constructor function */
    return SpecimenLinkType;
  }

  return SpecimenLinkTypeFactory;
});
