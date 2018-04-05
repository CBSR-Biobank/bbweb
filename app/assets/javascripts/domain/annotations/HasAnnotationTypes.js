/*
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

import _ from 'lodash/core';

/**
 * A mixin for classes that hold {@link domain.annotations.AnnotationType AnnotationTypes}.
 *
 * @exports domain.annotations.HasAnnotationTypesMixin
 */
const HasAnnotationTypesMixin = Base => class extends Base {

  /**
   * Removes an Annotation Type from the parent entity.
   *
   * @return {Promise<object>} returns the parent entity in a plain object returned by the server.
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

export { HasAnnotationTypesMixin }
export default () => {}
