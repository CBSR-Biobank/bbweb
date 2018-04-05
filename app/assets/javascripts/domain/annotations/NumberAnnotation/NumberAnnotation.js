/*
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

import _ from 'lodash';

/* @ngInject */
function NumberAnnotationFactory(Annotation) {

  /**
   * An {@link domain.annotations.Annotation Annotation} that holds a number value.
   *
   * Please use {@link domain.AnnotationFactory#create AnnotationFactory.create()} to create annotation
   * objects.
   *
   * @memberOf domain.annotations
   * @extends domain.annotations.Annotation
   */
  class NumberAnnotation extends Annotation {

    /** @inheritdoc */
    constructor(obj = {}, annotationType) {
      super(obj, annotationType);
      this.valueType = 'Number';

      // convert number to a float
      if (obj.value && (obj.value.length > 0)) {
        this.value = parseFloat(obj.value);
      }
    }

    /**
     * Assigns a value to this annotation.
     *
     * @param {int|float} value - the value to assign to this annotation.
     */
    setValue(value) {
      this.value = value;
    }

    getServerAnnotation() {
      var value = (this.value) ? this.value.toString() : '';
      return {
        annotationTypeId: this.getAnnotationTypeId(),
        numberValue:      value,
        selectedValues:   []
      };
    }

    isValueValid() {
      const isANumber = this.isNumeric(this.value);
      if (this.required) {
        return isANumber;
      }

      return isANumber || _.isNull(this.value);
    }

    isNumeric(n) {
      return !isNaN(parseFloat(n)) && isFinite(n);
    }

    /**
     * Creates an Annotation, but first it validates `obj` to ensure that it has a valid schema.
     *
     * @param {object} obj={} - An initialization object whose properties are the same as the members from
     * this class. Objects of this type are usually returned by the server's REST API.
     *
     * @param {domain.annotations.AnnotationType} annotationType - the object containing the type information for this
     * annotation.
     *
     * @returns {domain.NumberAnnotation} An annotation created from the given object.
     */
    static create(obj = {}, annotationType) {
      const clientObj = super.create(obj,
                                     annotationType,
                                     (obj) => ({
                                       annotationTypeId: annotationType.id,
                                       value: obj.numberValue || null
                                     }));
      return new NumberAnnotation(clientObj, annotationType);
    }
  }

  return NumberAnnotation;
}

export default ngModule => ngModule.factory('NumberAnnotation', NumberAnnotationFactory)
