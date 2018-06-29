/*
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

var _ = require('lodash');

/* @ngInject */
function DateTimeAnnotationFactory(Annotation, timeService) {

  /**
   * An {@link domain.annotations.Annotation Annotation} that holds a Date value.
   *
   * Please use {@link domain.AnnotationFactory#create AnnotationFactory.create()} to create annotation
   * objects.
   *
   * @memberOf domain.annotations
   * @extends domain.annotations.Annotation
   */
  class DateTimeAnnotation extends Annotation {

    /** @inheritdoc */
    constructor(obj = {}, annotationType) {
      super(obj, annotationType);
      this.valueType = 'DateTime';

      if (obj.value) {
        this.value = new Date(obj.value);
      } else {
        this.value = null;
      }
    }

    /**
     * @return {Date} The date stored in this annotation.
     */
    getValue() {
      return _.isNull(this.value) ? null :timeService.dateToDisplayString(this.value);
    }

    /**
     * @return {Date} The date stored in this annotation.
     */
    getDisplayValue() {
      return this.getValue();
    }

    /**
     * Assigns a value to this annotation.
     *
     * @param {Date} value - the value to assign to this annotation.
     */
    setValue(value) {
      if (typeof value === 'string') {
        this.value = new Date(value);
      } else {
        this.value = value;
      }
    }

    /**
     * @return {object} An object that can be sent to the server to save this annotation.
     */
    getServerAnnotation() {
      return {
        annotationTypeId: this.getAnnotationTypeId(),
        stringValue:      this.value ? timeService.dateAndTimeToUtcString(this.value) : '',
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
     * @returns {domain.DateTimeAnnotation} An annotation created from the given object.
     */
    static create(obj = {}, annotationType) {
      const clientObj = super.create(obj,
                                     annotationType,
                                     (obj) => ({
                                       annotationTypeId: annotationType.id,
                                       value: obj.stringValue || null
                                     }));
      return new DateTimeAnnotation(clientObj, annotationType);
   }

  }

  return DateTimeAnnotation;
}

export default ngModule => ngModule.factory('DateTimeAnnotation', DateTimeAnnotationFactory)
