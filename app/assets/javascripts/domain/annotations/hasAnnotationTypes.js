/*
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

import _ from 'lodash/core';

/**
 * Maintains an array of annotation types.
 *
 * This is a mixin.
 */
const hasAnnotationTypesMixin = Base => class extends Base {

  /**
   * The entity that includes this mixin needs to implement 'asyncCreate'.
   */
  removeAnnotationType(annotationType, url) {
    const found = _.find(this.annotationTypes, { id: annotationType.id });

    if (!found) {
      return this.$q.reject(new this.DomainError('annotation type with ID not present: ' +
                                                 annotationType.id));
    }
    return this.biobankApi.del(url);
  }
}

export { hasAnnotationTypesMixin }
export default () => {}
