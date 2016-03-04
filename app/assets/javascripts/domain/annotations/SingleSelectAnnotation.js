/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(['underscore'], function(_) {
  'use strict';

  SingleSelectAnnotationFactory.$inject = ['Annotation'];

  function SingleSelectAnnotationFactory(Annotation) {

    /**
     * Please use annotationFactory.create to create annotation objects.
     */
    function SingleSelectAnnotation(obj, annotationType) {
      var self = this;

      obj = obj || {};
      Annotation.call(this, obj, annotationType);

      if (!_.isUndefined(obj.selectedValues)) {
        if (obj.selectedValues.length === 0) {
          self.value = null;
        } else if (obj.selectedValues.length === 1) {
          self.value = obj.selectedValues[0].value;
        } else {
          throw new Error('invalid value for selected values');
        }
      }
    }

    SingleSelectAnnotation.prototype = Object.create(Annotation.prototype);

    SingleSelectAnnotation.prototype.getValue = function () {
      return this.value;
    };

    SingleSelectAnnotation.prototype.getServerAnnotation = function () {
      var self = this,
          selectedValues = [];

      if (this.value) {
        selectedValues.push({ value: self.value });
      }

      return {
        annotationTypeId: this.getAnnotationTypeId(),
        selectedValues:   selectedValues
      };
    };

    return SingleSelectAnnotation;
  }

  return SingleSelectAnnotationFactory;
});
