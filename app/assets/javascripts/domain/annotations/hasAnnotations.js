/*
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

import _ from 'lodash';

/* @ngInject */
function hasAnnotationsFactory($q,
                               biobankApi,
                               ConcurrencySafeEntity,
                               DomainError,
                               Annotation,
                               AnnotationType,
                               annotationFactory) {

  /**
   * A base class for {@link domain.annotations.ConcurrencySafeEntity|Domain Entities} that contain {@link
   * domain.annotations.Annotation Annotations}.
   *
   * Maintains an array of annotations.
   *
   * @memberOf domain.annotations
   */
  class HasAnnotations extends ConcurrencySafeEntity {

    /**
     * Adds an annotation to the parent entity.
     *
     * @return {Promise<object>} returns the parent entity in a plain object returned by the server.
     */
    addAnnotation(annotation, url) {
      return super.update(url, annotation.getServerAnnotation());
    }

    /**
     * Removes an annotation from the parent entity.
     *
     * @return {Promise<object>} returns the parent entity in a plain object returned by the server.
     */
    removeAnnotation(annotation, url) {
      var found = _.find(this.annotations,  { annotationTypeId: annotation.annotationTypeId });

      if (!found) {
        return $q.reject('annotation with annotation type ID not present: ' + annotation.annotationTypeId);
      }
      return biobankApi.del(url);
    }

    /**
     * Assigns the {@link domain.annotations.AnnotationType AnnotationType} to the parent entity and converts
     * the annotations to the matching objects derived from {@link domain.annotations.Annotation Annotation}.
     *
     * @see domain.annotations.DateTimeAnnotation
     * @see domain.annotations.MultipleSelectAnnotation
     * @see domain.annotations.NumberAnnotation
     * @see domain.annotations.SingleSelectAnnotation
     * @see domain.annotations.TextAnnotation
     *
     * @return {undefined}
     */
    setAnnotationTypes(annotationTypes) {
      annotationTypes = annotationTypes || [];
      this.annotations = this.annotations || [];

      // make sure the annotations ids match up with the corresponding annotation types
      const differentIds = _.difference(_.map(this.annotations, 'annotationTypeId'),
                                        _.map(annotationTypes, 'id'));

      if (differentIds.length > 0) {
        throw new DomainError('annotation types not found: ' + differentIds);
      }

      this.annotations = annotationTypes.map((annotationType) => {
        var jsonAnnotationMaybe = _.find(this.annotations, { annotationTypeId: annotationType.id });

        if ((jsonAnnotationMaybe instanceof Annotation) &&
            (jsonAnnotationMaybe.annotationType) &&
            (jsonAnnotationMaybe.annotationType instanceof AnnotationType)) {
          // annotation was already converted to Annotation or sub class
          return jsonAnnotationMaybe;
        }

        // undefined is valid input
        return annotationFactory.create(jsonAnnotationMaybe, annotationType);
      });
    }
  }

  return HasAnnotations
}

export default ngModule => ngModule.factory('HasAnnotations', hasAnnotationsFactory)
