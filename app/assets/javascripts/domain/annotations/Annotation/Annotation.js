/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(['lodash'], function(_) {
  'use strict';

  AnnotationFactory.$inject = [
    '$log',
    'AnnotationValueType',
    'DomainEntity',
    'DomainError'
  ];

  function AnnotationFactory($log, AnnotationValueType, DomainEntity, DomainError) {

    var schema = {
      'id': 'Annotation',
      'type': 'object',
      'properties': {
        'annotationTypeId': { 'type': 'string' },
        'stringValue':      { 'type': [ 'string', 'null' ] },
        'numberValue':      { 'type': [ 'string', 'null' ] },
        'selectedValues':   { 'type': 'array', items: 'string' }
      },
      'required': [ 'annotationTypeId', 'selectedValues' ]
    };

    /**
     * Please use annotationFactory.create to create annotation objects.
     */
    function Annotation(obj, annotationType) {
      // FIXME: jsdoc for this classes members is needed
      //       annotationTypeId
      //       stringValue
      //       numberValue
      //       selectedValues

      DomainEntity.call(this, schema, obj);

      if (annotationType) {
        this.annotationTypeId = annotationType.id;
        this.annotationType = annotationType;

        if (!_.includes(_.values(AnnotationValueType), annotationType.valueType)) {
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

    Annotation.prototype = Object.create(DomainEntity.prototype);
    Annotation.prototype.constructor = Annotation;

    Annotation.isValid = function (obj) {
      return DomainEntity.isValid(schema, null, obj);
    };

    Annotation.validAnnotations = function (annotations) {
      return _.reduce(
        annotations,
        function (memo, annotation) {
          return memo && Annotation.isValid(annotation);
        },
        true);
    };

    Annotation.create = function (obj) {
      var validation = Annotation.isValid(obj);

      if (!validation.valid) {
        $log.error('invalid object to create from: ' + validation.error);
        throw new DomainError('invalid object to create from: ' + validation.error);
      }
      return new Annotation(obj);
    };

    /**
     *
     */
    Annotation.prototype.getAnnotationTypeId = function () {
      if (_.isUndefined(this.annotationType)) {
        throw new DomainError('annotation type not assigned');
      }
      return this.annotationType.id;
    };

    /**
     *
     */
    Annotation.prototype.getValueType = function () {
      if (_.isUndefined(this.annotationType)) {
        throw new DomainError('annotation type not assigned');
      }
      return this.annotationType.valueType;
    };

    Annotation.prototype.setValue = function (value) {
      this.value = value;
    };

    /**
     * Returns the label to display for the annotation.
     */
    Annotation.prototype.getLabel = function () {
      if (_.isUndefined(this.annotationType)) {
        throw new DomainError('annotation type not assigned');
      }
      return this.annotationType.name;
    };

    /**
     * For non requried annotation types, this always returns true. For required annotation types,
     * returns true if the value is not empty.
     */
    Annotation.prototype.isValueValid = function () {
      var value;

      if (!this.required) {
        return true;
      }

      value = this.getValue();
      if (_.isUndefined(value) || _.isNull(value)) {
        return false;
      }

      if (_.isString(value)) {
        value = value.trim();
        return (value !== '');
      }

      return (value !== null);
    };

    /** return constructor function */
    return Annotation;
  }

  return AnnotationFactory;
});
