/*
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

import _ from 'lodash';

/**
 * Maintains an array of annotation types.
 *
 * This is a mixin.
 */
/* @ngInject */
function HasAnnotationTypesFactory($q,
                                   biobankApi,
                                   AnnotationType,
                                   DomainError) {

  function HasAnnotationTypes() {}

  HasAnnotationTypes.prototype.validAnnotationTypes = function (annotationTypes) {
    var result;

    if (_.isUndefined(annotationTypes) || (annotationTypes.length <= 0)) {
      // there are no annotation types, nothing to validate
      return true;
    }
    result = _.find(annotationTypes, function (annotType) {
      return !AnnotationType.valid(annotType);
    });

    return _.isUndefined(result);
  };

  /**
   * The entity that includes this mixin needs to implement 'asyncCreate'.
   */
  HasAnnotationTypes.prototype.removeAnnotationType = function (annotationType, url) {
    var self = this,
        found = _.find(self.annotationTypes,  { id: annotationType.id });

    if (!found) {
      return $q.reject(new DomainError('annotation type with ID not present: ' + annotationType.id));
    }
    return biobankApi.del(url);
  };

  return HasAnnotationTypes;
}

export default ngModule => ngModule.factory('HasAnnotationTypes', HasAnnotationTypesFactory)
