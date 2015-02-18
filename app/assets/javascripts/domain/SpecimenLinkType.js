/* global define */
define(['./module', 'underscore'], function(module, _) {
  'use strict';

  module.factory('SpecimenLinkType', SpecimenLinkTypeFactory);

  SpecimenLinkTypeFactory.$inject = ['SpecimenGroupSet', 'AnnotationTypeSet'];

  /**
   * Factory for specimenLinkTypes.
   */
  function SpecimenLinkTypeFactory(SpecimenGroupSet, AnnotationTypeSet) {

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
    function SpecimenLinkType(specimenLinkType, processingType, options) {
      var self = this;

      _.extend(self, specimenLinkType);

      self.isNew              = !self.id;
      self.processingType     = processingType;

      options = options || {};

      if (options.specimenGroups && options.specimenGroupSet) {
        throw new Error('cannot create with both specimenGroups and specimenGroupSet');
      }

      if (options.specimenGroups) {
        options.specimenGroupSet = new SpecimenGroupSet(options.specimenGroups);
      }

      if (options.specimenGroupSet) {
        self.inputGroup  = options.specimenGroupSet.get(self.inputGroupId);
        self.outputGroup = options.specimenGroupSet.get(self.outputGroupId);
      }

      self.annotationTypeData = new AnnotationTypeData(
        self.annotationTypeData,
        _.pick(options, 'annotationTypes', 'annotationTypeSet'));

    }

    SpecimenLinkType.prototype.addAnnotationTypeData = function (atDataItem) {
      this.annotationTypeData.add(atDataItem);
    };

    SpecimenLinkType.prototype.removeAnnotationTypeData = function (atDataItem) {
      this.annotationTypeData.remove(atDataItem);
    };

    SpecimenLinkType.prototype.getAnnotationTypesAsString = function () {
      this.annotationTypeData.getAsString();
    };


    /** return constructor function */
    return SpecimenLinkType;
  }

});
