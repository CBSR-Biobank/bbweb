define(['underscore'], function(_) {
  'use strict';

  //AnnotationTypeSetFactory.$inject = [];

  /**
   * A set of annotation types.
   */
  function AnnotationTypeSetFactory() {

    /**
     * @param annotationTypes a list of annotation types returned by the server.
     */
    function AnnotationTypeSet(annotationTypes) {
      if (_.isUndefined(annotationTypes)) {
        throw new Error('annotationTypes is undefined');
      }
      this.annotationTypes = _.indexBy(annotationTypes, 'id');
    }

    /**
     * Returns the annotation type with the given id.
     *
     * @param id the ID of the required wannotation type.
     */
    AnnotationTypeSet.prototype.get = function (sgId) {
      var result = this.annotationTypes[sgId];
      if (!result) {
        throw new Error('annotation type not found: ' + sgId);
      }
      return result;
    };

    return AnnotationTypeSet;
  }

  return AnnotationTypeSetFactory;
});
