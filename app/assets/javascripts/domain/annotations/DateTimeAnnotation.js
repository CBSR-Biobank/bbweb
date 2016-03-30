/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(['moment', 'underscore'], function(moment, _) {
  'use strict';

  DateTimeAnnotationFactory.$inject = ['Annotation', 'timeService'];

  function DateTimeAnnotationFactory(Annotation, timeService) {

    /**
     * Please use annotationFactory.create to create annotation objects.
     */
    function DateTimeAnnotation(obj, annotationType) {
      var self = this;

      obj = obj || {};
      Annotation.call(this, obj, annotationType);

      _.extend(self, timeService.stringToDateAndTime(obj.stringValue));
      self.valueType = 'DateTime';
  }

    DateTimeAnnotation.prototype = Object.create(Annotation.prototype);

    /**
     * Must return a string.
     */
    DateTimeAnnotation.prototype.getValue = function () {
      this.value = timeService.dateAndTimeToDisplayString(this.date, this.time);
      return this.value;
    };

    DateTimeAnnotation.prototype.setValue = function (value) {
      if (typeof value !== 'object') {
        throw new Error('value is not an object');
      }
      this.date = value.date;
      this.time = value.time;
      this.value = timeService.dateAndTimeToDisplayString(this.date, this.time);
    };

    /**
     * date part is kept in this.date and time in this.time,
     *
     * they must be combined
     */
    DateTimeAnnotation.prototype.getServerAnnotation = function () {
      return {
        annotationTypeId: this.getAnnotationTypeId(),
        stringValue:      timeService.dateAndTimeToUtcString(this.date, this.time),
        selectedValues:   []
      };
    };

    return DateTimeAnnotation;
  }

  return DateTimeAnnotationFactory;
});
