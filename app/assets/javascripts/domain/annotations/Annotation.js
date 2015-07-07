/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
/* global define */
define(['moment', 'underscore'], function(moment, _) {
  'use strict';

  AnnotationFactory.$inject = [
    'AnnotationValueType'
  ];

  function AnnotationFactory(AnnotationValueType) {

    /**
     * Please use annotationFactory.create to create annotation objects.
     */
    function Annotation(annotationType, required) {
      var self = this;

      if (annotationType) {
        self.annotationTypeId = annotationType.id;
        self.annotationType = annotationType;

        if (!_.contains(AnnotationValueType.values(), annotationType.valueType)) {
          throw new Error('value type is invalid: ' + annotationType.valueType);
        }

        if (_.isUndefined(annotationType.required)) {
          if (_.isUndefined(required)) {
            throw new Error('required not assigned');
          }
          self.required = required;
        } else {
          self.required = annotationType.required;
        }

        if (annotationType.valueType === AnnotationValueType.SELECT()) {
          if (!annotationType.isMultipleSelect() && !annotationType.isSingleSelect()) {
            throw new Error('invalid value for max count');
          }
        }
      }
    }

    /**
     *
     */
    Annotation.prototype.getAnnotationTypeId = function () {
      if (_.isUndefined(this.annotationType)) {
        throw new Error('annotation type not assigned');
      }
      return this.annotationType.id;
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
