/* global define */
define(['underscore'], function(_) {
  'use strict';

  SpecimenLinkTypeFactory.$inject = ['AnnotationTypeDataSet'];

  /**
   * Factory for specimenLinkTypes.
   */
  function SpecimenLinkTypeFactory(AnnotationTypeDataSet) {

    /**
     * Creates a specimen link type object with helper methods.
     *
     * @param specimenLinkType the specimen link type returned by the server.
     *
     * @param processing type processing type this specimenLinkType belongs. Returned by server.
     *
     * @param options.studySpecimenGroups all specimen groups for the study the processing type belongs to.
     *
     * @param options.studyAnnotationTypes all the specimen link annotation types for the study.
     */
    function SpecimenLinkType(processingType, specimenLinkType, options) {
      var self = this;

      specimenLinkType = specimenLinkType || {};
      options = options || {};

      _.extend(self, _.omit(specimenLinkType, 'annotationTypeData'));

      self.isNew              = !self.id;
      self.processingType     = processingType;

      if (options.studySpecimenGroups) {
        if (self.inputGroupId) {
          self.inputGroup = _.findWhere(options.studySpecimenGroups, { id: self.inputGroupId});
        }
        if (self.outputGroupId) {
          self.outputGroup = _.findWhere(options.studySpecimenGroups, { id: self.outputGroupId});
        }
      }

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
