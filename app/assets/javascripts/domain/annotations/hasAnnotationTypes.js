/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(['lodash'], function(_) {
  'use strict';

  hasAnnotationTypesFactory.$inject = [
    'biobankApi',
    'AnnotationType',
    'DomainError'
  ];

  /**
   * Maintains an array of annotation types.
   *
   * This is a mixin.
   */
  function hasAnnotationTypesFactory(biobankApi,
                                     AnnotationType,
                                     DomainError) {

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

    /**
     * The entity that includes this mixin needs to implement 'asyncCreate'.
     */
    function removeAnnotationType(annotationType, url) {
      /* jshint validthis:true */
      var self = this,
          found = _.find(self.annotationTypes,  { uniqueId: annotationType.uniqueId });

      if (!found) {
        throw new DomainError('annotation type with ID not present: ' + annotationType.uniqueId);
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

  return hasAnnotationTypesFactory;
});
