/* global define */
define(['./module', 'underscore'], function(module, _) {
  'use strict';

  module.factory('SpecimenLinkType', SpecimenLinkTypeFactory);

  //SpecimenLinkTypeFactory.$inject = [''];

  /**
   * Factory for specimenLinkTypes.
   */
  function SpecimenLinkTypeFactory() {

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
    function SpecimenLinkType(specimenLinkType,
                              processingType,
                              specimenGroupSet,
                              annotationTypeSet) {
      var self = this;

      _.extend(self, specimenLinkType);

      self.isNew              = !self.id;
      self.processingType     = processingType;
      self.inputGroup         = specimenGroupSet.get(self.inputGroupId);
      self.outputGroup        = specimenGroupSet.get(self.outputGroupId);

      _.each(self.annotationTypeData, function(atData) {
        atData.annotationType = annotationTypeSet.get(atData.annotationTypeId);
      });
    }

    SpecimenLinkType.prototype.addAnnotationTypeData = function (atData) {
      this.annotationTypeData.push(atData);
    };

    SpecimenLinkType.prototype.removeAnnotationTypeData = function (atData) {
      if (this.annotationTypeData.length < 1) {
        throw new Error('invalid length for annotation type data');
      }

      var index = this.annotationTypeData.indexOf(atData);
      if (index > -1) {
        this.annotationTypeData.splice(index, 1);
      }
    };

    SpecimenLinkType.prototype.getAnnotationTypesAsString = function () {
      var self = this;
      return _.map(self.annotationTypeData, function (atItem) {
        return atItem.annotationType.name + (atItem.required ? ' (Req)' : ' (N/R)');
      }).join(', ');
    };


    /** return constructor function */
    return SpecimenLinkType;
  }

});
