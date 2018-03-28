/*
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

import _ from 'lodash';

/* @ngInject */
function MultipleSelectAnnotationFactory(Annotation, DomainError) {

  const SCHEMA = Object.assign({},
                               Annotation.schema(),
                               { properties: { value: { type: [ 'array', 'null' ] } } });


  /**
   * An {@link domain.Annotation Annotation} that holds values selected by the user.
   *
   * Please use {@link domain.AnnotationFactory#create AnnotationFactory.create()} to create annotation
   * objects.
   *
   * @memberOf domain
   */
  class MultipleSelectAnnotation extends Annotation {

    constructor(obj = {}, annotationType) {
      super(obj, annotationType);
      this.valueType = 'MultipleSelect';

      if (annotationType && obj.value) {
        this.value = annotationType.options.map((opt) => ({
          name:    opt,
          checked: obj.value.includes(opt)
        }));
      }
    }

    /**
     * @override
     * @return {Array<string>} The selected values.
     */
    getValue() {
      return this.getValueAsArray(this.value);
    }

    /**
     * @override
     * @return {string} the values that have been selected converted a string.
     */
    getDisplayValue() {
      return this.getValue().join(', ');
    }

    setValue(value) {
      if (!Array.isArray(value)) {
        throw new DomainError('value is not an array');
      }
      this.value = value;
    }

    /**
     * @return {object} An object that can be sent to the server to save this annotation.
     */
    getServerAnnotation() {
      return {
        annotationTypeId: this.getAnnotationTypeId(),
        selectedValues:   this.getValueAsArray(this.value)
      };
    }

    /**
     * @return {boolean} For non requried annotation types, this always returns `TRUE`. For required
     * annotation types, returns `TRUE` if at least one value has been selected.
     *
     * @override
     */
    isValueValid() {
      if (!this.required) {
        return true;
      }

      const value = this.getValue();
      return !(_.isNil(value) || (Array.isArray(value) && (value.length === 0)));
    }

    /** @private */
    getValueAsArray(values) {
      return values
        .filter(value => value.checked)
        .map(value => value.name);
    }

    /**
     * @return {boolean} returns `TRUE` if some of the values have the checked field set to true.
     */
    someSelected() {
      return (this.value.find(value => value.checked) !== undefined);
    }

    /** @private */
    static schema() {
      return SCHEMA;
    }

    /**
     * Creates an Annotation, but first it validates `obj` to ensure that it has a valid schema.
     *
     * @param {object} [obj={}] - An initialization object whose properties are the same as the members from
     * this class. Objects of this type are usually returned by the server's REST API.
     *
     * @param {domain.AnnotationType} annotationType - the object containing the type information for this
     * annotation.
     *
     * @returns {domain.MultipleSelectAnnotation} An annotation created from the given object.
     */
    static create(obj = {}, annotationType) {
      const clientObj = super.create(obj,
                                     annotationType,
                                     (obj) => ({
                                       annotationTypeId: annotationType.id,
                                       value: _.isNil(obj.selectedValues) ? [] : obj.selectedValues
                                     }));
      return new MultipleSelectAnnotation(clientObj, annotationType);
   }
  }

  return MultipleSelectAnnotation;
}

export default ngModule => ngModule.factory('MultipleSelectAnnotation', MultipleSelectAnnotationFactory)
