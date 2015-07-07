/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
/* global define */
define(['underscore'], function(_) {
  'use strict';

  TextAnnotationFactory.$inject = ['Annotation'];

  function TextAnnotationFactory(Annotation) {

    /**
     * Please use annotationFactory.create to create annotation objects.
     */
    function TextAnnotation(obj, annotationType, required) {
      var self = this,
          defaults = {
            annotationTypeId     : null
          };

      obj = obj || {};
      _.extend(self, defaults, _.pick(obj, _.keys(defaults)));
      Annotation.call(this, annotationType, required);

      if (obj.stringValue) {
        this.value = obj.stringValue;
      }
    }

    TextAnnotation.prototype = Object.create(Annotation.prototype);

    TextAnnotation.prototype.getValue = function () {
      return this.value;
    };

    TextAnnotation.prototype.getServerAnnotation = function () {
      return {
        annotationTypeId: this.getAnnotationTypeId(),
        stringValue:      this.value || '',
        selectedValues:   []
      };
    };

    return TextAnnotation;
  }

  return TextAnnotationFactory;
});

