/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(['lodash'], function(_) {
  'use strict';

  NumberAnnotationFactory.$inject = ['Annotation'];

  function NumberAnnotationFactory(Annotation) {

    /**
     * Please use annotationFactory.create to create annotation objects.
     */
    function NumberAnnotation(obj, annotationType) {
      obj = obj || {};
      Annotation.call(this, obj, annotationType);

      // convert number to a float
      if (obj.numberValue && (obj.numberValue.length > 0)) {
        this.value = parseFloat(obj.numberValue);
      }

      this.valueType = 'Number';
    }

    NumberAnnotation.prototype = Object.create(Annotation.prototype);

    NumberAnnotation.prototype.getValue = function () {
      return this.value;
    };

    NumberAnnotation.prototype.getDisplayValue = function () {
      return this.value;
    };

    NumberAnnotation.prototype.getServerAnnotation = function () {
      var value = (this.value) ? this.value.toString() : '';
      return {
        annotationTypeId: this.getAnnotationTypeId(),
        numberValue:      value,
        selectedValues:   []
      };
    };

    NumberAnnotation.prototype.isValueValid = function () {
      if (this.required) {
        return !(_.isNaN(this.value) || _.isUndefined(this.value));
      }

      if (_.isUndefined(this.value)) {
        return true;
      }

      return ! _.isNaN(this.value);
    };

    return NumberAnnotation;
  }

  return NumberAnnotationFactory;
});
