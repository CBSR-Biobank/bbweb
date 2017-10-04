/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(function(require) {
  'use strict';

  var _ = require('lodash');

  DateTimeAnnotationFactory.$inject = ['Annotation', 'timeService'];

  function DateTimeAnnotationFactory(Annotation, timeService) {

    /*
     * Please use annotationFactory.create to create annotation objects.
     */
    function DateTimeAnnotation(obj, annotationType) {
      var self = this;


      obj = obj || {};
      if (obj.stringValue) {
        this.value = new Date(obj.stringValue);
      } else {
        this.value = null;
      }

      Annotation.call(this, obj, annotationType);

      self.valueType = 'DateTime';
    }

    DateTimeAnnotation.prototype = Object.create(Annotation.prototype);

    /*
     * Must return a string.
     */
    DateTimeAnnotation.prototype.getValue = function () {
      if (_.isNull(this.value)) {
        return null;
      }
      return timeService.dateToDisplayString(this.value);
    };

    DateTimeAnnotation.prototype.getDisplayValue = function () {
      return this.getValue();
    };

    DateTimeAnnotation.prototype.setValue = function (value) {
      if (typeof value === 'string') {
        this.value = new Date(value);
      } else {
        this.value = value;
      }
    };

    /*
     *
     */
    DateTimeAnnotation.prototype.getServerAnnotation = function () {
      return {
        annotationTypeId: this.getAnnotationTypeId(),
        stringValue:      this.value ? timeService.dateAndTimeToUtcString(this.value) : '',
        selectedValues:   []
      };
    };

    return DateTimeAnnotation;
  }

  return DateTimeAnnotationFactory;
});
