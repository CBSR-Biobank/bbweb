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
   * A base class for {@link domain.ConcurrencySafeEntity|Domain Entities} that have {@link domain.Annotation
   * Annotations}.
   *
   * Maintains an array of annotations.
   */
  class HasAnnotations extends ConcurrencySafeEntity {

    addAnnotation(annotation, url) {
      return super.update(url, annotation.getServerAnnotation());
    }

    /**
     * The entity that includes this mixin needs to implement 'asyncCreate'.
     */
    removeAnnotation(annotation, url) {
      var found = _.find(this.annotations,  { annotationTypeId: annotation.annotationTypeId });

      if (!found) {
        return $q.reject('annotation with annotation type ID not present: ' + annotation.annotationTypeId);
      }
      return biobankApi.del(url);
    }

    setAnnotationTypes(annotationTypes) {
      var differentIds;

      annotationTypes = annotationTypes || [];
      this.annotations = this.annotations || [];

      // make sure the annotations ids match up with the corresponding annotation types
      differentIds = _.difference(_.map(this.annotations, 'annotationTypeId'),
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
