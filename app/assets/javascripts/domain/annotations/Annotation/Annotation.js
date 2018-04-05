/*
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

import _ from 'lodash'

function AnnotationFactory($log, AnnotationValueType, DomainEntity, DomainError) {

  const SCHEMA = {
    'id': 'Annotation',
    'type': 'object',
    'properties': {
      'annotationTypeId': { 'type': 'string' },
      'stringValue':      { 'type': [ 'string', 'null' ] },
      'numberValue':      { 'type': [ 'string', 'null' ] },
      'selectedValues':   { 'type': 'array', items: 'string' },
      'value':            { 'type': [ 'string', 'null' ] }
    },
    'required': [ 'annotationTypeId' ]
  }

  /**
   * Annotations allow the system to collect custom named and defined pieces of data.
   *
   * The type information for an Annotation is stored in an {@link domain.annotations.AnnotationType
   * AnnotationType}.
   *
   * Please use {@link domain.AnnotationFactory#create AnnotationFactory.create()} to create annotation
   * objects.
   *
   * Objects of this type are used in HTML form code to manage annotation information. It differs from the
   * server representation in order to make setting the information via an HTML simpler.
   *
   * @memberOf domain.annotations
   */
  class Annotation extends DomainEntity {

    /**
     * @param {object} obj={} - An initialization object whose properties are the same as the members from
     *        this class. Objects of this type are usually returned by the server's REST API.
     *
     * @param {domain.annotations.AnnotationType} annotationType - the Annotation Type for this Annotation.
     */
    constructor(obj, annotationType) {
      /**
       * The {@link domain.annotations.AnnotationType| AnnotationType ID} for this annotation's type.
       *
       * @name domain.annotations.Annotation#annotationTypeId
       * @type {string}
       */

      /**
       * The value stored in this annotation.
       *
       * @name domain.annotations.Annotation#value
       * @type {(string|Array<string>)}
       */

      super(obj);

      if (annotationType) {
        this.annotationTypeId = annotationType.id;
        this.annotationType = annotationType;

        if (!Object.values(AnnotationValueType).includes(annotationType.valueType)) {
          throw new DomainError('value type is invalid: ' + annotationType.valueType);
        }

        if (_.isUndefined(annotationType.required)) {
          throw new DomainError('required not defined');
        }

        this.required = annotationType.required;

        if (annotationType.valueType === AnnotationValueType.SELECT) {
          if (!annotationType.isMultipleSelect() && !annotationType.isSingleSelect()) {
            throw new DomainError('invalid value for max count');
          }
        }
      }
    }

    /**
     * @return {string} the ID of the {@link domain.annotations.AnnotationType AnnotationType} for this annotation.
     */
    getAnnotationTypeId() {
      if (_.isUndefined(this.annotationType)) {
        throw new DomainError('annotation type not assigned');
      }
      return this.annotationType.id;
    }

    /**
     * @return {string} the `valueType` of the {@link domain.annotations.AnnotationType AnnotationType} for this
     * annotation.
     */
    getValueType() {
      if (_.isUndefined(this.annotationType)) {
        throw new DomainError('annotation type not assigned');
      }
      return this.annotationType.valueType;
    }

    /**
     * Assigns a value to this annotation.
     */
    setValue() {
      throw new DomainError('derived class must override this method');
    }

    /**
     * @return {object} the value contained in this annotation.
     */
    getValue() {
      return this.value;
    }

    /**
     * @return {object} the value contained in this annotation in a format that can be displayed to the user.
     */
    getDisplayValue() {
      return this.value;
    }

    /**
     * @return {string} the label to display for the annotation.
     */
    getLabel() {
      if (_.isUndefined(this.annotationType)) {
        throw new DomainError('annotation type not assigned');
      }
      return this.annotationType.name;
    }

    /**
     * @return {boolean} For non requried annotation types, this always returns `TRUE`. For required
     * annotation types, returns true if the value is not empty.
     */
    isValueValid() {
      let value;

      if (!this.required) {
        return true;
      }

      value = this.getValue();
      if (_.isNil(value)) {
        return false;
      }

      if (_.isString(value)) {
        value = value.trim();
        return (value !== '');
      }

      return (value !== null);
    }

    /**
     * @private
     * @return {object} The JSON schema for this class.
     */
    static schema() {
      return SCHEMA;
    }

    /** @private */
    static additionalSchemas() {
      return [];
    }

    /** @private */
    static create(obj, annotationType, clientObjCreate) {
      if (typeof obj !== 'object') {
        throw new DomainError('obj is not an object');
      }
      if (_.isNil(annotationType)) {
        throw new DomainError('annotation type is undefined');
      }
      if ((obj.annotationTypeId && annotationType) &&
          (obj.annotationTypeId !== annotationType.id)) {
        throw new DomainError('annotation type IDs dont match');
      }

      const clientObj = clientObjCreate(obj);

      const validation = this.isValid(clientObj);
      if (!validation.valid) {
        $log.error('invalid object to create from: ' + validation.message);
        throw new DomainError('invalid object to create from: ' + validation.message);
      }
      return clientObj;
    }
  }

  /** return constructor function */
  return Annotation;
}

export default ngModule => ngModule.factory('Annotation', AnnotationFactory)
