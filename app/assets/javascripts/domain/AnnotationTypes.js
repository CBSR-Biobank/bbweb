/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(['underscore'], function(_) {
  'use strict';

  AnnotationTypesFactory.$inject = [ 'biobankApi', 'AnnotationType' ];

  /**
   * Maintains an array of annotation types.
   *
   * This is a mixin.
   */
  function AnnotationTypesFactory(biobankApi, AnnotationType) {

    var mixins = {
      validAnnotationTypes: validAnnotationTypes,
      removeAnnotationType: removeAnnotationType
    };

    return mixins;

    //--

    function validAnnotationTypes(annotationTypes) {
      var result;

      if (_.isUndefined(annotationTypes) || (annotationTypes.length <= 0)) {
        // there are no annotation types, nothing to validate
        return true;
      }
      result = _.find(annotationTypes, function (annotType) {
        return !AnnotationType.valid(annotType);
      });

      return _.isUndefined(result);
    }

    function removeAnnotationType(annotationType, url) {
      /* jshint validthis:true */
      var self = this,
          found = _.findWhere(self.annotationTypes,  { uniqueId: annotationType.uniqueId });

      if (!found) {
        throw new Error('annotation type with ID not present: ' + annotationType.uniqueId);
      }

      return biobankApi.del(url).then(function () {
        return self.asyncCreate(
          _.extend(self, {
            version: self.version + 1,
            annotationTypes: _.filter(self.annotationTypes, function(at) {
              return at.uniqueId !== annotationType.uniqueId;
            })
          }));
      });
    }


  }

  return AnnotationTypesFactory;
});
