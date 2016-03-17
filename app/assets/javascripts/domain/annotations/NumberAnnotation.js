/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(['underscore'], function(_) {
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
      if (!_.isUndefined(obj.numberValue)) {
        this.value = parseFloat(obj.numberValue);
      }
    }

    NumberAnnotation.prototype = Object.create(Annotation.prototype);

    NumberAnnotation.prototype.getValue = function () {
      return this.value;
    };

    NumberAnnotation.prototype.getServerAnnotation = function () {
      var value;

      if (!_.isUndefined(this.value) && !_.isNaN(this.value)) {
        value = this.value.toString();
      } else {
        value = '';
      }

      return {
        annotationTypeId: this.getAnnotationTypeId(),
        numberValue:      value,
        selectedValues:   []
      };
    };

    NumberAnnotation.prototype.isValueValid = function () {
      return !(_.isNaN(this.value) || _.isUndefined(this.value));
    };

    return NumberAnnotation;
  }

  return NumberAnnotationFactory;
});
