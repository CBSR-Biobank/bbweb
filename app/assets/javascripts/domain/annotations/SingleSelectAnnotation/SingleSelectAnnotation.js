/*
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

import _ from 'lodash';

/* @ngInject */
function SingleSelectAnnotationFactory(Annotation, DomainError) {

  const SCHEMA = Object.assign({},
                               Annotation.schema(),
                               { properties: { value: { type: [ 'array', 'null' ] } } });

  /**
   * An {@link domain.Annotation Annotation} that holds a number value.
   *
   * Please use {@link domain.AnnotationFactory#create AnnotationFactory.create()} to create annotation
   * objects.
   */
  class SingleSelectAnnotation extends Annotation {

    constructor(obj = {}, annotationType) {
      super(obj, annotationType);
      this.valueType = 'SingleSelect';

      if (!_.isUndefined(obj.value)) {
        if (obj.value.length === 0) {
          this.value = null;
        } else if (obj.value.length === 1) {
          this.value = obj.value[0];
        } else {
          throw new DomainError('invalid value for selected values');
        }
      }
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
        selectedValues:   this.value ? [ this.value ] : []
      };
    }

    static schema() {
      return SCHEMA;
    }

    /**
     * Creates an Annotation, but first it validates `obj` to ensure that it has a valid schema.
     *
     * @param {object} obj={} - An initialization object whose properties are the same as the members from
     * this class. Objects of this type are usually returned by the server's REST API.
     *
     * @param {domain.AnnotationType} annotationType - the object containing the type information for this
     * annotation.
     *
     * @returns {domain.SingleSelectAnnotation} An annotation created from the given object.
     */
    static create(obj = {}, annotationType) {
      const clientObj = super.create(obj,
                                     annotationType,
                                     (obj) => ({
                                       annotationTypeId: annotationType.id,
                                       value: obj.selectedValues || []
                                     }));
      return new SingleSelectAnnotation(clientObj, annotationType);
    }

  }

  return SingleSelectAnnotation;
}

export default ngModule => ngModule.factory('SingleSelectAnnotation', SingleSelectAnnotationFactory)
