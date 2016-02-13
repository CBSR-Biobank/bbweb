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
    function NumberAnnotation(obj, annotationType, required) {
      var self = this,
          defaults = {
            annotationTypeId : null
          };

      obj = obj || {};
      _.extend(self, defaults, _.pick(obj, _.keys(defaults)));
      Annotation.call(this, annotationType, required);

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

      if (!_.isNaN(this.value)) {
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

    NumberAnnotation.prototype.isValid = function () {
      return !(_.isNaN(this.value) || _.isUndefined(this.value));
    };

    return NumberAnnotation;
  }

  return NumberAnnotationFactory;
});
