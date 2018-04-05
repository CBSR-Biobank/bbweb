/*
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

/* @ngInject */
function TextAnnotationFactory($log, Annotation) {

  /**
   * An {@link domain.annotations.Annotation Annotation} that holds a string value.
   *
   * Please use {@link domain.AnnotationFactory#create AnnotationFactory.create()} to create annotation
   * objects.
   *
   * @memberOf domain.annotations
   * @extends domain.annotations.Annotation
   */
  class TextAnnotation extends Annotation {

    /** @inheritdoc */
    constructor(obj = {}, annotationType) {
      super(obj, annotationType);
      this.valueType = 'Text';
    }

    /**
     * Assigns a value to this annotation.
     *
     * @param {string} value - the value to assign to this annotation.
     */
    setValue(value) {
      this.value = value;
    }

    getServerAnnotation() {
      return {
        annotationTypeId: this.getAnnotationTypeId(),
        stringValue:      this.value || '',
        selectedValues:   []
      };
    }

    /**
     * Creates an Annotation, but first it validates `obj` to ensure that it has a valid schema.
     *
     * @param {object} obj={} - An initialization object whose properties are the same as the members from
     * this class. Objects of this type are usually returned by the server's REST API.
     *
     * @param {domain.annotations.AnnotationType} annotationType - the object containing the type information
     * for this annotation.
     *
     * @returns {domain.TextAnnotation} An annotation created from the given object.
     */
    static create(obj = {}, annotationType) {
      const clientObj = super.create(obj,
                                     annotationType,
                                     (obj) => ({
                                       annotationTypeId: annotationType.id,
                                       value: obj.stringValue || null
                                     }));
      return new TextAnnotation(clientObj, annotationType);
    }
  }

  return TextAnnotation;
}

export default ngModule => ngModule.factory('TextAnnotation', TextAnnotationFactory)
