/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
/* global define */
define(['underscore'], function(_) {
  'use strict';

  NumberAnnotationFactory.$inject = ['Annotation'];

  function NumberAnnotationFactory(Annotation) {

    /**
     * Please use annotationFactory.create to create annotation objects.
     */
    function NumberAnnotation(obj, annotationType, required) {
      var self = this,
          defaults = {
            annotationTypeId : null
          };

      obj = obj || {};
      _.extend(self, defaults, _.pick(obj, _.keys(defaults)));
      Annotation.call(this, annotationType, required);

      // convert number to a float
      if (obj.numberValue) {
        this.value = parseFloat(obj.numberValue);
      }
    }

    NumberAnnotation.prototype = Object.create(Annotation.prototype);

    NumberAnnotation.prototype.getValue = function () {
      return this.value;
    };

    NumberAnnotation.prototype.getServerAnnotation = function () {
      var value;

      if (this.value) {
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

    return NumberAnnotation;
  }

  return NumberAnnotationFactory;
});

