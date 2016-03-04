/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(['moment', 'underscore', 'tv4'], function(moment, _, tv4) {
  'use strict';

  AnnotationFactory.$inject = [
    'AnnotationValueType'
  ];

  function AnnotationFactory(AnnotationValueType) {

    var schema = {
      'id': 'Annotation',
      'type': 'object',
      'properties': {
        'annotationType': { 'type': 'string' },
        'stringValue':    { 'type': [ 'string', 'null' ] },
        'numberValue':    { 'type': [ 'string', 'null' ] },
        'selectedValues': { 'type': 'array' }
      },
      'required': [ 'annotationTypeId', 'selectedValues' ]
    };

    /**
     * Please use annotationFactory.create to create annotation objects.
     */
    function Annotation(obj, annotationType) {
      var self = this,
          defaults = {
            annotationTypeId: null,
            stringValue:      null,
            numberValue:      null,
            selectedValues:   []
          };

      obj = obj || {};
      _.extend(this, defaults, _.pick(obj, _.keys(defaults)));

      if (annotationType) {
        self.annotationTypeId = annotationType.uniqueId;
        self.annotationType = annotationType;

        if (!_.contains(AnnotationValueType.values(), annotationType.valueType)) {
          throw new Error('value type is invalid: ' + annotationType.valueType);
        }

        if (_.isUndefined(annotationType.required)) {
          throw new Error('required not assigned');
        }

        self.required = annotationType.required;

        if (annotationType.valueType === AnnotationValueType.SELECT()) {
          if (!annotationType.isMultipleSelect() && !annotationType.isSingleSelect()) {
            throw new Error('invalid value for max count');
          }
        }
      }
    }

    Annotation.isValid = function (obj) {
      return tv4.validate(obj, schema);
    };

    Annotation.validAnnotations = function (annotations) {
      return _.reduce(
        annotations,
        function (memo, annotation) {
          return memo && tv4.validate(annotation, schema);
        },
        true);
    };

    Annotation.getInvalidError = function () {
      return tv4.error;
    };

    Annotation.create = function (obj) {
      if (!Annotation.validate(obj)) {
        throw new Error('invalid object to create from: ' + tv4.error);
      }
      return new Annotation(obj);
    };

    /**
     *
     */
    Annotation.prototype.getAnnotationTypeId = function () {
      if (_.isUndefined(this.annotationType)) {
        throw new Error('annotation type not assigned');
      }
      return this.annotationType.uniqueId;
    };

    /**
     *
     */
    Annotation.prototype.getValueType = function () {
      if (_.isUndefined(this.annotationType)) {
        throw new Error('annotation type not assigned');
      }
      return this.annotationType.valueType;
    };

    /**
     * Returns the label to display for the annotation.
     */
    Annotation.prototype.getLabel = function () {
      if (_.isUndefined(this.annotationType)) {
        throw new Error('annotation type not assigned');
      }
      return this.annotationType.name;
    };

    /**
     * For non requried annotation types, this always returns true. For required annotation types,
     * returns true if the value is not empty.
     */
    Annotation.prototype.isValid = function () {
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
